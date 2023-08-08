package ca.yorku.eecs.api.put;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;

import java.io.IOException;

import org.json.JSONObject;

/**
 * Handles the addition of movies to the Neo4j database via HTTP requests.
 * <p>
 * This handler checks if a movie with the given movieId already exists in the database.
 * If it does, a 400 status code is returned. If not, the movie is added and a 200 status code is returned.
 * </p>
 *
 * @since 2023-08-06
 */
public class AddMovieHandler implements HttpHandler {

	/**
	 * The Neo4j database driver instance used for database operations.
	 */
	private final Driver driver;

	/**
	 * Constructs a new AddMovieHandler with the provided Neo4j driver.
	 *
	 * @param driver The Neo4j driver instance.
	 */
	public AddMovieHandler(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Handles the HTTP request to add a movie to the database.
	 * <p>
	 * If the movie with the given movieId already exists, a 400 status code is returned.
	 * Otherwise, the movie is added and a 200 status code is returned.
	 * </p>
	 *
	 * @param exchange The HTTP exchange object containing request and response details.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String body = Utils.getBody(exchange);

		try (Session session = driver.session()) {
			JSONObject json = new JSONObject(body);
			String name = json.getString("name");
			String movieId = json.getString("movieId");

			try (Transaction tx = session.beginTransaction()) {
				StatementResult result = tx.run("MATCH (m:Movie {movieId: $movieId}) RETURN m", Values.parameters("movieId", movieId));

				if (result.hasNext()) {
					String response = "Movie with given movieId already exists.";
					exchange.sendResponseHeaders(400, response.length());
					exchange.getResponseBody().write(response.getBytes());
				} else {
					tx.run("CREATE (m:Movie {name: $name, movieId: $movieId})", Values.parameters("name", name, "movieId", movieId));

					String response = "Movie added successfully.";
					exchange.sendResponseHeaders(200, response.length());
					exchange.getResponseBody().write(response.getBytes());
				}

				tx.success();
			}
		} catch (Exception e) {
			String response = "Internal server error.";
			exchange.sendResponseHeaders(500, response.length());
			exchange.getResponseBody().write(response.getBytes());
		} finally {
			exchange.getResponseBody().close();
		}
	}
}
