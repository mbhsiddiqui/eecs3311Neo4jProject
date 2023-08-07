package ca.yorku.eecs.api.put;

import ca.yorku.eecs.api.put.AddActorHandler;
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

@RunWith(MockitoJUnitRunner.class)
public class AddActorHandlerTest {

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

	@Before
	public void setUp() throws IOException {
		when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"name\": \"John Doe\", \"actorId\": \"123\"}".getBytes()));
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.beginTransaction()).thenReturn(transaction);
		when(transaction.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	@Test
	public void testAddActorHandlerSuccess() throws IOException {
		when(statementResult.hasNext()).thenReturn(false);

		AddActorHandler handler = new AddActorHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, times(1)).success();
	}

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
