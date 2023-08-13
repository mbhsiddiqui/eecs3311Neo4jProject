package ca.yorku.eecs.handler.get;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveDataHandler implements HttpHandler {

    private final Driver driver;
    private static final Logger logger = Logger.getLogger(RetrieveDataHandler.class.getName());

    public RetrieveDataHandler(Driver driver) {
        this.driver = driver;
    }

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
