package ca.yorku.eecs.handler.put;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.neo4j.driver.v1.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Mockito.*;

/**
 * Unit test class for the AddMovieHandler class. It includes tests for both
 * success and failure scenarios when attempting to add a movie.
 *
 * @since 2023-08-06
 */
@RunWith(MockitoJUnitRunner.class)
public class AddMovieHandlerTest {

	/**
	 * Mock object to simulate HTTP exchange.
	 */
	@Mock
	private HttpExchange httpExchange;

	/**
	 * Mock object to simulate the Neo4j database driver.
	 */
	@Mock
	private Driver driver;

	/**
	 * Mock object to simulate a database session.
	 */
	@Mock
	private Session session;

	/**
	 * Mock object to simulate a database transaction.
	 */
	@Mock
	private Transaction transaction;

	/**
	 * Mock object to simulate the result of a database statement.
	 */
	@Mock
	private StatementResult statementResult;

	/**
	 * Mock object to simulate an output stream for HTTP response.
	 */
	@Mock
	private OutputStream outputStream;

	/**
	 * Sets up the common mock behaviors for all tests. This method is run before each test.
	 *
	 * @throws IOException If there is an error setting up the input or output streams.
	 */
	@Before
	public void setUp() throws IOException {
		when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"name\": \"John Doe\", \"movieId\": \"123\"}".getBytes()));
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.beginTransaction()).thenReturn(transaction);
		when(transaction.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	/**
	 * Tests the successful addition of a new movie. This test case assumes the movie does not
	 * exist in the database prior to the operation.
	 *
	 * @throws IOException If there is an error writing to the output stream.
	 */
	@Test
	public void testAddMovieHandlerSuccess() throws IOException {
		when(statementResult.hasNext()).thenReturn(false);

		AddMovieHandler handler = new AddMovieHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, times(1)).success();
	}

	/**
	 * Tests the failure of adding a new movie due to the movie already existing in the database.
	 *
	 * @throws IOException If there is an error writing to the output stream.
	 */
	@Test
	public void testAddMovieHandlerFailure() throws IOException {
		when(statementResult.hasNext()).thenReturn(true);

		AddMovieHandler handler = new AddMovieHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, never()).success();
	}
}
