package es.ulpgc.catalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.ulpgc.model.BookMetadata;

public class CatalogLoader {
    private static final Path catalogPath = Path.of("metadata/catalog.json");

    private static final Gson gson = new Gson();
  
    public static Map<String, BookMetadata> loadCatalog() throws IOException {
        try {
            String json = Files.readString(catalogPath);
            Map<String, BookMetadata> catalog = gson.fromJson(json,
            new TypeToken<Map<String, BookMetadata>>(){}.getType());

            if (catalog == null) {
                throw new IOException("Catalog JSON must be a map of book_id to metadata");
            }

            return catalog;
            
        } catch (IOException exception) {
            throw new IOException("Error accessing catalog: " + exception.getMessage());
        }
    }
}
