package es.ulpgc.datamart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ulpgc.model.BookMetadata;

public class MetadataStore {
    private static final Logger logger = LoggerFactory.getLogger(MetadataStore.class);

    public static void saveToDatamart(BookMetadata metadata){
        String insertQuery = "INSERT OR REPLACE INTO books (book_id, title, author, release_date, language) " +
                                    "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatamartInitializer.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setInt(1, metadata.getBookId());
            preparedStatement.setString(2, metadata.getTitle());
            preparedStatement.setString(3, metadata.getAuthor());
            preparedStatement.setString(4, metadata.getReleaseDate());
            preparedStatement.setString(5, metadata.getLanguage());

            preparedStatement.executeUpdate();
            logger.info("Metadata for book ID {} saved successfully.", metadata.getBookId());
        } catch (SQLException exception) {
            logger.error("Error saving metadata for book ID {}: {}", metadata.getBookId(), exception.getMessage());
        }
    }
}
