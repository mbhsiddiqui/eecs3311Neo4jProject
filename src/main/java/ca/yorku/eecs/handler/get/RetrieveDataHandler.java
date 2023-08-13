package ca.yorku.eecs.handler.get;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the HTTP request to retrieve data from the Neo4j database.
 * <p>
 * This handler executes a Cypher query to retrieve actor names and IDs from the database,
 * and then prints the retrieved data to the console. It also sends a response to the client
 * indicating the successful retrieval and printing of data.
 * </p>
 * <p>
 * Implements the HttpHandler interface to handle the HTTP request and response.
 * </p>
 *
 * @since 2023-08-06
 */
public class RetrieveDataHandler implements HttpHandler {

    private final Driver driver;
    private static final Logger logger = Logger.getLogger(RetrieveDataHandler.class.getName());

    /**
     * Constructs a new RetrieveDataHandler with the provided Neo4j driver.
     *
     * @param driver The Neo4j driver instance.
     */
    public RetrieveDataHandler(Driver driver) {
        this.driver = driver;
    }

    /**
     * Handles the HTTP request to retrieve data from the database.
     * <p>
     * Executes a Cypher query to retrieve actor names and IDs, prints the data to the console,
     * and sends a response indicating success.
     * </p>
     *
     * @param exchange The HTTP exchange object containing request and response details.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Received request to retrieve data");

        try (Session session = driver.session()) {
            // Query to retrieve data
            String cypherQuery = "MATCH (a:Actor) RETURN a.name, a.actorId";

            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run(cypherQuery);

                // Prepare the data for printing
                StringBuilder data = new StringBuilder();
                while (result.hasNext()) {
                    Record record = result.next();
                    String name = record.get("a.name").asString();
                    String actorId = record.get("a.actorId").asString();
                    data.append("Actor Name: ").append(name).append(", Actor ID: ").append(actorId).append("\n");
                }

                // Print the data to console
                System.out.println(data.toString());

                // Send a response indicating success
                String response = "Database content printed to console.";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

                tx.success();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Internal server error: " + e.getMessage(), e);
            String response = "Internal server error.";
            exchange.sendResponseHeaders(500, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
