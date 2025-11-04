package es.ulpgc.inverted_index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileProcessor {
    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("(\\d+)(?=_)");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-záéíóúüñ]+\\b");

    public int extractBookId(String filename) {
        Matcher matcher = BOOK_ID_PATTERN.matcher(filename);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    public Set<String> extractWords(String text) {
        Set<String> words = new HashSet<>();
        Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase());
        
        while (matcher.find()) {
            words.add(matcher.group());
        }
        
        return words;
    }
    
    public String readTextFile(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    public List<Path> getTxtFiles(Path directory) throws IOException {
        return Files.list(directory)
            .filter(path -> path.toString().endsWith(".txt"))
            .collect(Collectors.toList());
    }

    public List<Integer> getUniqueBookIds(List<Path> txtFiles) {
        Set<Integer> bookIds = new HashSet<>();
        
        for (Path file : txtFiles) {
            int id = extractBookId(file.getFileName().toString());
            if (id != -1) {
                bookIds.add(id);
            }
        }
        
        return bookIds.stream()
            .sorted()
            .collect(Collectors.toList());
    }
}