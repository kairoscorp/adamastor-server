package weka;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Weka {
    private static final JsonParser parser = new JsonParser();
    private static Weka instance;

    private void Weka (){
        //
    }

    public static Weka getInstace(){
        if(instance == null) {
            instance = new Weka();
        }
        return instance;
    }

    public JsonObject registerSettings(String requestBody) {
        // Parse request
        JsonObject requestJson = parser.parse(requestBody).getAsJsonObject();
        String userId = requestJson.get("user_id").getAsString();
        String workingHours = requestJson.get("working_hours").getAsString();
        String homeHours = requestJson.get("home_hours").getAsString();

        // Parse more parameters

        // Generate a model

        // Create a response
        JsonObject response = new JsonObject();
        response.add("generate_model", null);
        return response;
    }

    public JsonObject registerData(String requestBody) {
        // Parse request
        JsonObject requestJson = parser.parse(requestBody).getAsJsonObject();
        String userId = requestJson.get("user_id").getAsString();
        String workingHours = requestJson.get("working_hours").getAsString();
        String homeHours = requestJson.get("home_hours").getAsString();
        JsonElement data = requestJson.get("data");

        // Save data

        // Create a response
        JsonObject response = new JsonObject();
        response.add("generate_model", null);
        return response;
    }
}
