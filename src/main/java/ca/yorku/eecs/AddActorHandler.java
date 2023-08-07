package ca.yorku.eecs;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;

import java.io.IOException;

import org.json.JSONObject;

/**
 * Handles the addition of actors to the Neo4j database via HTTP requests.
 * <p>
 * This handler checks if an actor with the provided actorId already exists in the database.
 * If the actor exists, a 400 status code is returned. If not, the actor is added and a 200 status code is returned.
 * </p>
 *
 * @since 2023-08-06
 */
public class AddActorHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * Constructs a new AddActorHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance used for database operations.
	 */
	public AddActorHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to add an actor to the database.
	 * <p>
	 * If the actor with the given actorId already exists, a 400 status code is returned.
	 * Otherwise, the actor is added and a 200 status code is returned.
	 *
	 * @param exchange The HTTP exchange object containing request and response details.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
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
					String response = "Actor with given actorId already exists.";
					exchange.sendResponseHeaders(400, response.length());
					exchange.getResponseBody().write(response.getBytes());
				} else {
					// Create new actor
					tx.run("CREATE (a:Actor {name: $name, actorId: $actorId})", Values.parameters("name", name, "actorId", actorId));

					String response = "Actor added successfully.";
					exchange.sendResponseHeaders(200, response.length());
					exchange.getResponseBody().write(response.getBytes());
				}

				// Commit the transaction
				tx.success();
			}
		} catch (Exception e) {
			// Exception occurred
			String response = "Internal server error.";
			exchange.sendResponseHeaders(500, response.length());
			exchange.getResponseBody().write(response.getBytes());
		} finally {
			exchange.getResponseBody().close();
		}
	}
}
