package ca.yorku.eecs.api.get;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

/**
 * Handles the computation of the Bacon number for a given actor from the Neo4j database via HTTP requests.
 * <p>
 * This handler computes the shortest path between the provided actor and Kevin Bacon, returning the Bacon number.
 * Kevin Bacon is always assumed to have an actorId of nm0000102 and a Bacon number of 0.
 * </p>
 *
 * @since 2023-08-07
 */
public class ComputeBaconNumberHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * The constant actorId for Kevin Bacon.
	 */
	private static final String KEVIN_BACON_ID = "nm0000102";

	/**
	 * Constructs a new ComputeBaconNumberHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public ComputeBaconNumberHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to compute the Bacon number for a given actor.
	 * <p>
	 * Computes the shortest path between the provided actor and Kevin Bacon and returns the Bacon number.
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

		if (queryParams.containsKey("actorId")) {
			String actorId = queryParams.get("actorId");

			if (KEVIN_BACON_ID.equals(actorId)) {
				JSONObject responseJson = new JSONObject();
				try {
					responseJson.put("baconNumber", 0);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}

				String response = responseJson.toString();
				exchange.sendResponseHeaders(200, response.length());
				exchange.getResponseBody().write(response.getBytes());
				return;
			}

			try (Session session = driver.session()) {
				StatementResult result = session.run(
						"MATCH p=shortestPath((a:Actor {actorId: $actorId})-[:ACTED_IN*]-(b:Actor {actorId: $kevinBaconId})) RETURN length(p)/2 AS baconNumber",
						Values.parameters("actorId", actorId, "kevinBaconId", KEVIN_BACON_ID)
				);

				if (result.hasNext()) {
					int baconNumber = result.single().get("baconNumber").asInt();
					JSONObject responseJson = new JSONObject();
					responseJson.put("baconNumber", baconNumber);

					String response = responseJson.toString();
					exchange.sendResponseHeaders(200, response.length());
					exchange.getResponseBody().write(response.getBytes());
				} else {
					String response = "No path to Kevin Bacon found.";
					exchange.sendResponseHeaders(404, response.length());
					exchange.getResponseBody().write(response.getBytes());
				}
			} catch (Exception e) {
				String response = "Internal server error.";
				exchange.sendResponseHeaders(500, response.length());
				exchange.getResponseBody().write(response.getBytes());
			}
		} else {
			String response = "actorId is required.";
			exchange.sendResponseHeaders(400, response.length());
			exchange.getResponseBody().write(response.getBytes());
		}

		exchange.getResponseBody().close();
	}
}
