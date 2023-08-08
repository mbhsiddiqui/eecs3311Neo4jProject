package ca.yorku.eecs.api.get;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HasRelationshipHandlerTest {

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

	@Mock
	private Record record;

	@Before
	public void setUp() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/hasRelationship?actorId=123&movieId=456"));
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	@Test
	public void testHasRelationshipHandlerSuccess() throws IOException {
		when(statementResult.hasNext()).thenReturn(true);

		HasRelationshipHandler handler = new HasRelationshipHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	@Test
	public void testHasRelationshipHandlerRelationshipNotFound() throws IOException {
		when(statementResult.hasNext()).thenReturn(false);

		HasRelationshipHandler handler = new HasRelationshipHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	@Test
	public void testHasRelationshipHandlerNoActorOrMovieId() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/hasRelationship"));

		HasRelationshipHandler handler = new HasRelationshipHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}
}
