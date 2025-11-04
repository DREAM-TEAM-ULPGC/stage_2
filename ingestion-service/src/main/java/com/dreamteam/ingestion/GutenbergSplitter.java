package com.dreamteam.ingestion;

public class GutenbergSplitter {

    public record Parts(String header, String body) {}

    public static Parts splitHeaderBody(String text) {
        String startMarker = "*** START OF";
        String endMarker = "*** END OF";

        int start = indexOfLineContaining(text, startMarker);
        int end = indexOfLineContaining(text, endMarker);

        String header = "";
        String body = text;

        if (start >= 0) {
            header = text.substring(0, start);
            body = text.substring(start);
        }

        if (end >= 0 && end > start) {
            body = text.substring(start >= 0 ? start : 0, end);
        } else if (start >= 0) {
            body = text.substring(start);
        }

        return new Parts(header.strip(), body.strip());
    }

    private static int indexOfLineContaining(String text, String marker) {
        int idx = -1, from = 0;
        while (true) {
            int nl = text.indexOf('\n', from);
            if (nl < 0) {
                if (from < text.length() && text.substring(from).toUpperCase().contains(marker)) {
                    idx = from;
                }
                break;
            }

            String line = text.substring(from, nl);
            if (line.toUpperCase().contains(marker)) {
                idx = from;
                break;
            }

            from = nl + 1;
        }
        return idx;
    }
}