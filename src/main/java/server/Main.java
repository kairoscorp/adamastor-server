package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.Weka;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static spark.Spark.*;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static int port = 4567;
    private static Weka modelGenerator = Weka.getInstace();
    // private static int maxThreads = 8;

    public static void main(String[] args) {
        port(port);
        // threadPool(maxThreads);
        logger.info("Kairos Server Server starting at port: "+ port);

        // Authentication
        before("/*", (request, response) -> {
            boolean authenticated = true;
            if (! authenticated) {
                logger.debug("Someone tried to make a request without permissions");
                throw halt(401, "You shall not pass!");
            }
        });

        // Create resources
        // When on boarding is done the settings should be registered in the browser
        post("/settings", (request, response) -> {
            logger.debug("Received a request to set user settings. Request: "+request.body());
            JsonObject result = new JsonObject();
            try {
                result = modelGenerator.registerSettings(request.body());
            } catch (JsonParseException je) {
                // Invalid json request
                logger.debug("Malformed Json Request.");
                response.status(HTTP_BAD_REQUEST);
                result.addProperty("error", je.getMessage());
            }
            return result;

        });

        // Data should be sent to the data in a window of opportunity
        post("/data", (request, response) -> {
            logger.debug("Received a request with new data from the collector. Request: "+request.body());
            JsonObject result = new JsonObject();
            try {
                result = modelGenerator.registerData(request.body());
            } catch (JsonParseException je) {
                // Invalid json request
                logger.debug("Malformed Json Request.");
                response.status(HTTP_BAD_REQUEST);
                result.addProperty("error", je.getMessage());
            }
            return result;
        });
    }
}
