package ca.yorku.eecs.api.get;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.types.Node;

/**
 * Handles the computation of the Bacon path for a given actor from the Neo4j database via HTTP requests.
 * <p>
 * This handler computes the shortest path of alternating actors and movies between the provided actor and Kevin Bacon.
 * Kevin Bacon is always assumed to have an actorId of nm0000102.
 * </p>
 *
 * @since 2023-08-07
 */
public class ComputeBaconPathHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * The constant actorId for Kevin Bacon.
	 */
	private static final String KEVIN_BACON_ID = "nm0000102";

	/**
	 * Constructs a new ComputeBaconPathHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public ComputeBaconPathHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to compute the Bacon path for a given actor.
	 * <p>
	 * Computes the shortest path of alternating actors and movies between the provided actor and Kevin Bacon.
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
				JSONArray baconPath = new JSONArray();
				baconPath.put(KEVIN_BACON_ID);
				try {
					responseJson.put("baconPath", baconPath);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}

				String response = responseJson.toString();
				exchange.sendResponseHeaders(200, response.length());
				exchange.getResponseBody().write(response.getBytes());
				return;
			}

			try (Session session = driver.session()) {
				StatementResult result = session.run("MATCH p=shortestPath((a:Actor {actorId: $actorId})-[:ACTED_IN*]-(b:Actor {actorId: $kevinBaconId})) RETURN nodes(p) AS nodes", Values.parameters("actorId", actorId, "kevinBaconId", KEVIN_BACON_ID));

				if (result.hasNext()) {
					List<Object> nodesList = result.single().get("nodes").asList();
					JSONArray baconPath = new JSONArray();
					for (Object nodeValue : nodesList) {
						if (nodeValue instanceof Value) {
							Node node = ((Value) nodeValue).asNode();

							if (node.hasLabel("Actor")) {
								baconPath.put(node.get("actorId").asString());
							} else if (node.hasLabel("Movie")) {
								baconPath.put(node.get("movieId").asString());
							}
						}
					}


					JSONObject responseJson = new JSONObject();
					responseJson.put("baconPath", baconPath);

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
