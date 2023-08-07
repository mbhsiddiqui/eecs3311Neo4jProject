package ca.yorku.eecs.api.get;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;
import java.io.IOException;
import java.util.Map;

/**
 * Handles the verification of an ACTED_IN relationship between an actor and a movie in the Neo4j database via HTTP requests.
 * <p>
 * This handler checks if a relationship exists between a movie and an actor using the provided movieId and actorId.
 * </p>
 *
 * @since 2023-08-07
 */
public class HasRelationshipHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * Constructs a new HasRelationshipHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public HasRelationshipHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to verify an ACTED_IN relationship between an actor and a movie.
	 * <p>
	 * The relationship's existence is verified using the provided actorId and movieId.
	 * </p>
	 *
	 * @param exchange The HTTP exchange object containing request and response details.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// Extracting the query parameters
		String query = exchange.getRequestURI().getQuery();
		Map<String, String> queryParams = Utils.splitQuery(query);

		if (queryParams.containsKey("actorId") && queryParams.containsKey("movieId")) {
			String actorId = queryParams.get("actorId");
			String movieId = queryParams.get("movieId");

			try (Session session = driver.session()) {
				StatementResult result = session.run(
						"MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]->(m:Movie {movieId: $movieId}) RETURN r",
						Values.parameters("actorId", actorId, "movieId", movieId)
				);

				if (result.hasNext()) {
					String response = "Relationship exists.";
					exchange.sendResponseHeaders(200, response.length());
					exchange.getResponseBody().write(response.getBytes());
				} else {
					String response = "Relationship does not exist.";
					exchange.sendResponseHeaders(404, response.length());
					exchange.getResponseBody().write(response.getBytes());
				}
			} catch (Exception e) {
				String response = "Internal server error.";
				exchange.sendResponseHeaders(500, response.length());
				exchange.getResponseBody().write(response.getBytes());
			}
		} else {
			String response = "actorId and movieId are required.";
			exchange.sendResponseHeaders(400, response.length());
			exchange.getResponseBody().write(response.getBytes());
		}

		exchange.getResponseBody().close();
	}
}
