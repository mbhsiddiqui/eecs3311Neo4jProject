/**
 * The App class is the main entry point for the application. It sets up an HTTP server, initializes a Neo4j database driver,
 * and creates handlers for various API endpoints.
 */
package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.yorku.eecs.api.get.*;
import ca.yorku.eecs.api.put.AddActorHandler;
import ca.yorku.eecs.api.put.AddMovieHandler;
import ca.yorku.eecs.api.put.AddRelationshipHandler;
import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class App {

	/**
	 * Constant representing the server port.
	 */
	private static final int PORT = 8080;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(App.class.getName());

	/**
	 * Main method for setting up the HTTP server, initializing the Neo4j driver, and setting up API endpoints.
	 * In case of any IOException, it logs the error and terminates the application.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		try {
			// Create the HTTP server
			HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

			// Initialize Neo4j driver
			Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"), Config.build().withoutEncryption().toConfig());

			// Create context for each API endpoint with corresponding handlers
			server.createContext("/api/v1/addActor", new AddActorHandler(driver));
			server.createContext("/api/v1/addMovie", new AddMovieHandler(driver));
			server.createContext("/api/v1/addRelationship", new AddRelationshipHandler(driver));
			server.createContext("/api/v1/getActor", new GetActorHandler(driver));
			server.createContext("/api/v1/getMovie", new GetMovieHandler(driver));
			server.createContext("/api/v1/hasRelationship", new HasRelationshipHandler(driver));
			server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumberHandler(driver));
			server.createContext("/api/v1/computeBaconPath", new ComputeBaconPathHandler(driver));

			// Use a thread pool executor for the server
			server.setExecutor(Executors.newCachedThreadPool());

			// Start the server
			server.start();

			logger.info(String.format("Server started on port %d...\n", PORT));
		} catch (IOException e) {
			// Log the error and exit the application
			logger.log(Level.SEVERE, "Error starting the server: " + e.getMessage(), e);
			System.exit(1);
		}
	}
}
