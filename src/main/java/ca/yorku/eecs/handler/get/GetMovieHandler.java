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
 * Handles the retrieval of movies from the Neo4j database via HTTP requests.
 * <p>
 * This handler checks if a movie with the given movieId already exists in the database.
 * If it does not, a 404 status code is returned. If it is, the movie with their list of
 * actors and a 200 status code is returned.
 * </p>
 *
 * @since 2023-08-07
 */
public class GetMovieHandler implements HttpHandler {
    /**
     * The Neo4j database driver instance used for database operations.
     */
    private final Driver driver;

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(GetMovieHandler.class.getName());

    /**
     * Constructs a new GetMovieHandler with the provided Neo4j driver.
     *
     * @param driver The Neo4j driver instance.
     */
    public GetMovieHandler(Driver driver) {
        this.driver = driver;
    }

    /**
     * Handles the HTTP request to get a movie from the database.
     * <p>
     * If the movie with the given movieId does not exists, a 404 status code is returned.
     * Otherwise, the movie with their list of actors and a 200 status code is returned.
     * </p>
     *
     * @param exchange The HTTP exchange object containing request and response details.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.log(Level.INFO, "Received request to get movie details.");

        // Extracting the query parameters
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = Utils.splitQuery(query);

        // Check if movieId is provided
        if (queryParams.containsKey("movieId")) {
            String movieId = queryParams.get("movieId");

            try (Session session = driver.session()) {
                // Run query to fetch movie and actors
                StatementResult result = session.run("MATCH (m:Movie {movieId: $movieId}) OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor) RETURN m.name as name, m.movieId as movieId, collect(a.actorId) as actors", Values.parameters("movieId", movieId));

                if (result.hasNext()) {
                    Record record = result.single();
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("name", record.get("name").asString());
                    responseJson.put("movieId", record.get("movieId").asString());

                    // Adding actors
                    JSONArray actorsArray = new JSONArray();
                    for (Value actorId : record.get("actors").values()) {
                        if (actorId != null && !actorId.isNull()) { // Checking for null values in case no actors acted in the movie
                            actorsArray.put(actorId.asString());
                        }
                    }
                    responseJson.put("actors", actorsArray);

                    String response = responseJson.toString();
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                } else {
                    // Movie not found
                    String response = "Movie not found.";
                    exchange.sendResponseHeaders(404, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while retrieving movie details: " + e.getMessage(), e);
                String response = "Internal server error.";
                exchange.sendResponseHeaders(500, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
        } else {
            // movieId not provided in query params
            String response = "movieId is required.";
            exchange.sendResponseHeaders(400, response.length());
            exchange.getResponseBody().write(response.getBytes());
        }

        exchange.getResponseBody().close();
    }
}
