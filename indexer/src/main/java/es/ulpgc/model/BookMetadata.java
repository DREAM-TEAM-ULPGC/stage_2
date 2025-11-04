package es.ulpgc.model;

public class BookMetadata {
    private int bookId;
    private String title;
    private String author;
    private String releaseDate;
    private String language;
    
    public BookMetadata() {
    }
    
    public BookMetadata(int bookId, String title, String author, String releaseDate, String language) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.releaseDate = releaseDate;
        this.language = language;
    }

    public int getBookId() {
        return bookId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    @Override
    public String toString() {
        return "BookMetadata{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
