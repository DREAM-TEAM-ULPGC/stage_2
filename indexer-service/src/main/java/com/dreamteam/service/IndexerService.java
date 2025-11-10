package com.dreamteam.service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.dreamteam.core.InvertedIndexer;
import com.dreamteam.core.MetadataCatalogBuilder;
import com.dreamteam.datamart.DatamartInitializer;
import com.dreamteam.datamart.MetadataStore;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class IndexerService {
    private static final Gson gson = new Gson();

    private final String datalakePath;
    private final String indexOutputPath;
    private final String catalogOutputPath;
    private final String dbPath;
    private final String indexProgressPath;
    private final String catalogProgressPath;

    public IndexerService(String datalakePath, String indexOutputPath, String catalogOutputPath,
                          String dbPath, String indexProgressPath, String catalogProgressPath) {
        this.datalakePath = datalakePath;
        this.indexOutputPath = indexOutputPath;
        this.catalogOutputPath = catalogOutputPath;
        this.dbPath = dbPath;
        this.indexProgressPath = indexProgressPath;
        this.catalogProgressPath = catalogProgressPath;
    }


    public Map<String, Object> updateBookIndex(int bookId) {
        try {
            DatamartInitializer.initDatamart(dbPath);

            // Update index for specific book only
            InvertedIndexer indexer = new InvertedIndexer(datalakePath, indexOutputPath, indexProgressPath);
            indexer.updateBookIndex(bookId);

            // Update catalog for specific book only
            MetadataCatalogBuilder catalogBuilder = new MetadataCatalogBuilder(
                    datalakePath, catalogOutputPath, catalogProgressPath);
            catalogBuilder.updateBookCatalog(bookId);

            // Update datamart for this book
            MetadataStore store = new MetadataStore(dbPath);
            store.run(catalogOutputPath);

            return Map.of(
                    "book_id", bookId,
                    "index", "updated"
            );
        } catch (Exception e) {
            return Map.of(
                    "book_id", bookId,
                    "index", "failed",
                    "error", e.getMessage()
            );
        }
    }

    public Map<String, Object> rebuildIndex() {
        long startTime = System.currentTimeMillis();
        int booksProcessed = 0;

        try {
            Files.deleteIfExists(Paths.get(indexProgressPath));
            Files.deleteIfExists(Paths.get(catalogProgressPath));

            DatamartInitializer.initDatamart(dbPath);

            InvertedIndexer indexer = new InvertedIndexer(datalakePath, indexOutputPath, indexProgressPath);
            indexer.buildIndex();

            MetadataCatalogBuilder catalogBuilder = new MetadataCatalogBuilder(
                    datalakePath, catalogOutputPath, catalogProgressPath);
            catalogBuilder.buildCatalog();

            MetadataStore store = new MetadataStore(dbPath);
            booksProcessed = store.run(catalogOutputPath);

            double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;

            return Map.of(
                    "books_processed", booksProcessed,
                    "elapsed_time", String.format("%.1fs", elapsedSeconds)
            );
        } catch (Exception e) {
            return Map.of(
                    "books_processed", booksProcessed,
                    "error", e.getMessage()
            );
        }
    }

    public Map<String, Object> getStatus() {
        try {
            Path indexPath = Paths.get(indexOutputPath);

            long indexSize = Files.exists(indexPath) ? Files.size(indexPath) : 0;
            int booksIndexed = countBooksInCatalog();

            return Map.of(
                    "books_indexed", booksIndexed,
                    "last_update", Files.exists(indexPath) ?
                            Files.getLastModifiedTime(indexPath).toString() : "never",
                    "index_size_MB", String.format("%.2f", indexSize / (1024.0 * 1024.0))
            );
        } catch (IOException e) {
            return Map.of(
                    "error", e.getMessage()
            );
        }
    }

    private int countBooksInCatalog() {
        try {
            Path catalogPath = Paths.get(catalogOutputPath);
            if (!Files.exists(catalogPath)) {
                return 0;
            }

            String json = Files.readString(catalogPath);
            Map<?, ?> catalog = gson.fromJson(json, Map.class);
            return catalog != null ? catalog.size() : 0;
        } catch (JsonSyntaxException | IOException e) {
            return 0;
        }
    }
}
