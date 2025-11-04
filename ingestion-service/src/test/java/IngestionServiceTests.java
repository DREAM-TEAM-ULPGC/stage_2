import com.dreamteam.ingestion.IngestionService;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IngestionServiceTests {

	private Path tempDatalake;
	private Path tempLogFile;
	private IngestionService service;
	private static final int EXISTING_BOOK_ID = 11; // 'Alice’s Adventures in Wonderland'

	@BeforeAll
	public void setupAll() throws IOException {
		tempDatalake = Paths.get("build/test-datalake");
		tempLogFile = tempDatalake.resolve("ingestions.log");
		service = new IngestionService(tempDatalake.toString(), tempLogFile.toString());
		Files.createDirectories(tempDatalake);
	}

	@AfterAll
	public void tearDownAll() throws IOException {
		if (Files.exists(tempDatalake)) {
			try (var walk = Files.walk(tempDatalake)) {
				walk.sorted(java.util.Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(java.io.File::delete);
			}
		}
	}

	@Test
	public void testListEmpty() {
		List<Integer> books = service.listBooks();
		assertTrue(books.isEmpty());
		assertEquals(0, books.size());
	}

	@Test
	public void testStatusMissing() {
		assertEquals("missing", service.status(9999999));
	}

	@Test
	@Tag("Integration")
	public void testSuccessfulIngestionAndStatus() {
		IngestionService.IngestionResult result = service.ingest(EXISTING_BOOK_ID);
		assertEquals("downloaded", result.status());
		assertTrue(result.path().contains(String.valueOf(EXISTING_BOOK_ID)));
		assertEquals("available", service.status(EXISTING_BOOK_ID));
	}

	@Test
	@Tag("Integration")
	@Order(1)
	public void testListBooksAfterIngestion() {
		service.ingest(EXISTING_BOOK_ID + 1);
		List<Integer> books = service.listBooks();
		assertTrue(books.contains(EXISTING_BOOK_ID));
		assertTrue(books.contains(EXISTING_BOOK_ID + 1));
		assertTrue(books.size() >= 2);
	}

	@Test
	@Tag("Integration")
	@Order(2)
	public void testIngestionIsIdempotent() throws IOException {
		service.ingest(EXISTING_BOOK_ID);
		long initialBookCount = service.listBooks().size();

		IngestionService.IngestionResult result = service.ingest(EXISTING_BOOK_ID);

		assertEquals("available", result.status());
		long finalBookCount = service.listBooks().size();
		assertEquals(initialBookCount, finalBookCount);

		long logLines = Files.exists(tempLogFile) ? Files.lines(tempLogFile).filter(l -> l.contains("book=" + EXISTING_BOOK_ID + ";")).count() : 0;
		assertEquals(1, logLines);
	}

	@Test
	@Tag("Integration")
	public void testIngestionCreatesHeaderAndBodyFiles() throws IOException {
		int testBookId = EXISTING_BOOK_ID + 2;
		IngestionService.IngestionResult result = service.ingest(testBookId);

		assumeTrue("downloaded".equals(result.status()));

		Path bookPath = tempDatalake.resolve(result.path());

		Path actualBookPath = service.findExistingBook(testBookId).orElseThrow(() -> new IOException("Book not found after ingestion."));

		assertTrue(Files.exists(actualBookPath.resolve("header.txt")));
		assertTrue(Files.exists(actualBookPath.resolve("body.txt")));
		assertTrue(Files.exists(actualBookPath.resolve("raw.txt")));
	}
}