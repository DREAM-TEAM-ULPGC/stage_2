package es.ulpgc.catalog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ulpgc.model.BookMetadata;

public class MetadataExtractor {
    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("(\\d+)(?=_)");
    private static final Pattern TITLE_PATTERN = Pattern.compile("^\\s*Title\\s*:\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("^\\s*Author\\s*:\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern RELEASE_PATTERN = Pattern.compile("^\\s*Release\\s+date\\s*:\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^\\s*Language\\s*:\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern GUTENBERG_TRAIL = Pattern.compile("\\s*\\[eBook\\s*#\\d+\\]\\s*$", Pattern.CASE_INSENSITIVE);

    public static int extractBookId(String filename) {
        Matcher matcher = BOOK_ID_PATTERN.matcher(filename);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    public static BookMetadata parseHeaderMetadata(String text) {
        String title = findFirstGroup(text, TITLE_PATTERN);
        String author = findFirstGroup(text, AUTHOR_PATTERN);
        String release = findFirstGroup(text, RELEASE_PATTERN);
        if (release != null) {
            release = GUTENBERG_TRAIL.matcher(release).replaceFirst("");
            release = release.trim();
        }
        String language = findFirstGroup(text, LANGUAGE_PATTERN);

        BookMetadata bookMetadata = new BookMetadata();
        bookMetadata.setTitle(title);
        bookMetadata.setAuthor(author);
        bookMetadata.setReleaseDate(release);
        bookMetadata.setLanguage(language);
        return bookMetadata;
    }

    private static String findFirstGroup(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
