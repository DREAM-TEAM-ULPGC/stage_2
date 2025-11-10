package com.dreamteam;

import java.util.Map;

import com.dreamteam.config.ConfigLoader;
import com.dreamteam.service.IndexerService;
import com.google.gson.Gson;

import io.javalin.Javalin;


public class App {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String datalakePath = ConfigLoader.getProperty("datalake.path", "datalake");
        String indexOutputPath = ConfigLoader.getProperty("index.output.path", "indexer/inverted_index.json");
        String catalogOutputPath = ConfigLoader.getProperty("catalog.output.path", "metadata/catalog.json");
        String dbPath = ConfigLoader.getProperty("db.path", "datamart/datamart.db");
        String indexProgressPath = ConfigLoader.getProperty("index.progress.path", "indexer/progress.json");
        String catalogProgressPath = ConfigLoader.getProperty("catalog.progress.path", "metadata/progress_parser.json");
        int port = ConfigLoader.getIntProperty("server.port", 7002);

        IndexerService service = new IndexerService(
                datalakePath, indexOutputPath, catalogOutputPath,
                dbPath, indexProgressPath, catalogProgressPath
        );

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(port);

        System.out.println("Indexer Service started on port " + port);

        app.get("/status", ctx -> {
            Map<String, Object> response = Map.of(
                    "service", "indexer-service",
                    "status", "running"
            );
            ctx.result(gson.toJson(response));
        });

        app.get("/index/status", ctx -> {
            Map<String, Object> response = service.getStatus();
            ctx.result(gson.toJson(response));
        });

        app.post("/index/update/{book_id}", ctx -> {
            try {
                int bookId = Integer.parseInt(ctx.pathParam("book_id"));
                Map<String, Object> response = service.updateBookIndex(bookId);
                ctx.result(gson.toJson(response));
            } catch (NumberFormatException e) {
                ctx.status(400).result(gson.toJson(Map.of(
                        "error", "Invalid book_id format"
                )));
            }
        });

        app.post("/index/rebuild", ctx -> {
            Map<String, Object> response = service.rebuildIndex();
            ctx.result(gson.toJson(response));
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Indexer Service...");
            app.stop();
        }));
    }
}