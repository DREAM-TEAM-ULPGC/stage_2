package es.ulpgc.control;

import io.javalin.Javalin;
import io.javalin.http.Context;

import com.google.gson.Gson;

import es.ulpgc.datamart.MetadataParser;
import es.ulpgc.datamart.MetadataStore;
import es.ulpgc.model.BookMetadata;
import es.ulpgc.inverted_index.InvertedIndex;

public class IndexerController {
    private final int port;
    private Javalin app;
    private final Gson gson = new Gson();

    public IndexerController(int port) {
        this.port = port;
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
        String bookId = ctx.pathParam("book_id");
        BookMetadata bookMetadata = MetadataParser.parseHeaderMetadata(bookId);
        MetadataStore.saveToDatamart(bookMetadata);
        InvertedIndex.loadIndex("datamart/inverted_index.json");
        InvertedIndex.addWordsForBook(bookMetadata.getContent(), Integer.parseInt(bookId));
        ctx.json("{\"status\":\"Index updated for book " + bookId + "\"}");
    }


}
