package ca.yorku.eecs.api.get;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComputeBaconPathHandlerTest {

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

	@Mock
	private Value value;

	@Mock
	private Node node;

	@Before
	public void setUp() throws IOException {
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	@Test
	public void testComputeBaconPathHandlerSuccess() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/computeBaconPath?actorId=123"));
		when(statementResult.hasNext()).thenReturn(true);
		when(statementResult.single()).thenReturn(record);
		when(record.get("nodes")).thenReturn(value);
		when(value.asList()).thenReturn(Arrays.asList(value, value));
		when(value.asNode()).thenReturn(node);
		when(node.hasLabel("Actor")).thenReturn(true);
		when(node.get("actorId")).thenReturn(value);
		when(value.asString()).thenReturn("123");

		ComputeBaconPathHandler handler = new ComputeBaconPathHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	@Test
	public void testComputeBaconPathHandlerNoPathFound() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/computeBaconPath?actorId=123"));
		when(statementResult.hasNext()).thenReturn(false);

		ComputeBaconPathHandler handler = new ComputeBaconPathHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	@Test
	public void testComputeBaconPathHandlerNoActorId() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/computeBaconPath"));

		ComputeBaconPathHandler handler = new ComputeBaconPathHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}
}
