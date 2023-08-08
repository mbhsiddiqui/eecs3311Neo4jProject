
package ca.yorku.eecs.handler.put;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * The AddActorHandler class is a HTTP handler that handles requests to add a new actor to the database.
 * It implements the HttpHandler interface provided by the com.sun.net.httpserver package.
 */
public class AddActorHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(AddActorHandler.class.getName());

	/**
	 * Constructs a new AddActorHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public AddActorHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to add a new actor.
	 * If the actor already exists in the database, an error response is sent.
	 *
	 * @param exchange The HTTP exchange object containing request and response details.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		logger.info("Received request to add actor");

		// Read the request body
		String body = Utils.getBody(exchange);

		try (Session session = driver.session()) {
			// Parse the JSON body
			JSONObject json = new JSONObject(body);
			String name = json.getString("name");
			String actorId = json.getString("actorId");

			// Start a transaction
			try (Transaction tx = session.beginTransaction()) {
				// Check if the actorId already exists
				StatementResult result = tx.run("MATCH (a:Actor {actorId: $actorId}) RETURN a", Values.parameters("actorId", actorId));

				if (result.hasNext()) {
					// Actor with given actorId already exists
					logger.warning("Attempted to add actor with existing actorId");
					String response = "Actor with given actorId already exists.";
					exchange.sendResponseHeaders(400, response.length());
					exchange.getResponseBody().write(response.getBytes());
				} else {
					// Create new actor
					tx.run("CREATE (a:Actor {name: $name, actorId: $actorId})", Values.parameters("name", name, "actorId", actorId));
					logger.info("Actor added successfully");
					String response = "Actor added successfully.";
					exchange.sendResponseHeaders(200, response.length());
					exchange.getResponseBody().write(response.getBytes());
				}

				// Commit the transaction
				tx.success();
			}
		} catch (Exception e) {
			// Exception occurred
			logger.log(Level.SEVERE, "Internal server error: " + e.getMessage(), e);
			String response = "Internal server error.";
			exchange.sendResponseHeaders(500, response.length());
			exchange.getResponseBody().write(response.getBytes());
		} finally {
			exchange.getResponseBody().close();
		}
	}
}
