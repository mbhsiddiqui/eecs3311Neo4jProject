package ca.yorku.eecs.handler.get;

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

/**
 * This class is responsible for testing the ComputeBaconPathHandler.
 * It checks for different scenarios using Mockito to mock dependencies.
 *
 * @since 2023-08-07
 */
@RunWith(MockitoJUnitRunner.class)
public class ComputeBaconPathHandlerTest {

	/**
	 * Mock of the HttpExchange class. This is the argument that will be passed to the handle method
	 * of ComputeBaconPathHandler.
	 */
	@Mock
	private HttpExchange httpExchange;

	/**
	 * Mock of the Driver class, which is the Neo4j database driver.
	 * This is the argument that will be passed to the constructor of ComputeBaconPathHandler.
	 */
	@Mock
	private Driver driver;

	/**
	 * Mock of the Session class. This is used to mock the database session.
	 */
	@Mock
	private Session session;

	/**
	 * Mock of the StatementResult class. This is used to mock the result of the database query.
	 */
	@Mock
	private StatementResult statementResult;

	/**
	 * Mock of the OutputStream class. This is used to mock the output stream of the HttpExchange.
	 */
	@Mock
	private OutputStream outputStream;

	/**
	 * Mock of the Record class. This is used to mock a record of the database query result.
	 */
	@Mock
	private Record record;

	/**
	 * Mock of the Value class. This is used to mock the value of a field in the database record.
	 */
	@Mock
	private Value value;

	/**
	 * Mock of the Node class. This is used to mock a node in the path returned by the database query.
	 */
	@Mock
	private Node node;

	/**
	 * This method is called before each test. It sets up the mocks.
	 *
	 * @throws IOException If there's an issue with input or output.
	 */
	@Before
	public void setUp() throws IOException {
		when(httpExchange.getResponseBody()).thenReturn(outputStream);
		when(driver.session()).thenReturn(session);
		when(session.run(anyString(), any(Value.class))).thenReturn(statementResult);
	}

	/**
	 * This test verifies the successful computation of the Bacon path.
	 *
	 * @throws IOException If there's an issue with input or output.
	 */
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

	/**
	 * This test verifies the case where no path to Kevin Bacon is found.
	 *
	 * @throws IOException If there's an issue with input or output.
	 */
	@Test
	public void testComputeBaconPathHandlerNoPathFound() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/computeBaconPath?actorId=123"));
		when(statementResult.hasNext()).thenReturn(false);

		ComputeBaconPathHandler handler = new ComputeBaconPathHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}

	/**
	 * This test verifies the case where actorId is not provided in the URL.
	 *
	 * @throws IOException If there's an issue with input or output.
	 */
	@Test
	public void testComputeBaconPathHandlerNoActorId() throws IOException {
		when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/v1/computeBaconPath"));

		ComputeBaconPathHandler handler = new ComputeBaconPathHandler(driver);
		handler.handle(httpExchange);

		verify(outputStream).write(any(byte[].class));
		verify(outputStream).close();
	}
}
