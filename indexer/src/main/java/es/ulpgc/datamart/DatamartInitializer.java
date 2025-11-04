package es.ulpgc.datamart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import es.ulpgc.config.Config;

public class DatamartInitializer {
    private static final String DB_PATH = Config.getProperty("datamart.db.path");
    private static final String INIT_SCRIPT = "indexer/src/main/resources/init.sql";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    public static void initializeDatamart() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            String sql = new String(Files.readAllBytes(Paths.get(INIT_SCRIPT)), StandardCharsets.UTF_8);
            statement.executeUpdate(sql);
            System.out.println("Databse succesfully initialized.");

        } catch (SQLException exception) {
            System.err.println("Error initializing database: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Error reading initialization script: " + exception.getMessage());
        }
    }
}
