package es.ulpgc.control;

import es.ulpgc.catalog.CatalogLoader;
import es.ulpgc.config.Config;
import es.ulpgc.datamart.DatamartInitializer;
import es.ulpgc.inverted_index.FileProcessor;
import es.ulpgc.inverted_index.InvertedIndex;
import es.ulpgc.metadata.MetadataExtractor;
import es.ulpgc.metadata.MetadataStore;
import es.ulpgc.metadata.BookMetadata;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class IndexerController {

    private final CatalogLoader catalogLoader;
    private final MetadataExtractor metadataExtractor;
    private final MetadataStore metadataStore;
    private final InvertedIndex invertedIndex;
    private final DatamartInitializer datamartInitializer;
    private final Config config;
    private final Path progressPath;

    public IndexerController(CatalogLoader catalogLoader,
                             MetadataExtractor metadataExtractor,
                             MetadataStore metadataStore,
                             InvertedIndex invertedIndex,
                             DatamartInitializer datamartInitializer,
                             Config config) {
        this.catalogLoader = catalogLoader;
        this.metadataExtractor = metadataExtractor;
        this.metadataStore = metadataStore;
        this.invertedIndex = invertedIndex;
        this.datamartInitializer = datamartInitializer;
        this.config = config;
        this.progressPath = Paths.get(config.getProperty("progress.path"));

        datamartInitializer.initializeDatamart();
        System.out.println("[IndexerController] Datamart initialized successfully.");
    }

    public void updateBook(int bookId) {
        System.out.printf("[IndexerController] Updating book ID %d...%n", bookId);
        try {
            String text = catalogLoader.loadBook(bookId);

            BookMetadata metadata = metadataExtractor.extractMetadata(text, bookId);

            metadataStore.insertOrUpdate(metadata);

            Set<String> words = invertedIndex.extractWords(text);

            invertedIndex.addWordsForBook(words, bookId);
            invertedIndex.cleanAndSort();
            invertedIndex.saveIndex(config.getProperty("index.path"));

            saveProgress(bookId);

        } catch (Exception e) {
            System.err.printf("[IndexerController] Error updating book %d: %s%n", bookId, e.getMessage());
        }
    }

        public void rebuild() {
        System.out.println("[IndexerController] Starting full reindex...");
        long start = System.nanoTime();
        int lastIndexed = getLastIndexedBook();

        try {
            invertedIndex.loadIndex(config.getProperty("index.path"));
        } catch (IOException e) {
            System.err.println("[IndexerController] Warning: No previous index found, starting fresh.");
        }

        try {
            Path datalakeDir = Paths.get(config.getProperty("datalake.dir"));
            List<Path> txtFiles = fileProcessor.getTxtFiles(datalakeDir);
            List<Integer> bookIds = fileProcessor.getUniqueBookIds(txtFiles);

            List<Integer> pendingIds = bookIds.stream()
                    .filter(id -> id > lastIndexed)
                    .collect(Collectors.toList());

            for (int bookId : pendingIds) {
                updateBook(bookId);
            }

            long elapsed = (System.nanoTime() - start) / 1e9;
            System.out.printf("[IndexerController] Reindex complete. Books processed: %d, time: %.2fs%n",
                    pendingIds.size(), elapsed);

        } catch (IOException e) {
            System.err.println("[IndexerController] Error reading datalake directory: " + e.getMessage());
        }
    }

    private void saveProgress(int lastBookId) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("last_book_indexed", lastBookId);
        progress.put("timestamp", Instant.now().toString());

        try {
            Files.createDirectories(progressPath.getParent());
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(progress);
            Files.writeString(progressPath, json);
        } catch (IOException e) {
            System.err.println("[IndexerController] Failed to write progress file: " + e.getMessage());
        }
    }


    public IndexStatus getStatus() {
        int booksIndexed = 0;
        Instant lastUpdate = Instant.EPOCH;
        double indexSizeMB = 0.0;

        try {
            booksIndexed = metadataStore.countBooks();
        } catch (Exception e) {
            System.err.println("[IndexerController] Unable to count books: " + e.getMessage());
        }

        Path progressFile = progressPath;
        if (Files.exists(progressFile)) {
            try {
                String json = Files.readString(progressFile);
                var obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                String timestamp = obj.has("timestamp") ? obj.get("timestamp").getAsString() : null;
                if (timestamp != null) {
                    lastUpdate = Instant.parse(timestamp);
                }
            } catch (Exception e) {
                System.err.println("[IndexerController] Failed to read progress timestamp: " + e.getMessage());
            }
        }

        try {
            Path indexPath = Paths.get(config.getProperty("index.path"));
            if (Files.exists(indexPath)) {
                long bytes = Files.size(indexPath);
                indexSizeMB = bytes / (1024.0 * 1024.0);
            }
        } catch (IOException e) {
            System.err.println("[IndexerController] Could not calculate index size: " + e.getMessage());
        }

        return new IndexStatus(booksIndexed, lastUpdate, indexSizeMB);
    }

    private int getLastIndexedBook() {
        if (!Files.exists(progressPath)) return -1;
        try {
            String json = Files.readString(progressPath);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.get("last_book_indexed").getAsInt();
        } catch (Exception e) {
            return -1;
        }
    }
}
