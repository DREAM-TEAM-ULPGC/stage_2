package com.dreamteam.ingestion;

import java.nio.file.Path;

public class DatalakeLayout {
	public static Path bookRoot(Path datalake, String dayYYYYMMDD, String hourHH, int bookId) {
		return datalake.resolve(dayYYYYMMDD).resolve(hourHH).resolve(String.valueOf(bookId));
	}
}
