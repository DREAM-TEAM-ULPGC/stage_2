package com.dreamteam.search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dreamteam.search.models.Book;


public class MetadataDao implements AutoCloseable {
    private Connection connection;
    private String jdbcUrl;

    public MetadataDao(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        connect();
    }

    private void connect() {
        try { this.connection = DriverManager.getConnection(jdbcUrl); }
        catch (SQLException exception) { throw new RuntimeException("Cannot connect to SQLite: " + jdbcUrl, exception); }
    }

    public void reload(String jdbcUrl) {
        close();
        this.jdbcUrl = jdbcUrl;
        connect();
    }

    public Book getBookById(int id) {
        String sql = "SELECT book_id, title, author, language FROM books WHERE book_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) return null;
                return new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("language")
                );
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }
    }

    public List<SearchEngine.ScoredDoc> enrichAndFilter(List<SearchEngine.ScoredDoc> docs,
                                                        String authorFilter,
                                                        String languageFilter) {
        if (docs.isEmpty()) return docs;
        String inClause = docs.stream().map(d -> "?").collect(Collectors.joining(","));
        String sql = "SELECT book_id, title, author, language FROM books WHERE book_id IN (" + inClause + ")";
        Map<Integer, Book> byId = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int i = 1;
            for (var doc : docs) preparedStatement.setInt(i++, doc.bookId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    byId.put(rs.getInt("book_id"), new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("language")
                    ));
                }
            }
        } catch (SQLException exception) { throw new RuntimeException(exception); }

        return docs.stream()
                .filter(doc -> {
                    Book book = byId.get(doc.bookId);
                    if (book == null) return false;
                    boolean ok = true;
                    if (authorFilter != null && !authorFilter.isBlank()) {
                        ok &= book.author() != null &&
                              book.author().toLowerCase().contains(authorFilter.toLowerCase());
                    }
                    if (languageFilter != null && !languageFilter.isBlank()) {
                        ok &= book.language() != null &&
                              book.language().toLowerCase().startsWith(languageFilter.toLowerCase());
                    }
                    return ok;
                })
                .map(doc -> new SearchEngine.ScoredDoc(doc.bookId, doc.score))
                .toList();
    }

    @Override public void close() {
        try { if (connection != null) connection.close(); } catch (SQLException ignored) {}
    }
}
