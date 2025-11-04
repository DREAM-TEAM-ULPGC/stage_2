package es.ulpgc.api;

import es.ulpgc.control.IndexerController;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;

public class ApiServer {
    private final int port;
    private final IndexerController controller;
    private Javalin app;
    private final Gson gson = new Gson();

    public ApiServer(int port, IndexerController controller) {
        this.port = port;
        this.controller = controller;
    }

    public void start() {
        app = Javalin.create(cfg -> cfg.http.defaultContentType = "application/json")
                .start(port);

        System.out.println("[ApiServer] Running on http://localhost:" + port);

        app.post("/index/update/{book_id}", this::handleUpdate);

        app.post("/index/rebuild", this::handleRebuild);

        app.get("/index/status", this::handleStatus);
    }

    public void stop() {
        if (app != null) app.stop();
    }


    private void handleUpdate(Context ctx) {
        try {
            int bookId = Integer.parseInt(ctx.pathParam("book_id"));
            controller.updateBook(bookId);
            ctx.status(200).result(gson.toJson(new UpdateResponse(bookId, "updated")));
        } catch (NumberFormatException e) {
            ctx.status(400).result("{\"error\": \"Invalid book_id parameter\"}");
        } catch (Exception e) {
            ctx.status(500).result("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleRebuild(Context ctx) {
        try {
            IndexStats stats = controller.rebuildAll();
            ctx.status(200).result(gson.toJson(stats));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleStatus(Context ctx) {
        try {
            IndexStatus status = controller.getStatus();
            ctx.status(200).result(gson.toJson(status));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }


    private static class UpdateResponse {
        private final int book_id;
        private final String index;

        public UpdateResponse(int book_id, String index) {
            this.book_id = book_id;
            this.index = index;
        }
    }
}
