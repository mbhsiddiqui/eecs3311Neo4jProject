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
 * This class tests the functionality of AddActorHandler.
 * It verifies that the handler correctly interacts with
 * the underlying Neo4j driver and produces the expected
 * responses.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddActorHandlerTest {

	/**
	 * A mock HttpExchange object used to simulate HTTP requests.
	 */
	@Mock
	private HttpExchange httpExchange;

	/**
	 * A mock Driver object used to simulate interactions with a Neo4j database.
	 */
	@Mock
	private Driver driver;

	/**
	 * A mock Session object used to simulate database sessions.
	 */
	@Mock
	private Session session;

	/**
	 * A mock Transaction object used to simulate database transactions.
	 */
	@Mock
	private Transaction transaction;

	/**
	 * A mock StatementResult object used to simulate the result of running a statement.
	 */
	@Mock
	private StatementResult statementResult;

	/**
	 * A mock OutputStream object used to simulate the response body output stream.
	 */
	@Mock
	private OutputStream outputStream;

	/**
	 * Sets up the common mock behaviors for all tests.
	 * This method is run before each test method.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Before
	public void setUp() throws IOException {
		when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"name\": \"John Doe\", \"actorId\": \"123\"}".getBytes()));
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.beginTransaction()).thenReturn(transaction);
		when(transaction.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	/**
	 * Tests that the handler correctly adds an actor when
	 * there is no existing actor with the same ID.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void testAddActorHandlerSuccess() throws IOException {
		when(statementResult.hasNext()).thenReturn(false);

		AddActorHandler handler = new AddActorHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, times(1)).success();
	}

	/**
	 * Tests that the handler correctly handles a failure to
	 * add an actor when there is an existing actor with the
	 * same ID.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void testAddActorHandlerFailure() throws IOException {
		when(statementResult.hasNext()).thenReturn(true);

		AddActorHandler handler = new AddActorHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, never()).success();
	}
}
