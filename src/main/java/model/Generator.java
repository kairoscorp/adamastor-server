package model;

import bayou.bytes.ByteSource;
import bayou.bytes.ByteSource2InputStream;
import bayou.bytes.InputStream2ByteSource;
import bayou.mime.MultipartParser;
import bayou.mime.MultipartPart;
import bayou.util.End;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serializer.Serializer;
import server.Main;
import spark.Request;
import spark.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

public class Generator {
    private static final JsonParser parser = new JsonParser();
    private static Generator instance;
    private SAXBuilder builder;
    private static Logger logger = LoggerFactory.getLogger(Generator.class);


    private Generator(){
        this.builder = new SAXBuilder();
    }

    public static Generator getInstace(){
        if(instance == null) {
            instance = new Generator();
        }
        return instance;
    }

    public byte[] registerSettings(String requestBody) throws GeneratorException {
        // Parse request
        JsonObject requestJson = parser.parse(requestBody).getAsJsonObject();

        // Validate request
        String [] fields = {
                "working_hours_start",
                "working_hours_end",
        };

        validateRequest(requestJson, fields);

        // Get config file from resources folder
        String folderPath = "src/main/resources/";
        File configFile = new File(folderPath + "config.xml");
        File customConfigFile = new File(folderPath + "custom_config.xml");
        File datasetGeneratorScript = new File(folderPath + "dataset_generator.py");
        File datasetFile = new File(folderPath + "data_set.csv");
        File modelGeneratorScript = new File(folderPath + "model_generator.R");
        File modelFile = new File(folderPath + "model.pmml");
        File serializedModelFile = new File(folderPath + "serialized_model.txt");

        // Create config file
        createConfigFile(requestJson, configFile, customConfigFile);

        // Generate dataset
        generateDataSet(datasetGeneratorScript, customConfigFile, datasetFile);

        // Generate model
        generateModel(modelGeneratorScript, datasetFile, modelFile);

        // Serialize data from model file
        Serializer.serialize(modelFile, serializedModelFile);

        byte[] data;
        try {
            data = Files.readAllBytes(Paths.get(serializedModelFile.getAbsolutePath()));
        } catch (IOException e) {
            throw new GeneratorException("An unexpected error has occurred");
        }

        return data;
    }

    public byte[] registerData(Request request) throws GeneratorException, Exception {
        byte [] data = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(request.body().getBytes("ISO-8859-1"));
            ByteSource byteSource = new InputStream2ByteSource(is, 1024);
            MultipartParser multipartParser = new MultipartParser(byteSource, "boundary");
            File csvFile = File.createTempFile("csvfile", ".tmp");
            File locationsFile = File.createTempFile("locations", ".tmp");
            Map<String, File> files = new TreeMap<>();
            files.put("csvfile", csvFile);
            files.put("locations", locationsFile);

            while (true) {
                try {
                    MultipartPart part = multipartParser.getNextPart().sync();   // async -> sync
                    ByteSource body = part.body();
                    String name = part.headers().get("name");
                    InputStream inputStream = new ByteSource2InputStream(body, Duration.ofSeconds(1));
                    OutputStream outputStream = new FileOutputStream(files.get(name));

                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                } catch (End end) {
                    logger.debug("Register data file uploaded.");
                    break;
                }
            }

            // Get config file from resources folder
            String folderPath = "src/main/resources/";
            File modelFile = new File(folderPath + "model.pmml");
            File generateModelFile = new File(folderPath + "clean_generate_model.R");
            File serializedModelFile = new File(folderPath + "serialized_model.txt");

            String [] command = {"Rscript", generateModelFile.getAbsolutePath(), csvFile.getAbsolutePath(), locationsFile.getAbsolutePath(), modelFile.getAbsolutePath()};
            executeCommand(command);

            // Serialize data from model file
            Serializer.serialize(modelFile, serializedModelFile);

            try {
                data = Files.readAllBytes(Paths.get(serializedModelFile.getAbsolutePath()));
            } catch (IOException e) {
                throw new GeneratorException("An unexpected error has occurred");
            }
        } catch (UnsupportedEncodingException e) {
            throw new GeneratorException("An unexpected error has occurred");
        }


        return data;
    }

    private void generateDataSet(File datasetGeneratorScript, File customConfig, File datasetFile ) throws GeneratorException {
        // Generate dataset
        String[] commands = {"python", datasetGeneratorScript.getAbsolutePath(), customConfig.getAbsolutePath(), datasetFile.getAbsolutePath()};
        executeCommand(commands);
    }

    private void createConfigFile(JsonObject requestJson, File config, File customConfig) throws GeneratorException {
        Integer workingHoursStart = Math.round(requestJson.get("working_hours_start").getAsFloat());
        Integer workingHoursEnd = Math.round(requestJson.get("working_hours_end").getAsFloat());


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
            Document document = builder.build(config);
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
            xmlOutput.output(document, new FileWriter(customConfig));
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

    private void generateModel(File modelGeneratorScript, File datasetFile, File modelFile) throws GeneratorException {
        // Generate model
        String[] commands = {"Rscript", modelGeneratorScript.getAbsolutePath(), datasetFile.getAbsolutePath(), modelFile.getAbsolutePath()};
        executeCommand(commands);
    }

    private void saveDataSet(String dataset, File modelFile) {
        //
    }

    private void executeCommand(String [] commands) throws GeneratorException {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commands);
            int returnValue = proc.waitFor();
            if(returnValue != 0) {
                printErrors(proc);
                throw new InterruptedException();
            }
        } catch (IOException | InterruptedException e) {
            throw new GeneratorException("An unexpected error has occurred");
        }
    }

    private void printErrors(Process proc) {
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        try {
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
