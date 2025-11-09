package es.ulpgc;

import java.util.Map;

import com.google.gson.Gson;

import es.ulpgc.service.IndexerService;
import io.javalin.Javalin;


public class App {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String datalakePath = System.getenv().getOrDefault("DATALAKE_PATH", "datalake");
        String indexOutputPath = System.getenv().getOrDefault("INDEX_OUTPUT_PATH", "indexer/inverted_index.json");
        String catalogOutputPath = System.getenv().getOrDefault("CATALOG_OUTPUT_PATH", "metadata/catalog.json");
        String dbPath = System.getenv().getOrDefault("DB_PATH", "datamart/datamart.db");
        String indexProgressPath = System.getenv().getOrDefault("INDEX_PROGRESS_PATH", "indexer/progress.json");
        String catalogProgressPath = System.getenv().getOrDefault("CATALOG_PROGRESS_PATH", "metadata/progress_parser.json");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7002"));

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