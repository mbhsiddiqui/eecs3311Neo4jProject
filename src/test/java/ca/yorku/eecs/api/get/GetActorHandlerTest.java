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
public class GetActorHandlerTest {

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
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/getActor?actorId=123"));
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	@Test
	public void testGetActorHandlerSuccess() throws IOException {
		when(statementResult.hasNext()).thenReturn(true);
		when(statementResult.single()).thenReturn(record);

		GetActorHandler handler = new GetActorHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	@Test
	public void testGetActorHandlerActorNotFound() throws IOException {
		when(statementResult.hasNext()).thenReturn(false);

		GetActorHandler handler = new GetActorHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	@Test
	public void testGetActorHandlerNoActorId() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/getActor"));

		GetActorHandler handler = new GetActorHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}
}
