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

@RunWith(MockitoJUnitRunner.class)
public class AddRelationshipHandlerTest {

	@Mock
	private HttpExchange httpExchange;

	@Mock
	private Driver driver;

	@Mock
	private Session session;

	@Mock
	private Transaction transaction;

	@Mock
	private StatementResult actorResult;

	@Mock
	private StatementResult movieResult;

	@Mock
	private StatementResult relationResult;

	@Mock
	private OutputStream outputStream;

	@Before
	public void setUp() throws IOException {
		when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"actorId\": \"123\", \"movieId\": \"456\"}".getBytes()));
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.beginTransaction()).thenReturn(transaction);
	}

	@Test
	public void testAddRelationshipHandlerSuccess() throws IOException {
		when(transaction.run(anyString(), any(Value.class))).thenReturn(actorResult, movieResult, relationResult);
		when(actorResult.hasNext()).thenReturn(true);
		when(movieResult.hasNext()).thenReturn(true);
		when(relationResult.hasNext()).thenReturn(false);

		AddRelationshipHandler handler = new AddRelationshipHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, times(1)).success();
	}

	@Test
	public void testAddRelationshipHandlerFailureDueToExistingRelationship() throws IOException {
		when(transaction.run(anyString(), any(Value.class))).thenReturn(actorResult, movieResult, relationResult);
		when(actorResult.hasNext()).thenReturn(true);
		when(movieResult.hasNext()).thenReturn(true);
		when(relationResult.hasNext()).thenReturn(true);

		AddRelationshipHandler handler = new AddRelationshipHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
		verify(transaction, never()).success();
	}
}
