package es.ulpgc.inverted_index;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class InvertedIndex {

    private static Map<String, List<Integer>> invertedIndex = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-záéíóúüñ]+\\b", Pattern.CASE_INSENSITIVE);

    public static void loadIndex(String indexPath) throws IOException {
        Path path = Paths.get(indexPath);
        if (Files.exists(path)) {
            String content = Files.readString(path);
            Type type = new TypeToken<Map<String, List<Integer>>>(){}.getType();
            invertedIndex = gson.fromJson(content, type);
        } else {
            invertedIndex = new HashMap<>();
        }
    }

    public static void addWordsForBook(String text, int bookId) {
        Set<String> words = extractWords(text);

        for (String word : words) {
            invertedIndex.computeIfAbsent(word, k -> new ArrayList<>());
            if (!invertedIndex.get(word).contains(bookId)) {
                invertedIndex.get(word).add(bookId);
            }
        }
    }

    public static void cleanAndSort() {
        for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
            List<Integer> sortedUnique = entry.getValue().stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            entry.setValue(sortedUnique);
        }
    }

    public static void saveIndex(String indexPath) throws IOException {
        Path path = Paths.get(indexPath);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        String json = gson.toJson(invertedIndex);
        Files.writeString(path, json);
    }
    
    public static Map<String, List<Integer>> getIndex() {
        return invertedIndex;
    }

    private static Set<String> extractWords(String text) {
        return WORD_PATTERN.matcher(text.toLowerCase())
                .results()
                .map(match -> match.group())
                .collect(Collectors.toSet());
    }

}
