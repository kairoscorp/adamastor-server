package serializer;

import org.dmg.pmml.PMML;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.SerializationUtil;
import org.jpmml.model.visitors.StringInterner;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import java.io.*;

public class Serializer {

    public static void serialize(File pmmlFile, File serFile) {
        try{
            InputStream is = new FileInputStream(pmmlFile);

            Source source = ImportFilter.apply(new InputSource(is));

            PMML pmml = JAXBUtil.unmarshalPMML(source);

            StringInterner si = new StringInterner();

            si.applyTo(pmml);

            OutputStream os = new FileOutputStream(serFile);

            SerializationUtil.serializePMML(pmml, os);

        }catch(Exception e){
            System.out.println("Error transforming file");
            e.printStackTrace();
        }
    }
}
