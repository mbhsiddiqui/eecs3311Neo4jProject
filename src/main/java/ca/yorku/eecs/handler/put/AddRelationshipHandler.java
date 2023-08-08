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
 * Handles the addition of ACTED_IN relationships between an actor and a movie in the Neo4j database via HTTP requests.
 * <p>
 * This handler checks if the actor and movie nodes with the provided IDs exist in the database.
 * If both nodes exist, it establishes an ACTED_IN relationship. If the relationship already exists,
 * a 400 status code is returned. If one of the nodes doesn't exist, a 404 status code is returned.
 * </p>
 * <p>
 * Implements the HttpHandler interface to handle the HTTP request and response.
 * </p>
 *
 * @since 2023-08-06
 */
public class AddRelationshipHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(AddRelationshipHandler.class.getName());

	/**
	 * Constructs a new AddRelationshipHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public AddRelationshipHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to add an ACTED_IN relationship between an actor and a movie.
	 * <p>
	 * Establishes the relationship if both nodes exist and the relationship is not present.
	 * Otherwise, the appropriate error status code is returned.
	 * </p>
	 *
	 * @param exchange The HTTP exchange object containing request and response details.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		logger.info("Received request to add relationship");

		String body = Utils.getBody(exchange);

		try (Session session = driver.session()) {
			JSONObject json = new JSONObject(body);
			String actorId = json.getString("actorId");
			String movieId = json.getString("movieId");

			try (Transaction tx = session.beginTransaction()) {
				StatementResult actorResult = tx.run("MATCH (a:Actor {actorId: $actorId}) RETURN a", Values.parameters("actorId", actorId));
				StatementResult movieResult = tx.run("MATCH (m:Movie {movieId: $movieId}) RETURN m", Values.parameters("movieId", movieId));

				if (!actorResult.hasNext() || !movieResult.hasNext()) {
					logger.warning("Attempted to add relationship with non-existent actor or movie");
					String response = "Actor or Movie not found.";
					exchange.sendResponseHeaders(404, response.length());
					exchange.getResponseBody().write(response.getBytes());
					return;
				}

				StatementResult relationResult = tx.run("MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]->(m:Movie {movieId: $movieId}) RETURN r", Values.parameters("actorId", actorId, "movieId", movieId));

				if (relationResult.hasNext()) {
					logger.warning("Attempted to add existing relationship");
					String response = "Relationship already exists.";
					exchange.sendResponseHeaders(400, response.length());
					exchange.getResponseBody().write(response.getBytes());
				} else {
					tx.run("MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId}) CREATE (a)-[:ACTED_IN]->(m)", Values.parameters("actorId", actorId, "movieId", movieId));
					logger.info("Relationship added successfully");
					String response = "Relationship added successfully.";
					exchange.sendResponseHeaders(200, response.length());
					exchange.getResponseBody().write(response.getBytes());
				}

				tx.success();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Internal server error: " + e.getMessage(), e);
			String response = "Internal server error.";
			exchange.sendResponseHeaders(500, response.length());
			exchange.getResponseBody().write(response.getBytes());
		} finally {
			exchange.getResponseBody().close();
		}
	}
}
