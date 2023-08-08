package ca.yorku.eecs.api.put;

import ca.yorku.eecs.utils.Utils;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;
import java.io.IOException;
import org.json.JSONObject;

public class AddActorHandler implements HttpHandler {
    private final Driver driver;

    public AddActorHandler(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Read the request body
        String body = Utils.getBody(exchange);

        try (Session session = driver.session()) {
            // Parse the JSON body
            JSONObject json = new JSONObject(body);
            String name = json.getString("name");
            String actorId = json.getString("actorId");

            // Start a transaction
            try (Transaction tx = session.beginTransaction()) {
                // Check if the actorId already exists
                StatementResult result = tx.run("MATCH (a:Actor {actorId: $actorId}) RETURN a", Values.parameters("actorId", actorId));

                if (result.hasNext()) {
                    // Actor with given actorId already exists
                    String response = "Actor with given actorId already exists.";
                    exchange.sendResponseHeaders(400, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                } else {
                    // Create new actor
                    tx.run("CREATE (a:Actor {name: $name, actorId: $actorId})", Values.parameters("name", name, "actorId", actorId));

                    String response = "Actor added successfully.";
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                }

                // Commit the transaction
                tx.success();
            }
        } catch (Exception e) {
            // Exception occurred
            String response = "Internal server error.";
            exchange.sendResponseHeaders(500, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } finally {
            exchange.getResponseBody().close();
        }
    }
}