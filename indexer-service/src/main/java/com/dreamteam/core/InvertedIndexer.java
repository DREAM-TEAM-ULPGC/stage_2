package com.dreamteam.core;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.dreamteam.progress.ProgressTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class InvertedIndexer {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-záéíóúüñ]+\\b");

    private final String datalakePath;
    private final String outputPath;
    private final String progressPath;

    public InvertedIndexer(String datalakePath, String outputPath, String progressPath) {
        this.datalakePath = datalakePath;
        this.outputPath = outputPath;
        this.progressPath = progressPath;
    }


    public void buildIndex() throws IOException {
        Path datalake = Paths.get(datalakePath);
        Path output = Paths.get(outputPath);

        ProgressTracker progress = ProgressTracker.load(progressPath);
        System.out.println("Last progress: " + progress);

        Map<String, List<Integer>> invertedIndex = loadIndex(output);

        List<Path> dayFolders = Files.list(datalake)
                .filter(Files::isDirectory)
                .sorted()
                .collect(Collectors.toList());

        for (Path dayFolder : dayFolders) {
            String dayName = dayFolder.getFileName().toString();

            if (progress.getLastDay() != null && dayName.compareTo(progress.getLastDay()) < 0) {
                continue;
            }

            List<Path> hourFolders = Files.list(dayFolder)
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparingInt(p -> {
                        String name = p.getFileName().toString();
                        return name.matches("\\d+") ? Integer.valueOf(name) : 0;
                    }))
                    .collect(Collectors.toList());

            for (Path hourFolder : hourFolders) {
                String hourName = hourFolder.getFileName().toString();

                if (dayName.equals(progress.getLastDay()) && progress.getLastHour() != null) {
                    if (hourName.matches("\\d+") && progress.getLastHour().matches("\\d+")) {
                        if (Integer.parseInt(hourName) < Integer.parseInt(progress.getLastHour())) {
                            continue;
                        }
                    }
                }

                System.out.printf("Processing day/hour %s/%s ...%n", dayName, hourName);

                List<Path> bookFolders = Files.list(hourFolder)
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().matches("\\d+"))
                        .sorted(Comparator.comparingInt(p -> Integer.valueOf(p.getFileName().toString())))
                        .collect(Collectors.toList());

                for (Path bookFolder : bookFolders) {
                    int bookId = Integer.parseInt(bookFolder.getFileName().toString());

                    if (dayName.equals(progress.getLastDay()) &&
                        hourName.equals(progress.getLastHour()) &&
                        bookId <= progress.getLastIndexedId()) {
                        continue;
                    }

                    Path bodyFile = bookFolder.resolve("body.txt");
                    if (!Files.exists(bodyFile)) {
                        continue;
                    }

                    String text = Files.readString(bodyFile).toLowerCase();
                    Matcher matcher = WORD_PATTERN.matcher(text);
                    Set<String> words = new HashSet<>();
                    while (matcher.find()) {
                        words.add(matcher.group());
                    }

                    if (words.isEmpty()) {
                        continue;
                    }

                    for (String word : words) {
                        invertedIndex.computeIfAbsent(word, k -> new ArrayList<>());
                        if (!invertedIndex.get(word).contains(bookId)) {
                            invertedIndex.get(word).add(bookId);
                        }
                    }

                    progress.setLastIndexedId(Math.max(progress.getLastIndexedId(), bookId));
                    System.out.printf("Indexed book ID %d (%s/%s)%n", bookId, dayName, hourName);
                }

                for (List<Integer> ids : invertedIndex.values()) {
                    Collections.sort(ids);
                }

                saveIndex(output, invertedIndex);
                progress.setLastDay(dayName);
                progress.setLastHour(hourName);
                progress.save(progressPath);
                System.out.printf("Progress saved: %s/%s (last ID: %d)%n",
                        dayName, hourName, progress.getLastIndexedId());
            }
        }

        System.out.printf("Indexing complete. Last indexed: %s/%s%n",
                progress.getLastDay(), progress.getLastHour());
    }

    public void updateBookIndex(int bookId) throws IOException {
        Path datalake = Paths.get(datalakePath);
        Path output = Paths.get(outputPath);

        Map<String, List<Integer>> invertedIndex = loadIndex(output);

        Path bookPath = findBookPath(datalake, bookId);
        if (bookPath == null) {
            throw new IOException("Book ID " + bookId + " not found in datalake");
        }

        Path bodyFile = bookPath.resolve("body.txt");
        if (!Files.exists(bodyFile)) {
            throw new IOException("body.txt not found for book ID " + bookId);
        }

        for (List<Integer> bookIds : invertedIndex.values()) {
            bookIds.remove(Integer.valueOf(bookId));
        }

        String text = Files.readString(bodyFile).toLowerCase();
        Matcher matcher = WORD_PATTERN.matcher(text);
        Set<String> words = new HashSet<>();
        while (matcher.find()) {
            words.add(matcher.group());
        }

        for (String word : words) {
            invertedIndex.computeIfAbsent(word, k -> new ArrayList<>());
            if (!invertedIndex.get(word).contains(bookId)) {
                invertedIndex.get(word).add(bookId);
            }
        }

        for (List<Integer> ids : invertedIndex.values()) {
            Collections.sort(ids);
        }

        saveIndex(output, invertedIndex);
        System.out.printf("Updated index for book ID %d%n", bookId);
    }

    private Path findBookPath(Path datalake, int bookId) throws IOException {
        String bookIdStr = String.valueOf(bookId);
        
        List<Path> dayFolders = Files.list(datalake)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());

        for (Path dayFolder : dayFolders) {
            List<Path> hourFolders = Files.list(dayFolder)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());

            for (Path hourFolder : hourFolders) {
                Path bookFolder = hourFolder.resolve(bookIdStr);
                if (Files.exists(bookFolder) && Files.isDirectory(bookFolder)) {
                    return bookFolder;
                }
            }
        }

        return null;
    }

    private Map<String, List<Integer>> loadIndex(Path output) throws IOException {
        if (Files.exists(output)) {
            String json = Files.readString(output);
            return gson.fromJson(json, new TypeToken<Map<String, List<Integer>>>(){}.getType());
        }
        return new HashMap<>();
    }

    private void saveIndex(Path output, Map<String, List<Integer>> index) throws IOException {
        Files.createDirectories(output.getParent());
        String json = gson.toJson(index);
        Files.writeString(output, json);
    }
}
