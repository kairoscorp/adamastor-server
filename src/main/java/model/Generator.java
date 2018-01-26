package model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Generator {
    private static final JsonParser parser = new JsonParser();
    private static Generator instance;
    private SAXBuilder builder;

    private Generator(){
        this.builder = new SAXBuilder();
    }

    public static Generator getInstace(){
        if(instance == null) {
            instance = new Generator();
        }
        return instance;
    }

    public JsonObject registerSettings(String requestBody) throws GeneratorException {
        // Parse request
        JsonObject requestJson = parser.parse(requestBody).getAsJsonObject();

        // Validate request
        String [] fields = {
                "working_hours_start",
                "working_hours_end",
                "working_location_longitude",
                "working_location_latitude",
                "leisure_location_longitude",
                "leisure_location_latitude",
        };

        validateRequest(requestJson, fields);

        // Get config file from resources folder
        String folderPath = "src/main/resources/";
        File inputFile = new File(folderPath + "config.xml");
        File outputFile = new File(folderPath + "custom_config.xml");
        File scriptFile = new File(folderPath + "generator.py");
        File modelFile = new File(folderPath + "data_set.csv");


        // Create config file
        createConfigFile(requestJson, inputFile, outputFile);

        // Generate dataset
        generateDataSet(scriptFile, outputFile, modelFile);

        // Generate model

        // Create a response
        JsonObject response = new JsonObject();
        response.add("generate_model", null);
        return response;
    }

    public JsonObject registerData(String requestBody) throws GeneratorException {
        // Parse request
        JsonObject requestJson = parser.parse(requestBody).getAsJsonObject();

        // Validate request
        String [] fields = {
                "working_hours_start",
                "working_hours_end",
                "working_location_longitude",
                "working_location_latitude",
                "leisure_location_longitude",
                "leisure_location_latitude",
                "data"
        };

        validateRequest(requestJson, fields);

        // Get data
        String dataset = null;

        // Get config file from resources folder
        String folderPath = "src/main/resources/";
        File inputFile = new File(folderPath + "config.xml");
        File outputFile = new File(folderPath + "custom_config.xml");
        File modelFile = new File(folderPath + "data_set.csv");

        // Create config file
        createConfigFile(requestJson, inputFile, outputFile);

        // Generate dataset
        saveDataSet(dataset, modelFile);

        // Generate model


        // Create a response
        JsonObject response = new JsonObject();
        response.add("generate_model", null);
        return response;
    }

    public static void main(String[] args) {
        Generator weka = Generator.getInstace();
        JsonObject request = new JsonObject();
        request.addProperty("working_hours_start", 8);
        request.addProperty("working_hours_end", 18);
        request.addProperty("working_location_longitude", 41.56131);
        request.addProperty("working_location_latitude", -8.393804);
        request.addProperty("leisure_location_longitude", 32.56131);
        request.addProperty("leisure_location_latitude", -12.323804);

        try {
            weka.registerSettings(request.toString());
        } catch (GeneratorException e) {
            e.printStackTrace();
        }
    }

    private void generateDataSet(File scriptFile, File outputFile, File modelFile ) throws GeneratorException {
        // Generate dataset
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"python",scriptFile.getAbsolutePath(), outputFile.getAbsolutePath(), modelFile.getAbsolutePath()};
        try {
            Process proc = rt.exec(commands);
            int returnValue = proc.waitFor();
            if(returnValue != 0) {
                throw new InterruptedException();
            }
        } catch (IOException | InterruptedException e) {
            throw new GeneratorException("An unexpected error has occurred");
        }
    }

    private void createConfigFile(JsonObject requestJson, File inputFile, File outputFile) throws GeneratorException {
        Integer workingHoursStart = Math.round(requestJson.get("working_hours_start").getAsFloat());
        Integer workingHoursEnd = Math.round(requestJson.get("working_hours_end").getAsFloat());
        Double workingLocationLongitude = requestJson.get("working_location_longitude").getAsDouble();
        Double workingLocationLatitude = requestJson.get("working_location_latitude").getAsDouble();
        Double leisureLocationLongitude = requestJson.get("leisure_location_longitude").getAsDouble();
        Double leisureLocationLatitude = requestJson.get("leisure_location_latitude").getAsDouble();


        Integer workingHoursStartMorning = workingHoursStart;
        Integer workingHourEndMorning = workingHoursStart + 4;
        Integer workingHoursLunchStart = workingHourEndMorning;
        Integer workingHoursLuchEnd = workingHoursLunchStart + 2;
        Integer workingHoursStartAfternoon = workingHoursLuchEnd;
        Integer workingHoursEndAfternoon = workingHoursEnd;

        Integer leisureHoursStartMorning = workingHoursStart - 3;
        Integer leisureHourEndMorning = leisureHoursStartMorning + 2;
        Integer leisureHoursStartAfternoon = workingHoursEndAfternoon;
        Integer leisureHoursEndAfternoon = leisureHoursStartAfternoon + 6;
        Integer leisureHoursDinnerStart = leisureHoursStartAfternoon + 2;
        Integer leisureHoursDinnerEnd = leisureHoursDinnerStart + 1;

        // Change config file
        try {
            Document document = builder.build(inputFile);
            Element rootNode = document.getRootElement();

            // Change work settings
            Element workSettings = rootNode.getChild("work");
            workSettings.getChild("work_start_morning").setText(workingHoursStartMorning.toString());
            workSettings.getChild("work_end_morning").setText(workingHourEndMorning.toString());
            workSettings.getChild("work_start_lunch").setText(workingHoursLunchStart.toString());
            workSettings.getChild("work_end_lunch").setText(workingHoursLuchEnd.toString());
            workSettings.getChild("work_start_afternoon").setText(workingHoursStartAfternoon.toString());
            workSettings.getChild("work_end_afternoon").setText(workingHoursEndAfternoon.toString());

            // Change home settings
            Element homeSettings = rootNode.getChild("leisure");
            homeSettings.getChild("leisure_start_morning").setText(leisureHoursStartMorning.toString());
            homeSettings.getChild("leisure_end_morning").setText(leisureHourEndMorning.toString());
            homeSettings.getChild("leisure_start_afternoon").setText(leisureHoursStartAfternoon.toString());
            homeSettings.getChild("leisure_end_afternoon").setText(leisureHoursEndAfternoon.toString());
            homeSettings.getChild("leisure_start_dinner").setText(leisureHoursDinnerStart.toString());
            homeSettings.getChild("leisure_end_dinner").setText(leisureHoursDinnerEnd.toString());

            // XML Writer
            XMLOutputter xmlOutput = new XMLOutputter();

            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter(outputFile));
        } catch (IOException | JDOMException e) {
            throw new GeneratorException("An unexpected error has occurred");
        }
    }

    private void validateRequest(JsonObject requestJson, String [] fields) throws GeneratorException {
        for(String field : fields) {
            if(! requestJson.has(field)) {
                throw new GeneratorException("Missing field: "+field);
            }
        }
    }


    private void saveDataSet(String dataset, File modelFile) {
        //
    }
}
