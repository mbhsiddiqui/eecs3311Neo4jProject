package ca.yorku.eecs.handler.get;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Record;

/**
 * Handles the retrieval of actors from the Neo4j database via HTTP requests.
 * <p>
 * This handler checks if an actor with the given actorId already exists in the database.
 * If it does not, a 404 status code is returned. If it is, the actor with their list of
 * movies and a 200 status code is returned.
 * </p>
 *
 * @since 2023-08-07
 */
public class GetActorHandler implements HttpHandler {
    /**
     * The Neo4j database driver instance used for database operations.
     */
    private final Driver driver;

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(GetActorHandler.class.getName());

    /**
     * Constructs a new GetActorHandler with the provided Neo4j driver.
     *
     * @param driver The Neo4j driver instance.
     */
    public GetActorHandler(Driver driver) {
        this.driver = driver;
    }

    /**
     * Handles the HTTP request to get an actor from the database.
     * <p>
     * If the actor with the given actorId does not exists, a 404 status code is returned.
     * Otherwise, the actor with their list of movies and a 200 status code is returned.
     * </p>
     *
     * @param exchange The HTTP exchange object containing request and response details.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.log(Level.INFO, "Received request to get actor details.");

        // Extracting the query parameters
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = Utils.splitQuery(query);

        // Check if actorId is provided
        if (queryParams.containsKey("actorId")) {
            String actorId = queryParams.get("actorId");

            try (Session session = driver.session()) {
                // Run query to fetch actor and movies
                StatementResult result = session.run("MATCH (a:Actor {actorId: $actorId}) OPTIONAL MATCH (a)-[:ACTED_IN]->(m:Movie) RETURN a.name as name, a.actorId as actorId, collect(m.movieId) as movies", Values.parameters("actorId", actorId));

                if (result.hasNext()) {
                    Record record = result.single();
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("name", record.get("name").asString());
                    responseJson.put("actorId", record.get("actorId").asString());

                    // Adding movies
                    JSONArray moviesArray = new JSONArray();
                    for (Value movieId : record.get("movies").values()) {
                        if (movieId != null && !movieId.isNull()) { // Checking for null values in case the actor has not acted in any movies
                            moviesArray.put(movieId.asString());
                        }
                    }
                    responseJson.put("movies", moviesArray);

                    String response = responseJson.toString();
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                } else {
                    // Actor not found
                    String response = "Actor not found.";
                    exchange.sendResponseHeaders(404, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while retrieving actor details: " + e.getMessage(), e);
                String response = "Internal server error.";
                exchange.sendResponseHeaders(500, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
        } else {
            // actorId not provided in query params
            String response = "actorId is required.";
            exchange.sendResponseHeaders(400, response.length());
            exchange.getResponseBody().write(response.getBytes());
        }

        exchange.getResponseBody().close();
    }
}
