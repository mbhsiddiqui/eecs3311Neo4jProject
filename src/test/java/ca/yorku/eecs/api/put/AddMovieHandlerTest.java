package ca.yorku.eecs.api.put;

import ca.yorku.eecs.api.put.AddMovieHandler;
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
 * This class contains unit tests for the AddMovieHandler class.
 * It tests both the success and failure scenarios when a movie is added.
 *
 * @since 2023-08-06
 */
@RunWith(MockitoJUnitRunner.class)
public class AddMovieHandlerTest {

	@Mock
	private HttpExchange httpExchange;

	@Mock
	private Driver driver;

	@Mock
	private Session session;

	@Mock
	private Transaction transaction;

	@Mock
	private StatementResult statementResult;

	@Mock
	private OutputStream outputStream;

	/**
	 * This method sets up the common mock behaviors used in the test methods.
	 * It is run before each test case.
	 *
	 * @throws IOException If an I/O error occurs while setting up the mocks.
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
	 * This test case tests the scenario where a new movie is successfully added.
	 * In this scenario, the movie does not exist in the database before the operation.
	 *
	 * @throws IOException If there is an error reading the HTTP request body.
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
	 * This test case tests the scenario where the addition of a movie fails because the movie already exists.
	 *
	 * @throws IOException If there is an error reading the HTTP request body.
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