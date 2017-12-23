/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.JsonArray;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;


/**
 *
 * @author Administrator
 */
public class PostSubscription {
    
    final private List<String> options;
    HashMap<String,String> arguments; 
    
    public static void main(String[] args) throws Exception {
        PostSubscription ps = new PostSubscription(args);
        switch (ps.getOption()) {
            case "CreateTemplate":
                if ( ps.getInputFile() != null) {
                    ps.createTemplateFromOrder(ps.getInputFile(),ps.getOutputFile());
                    System.out.println("CreateTemplate");
                }
                break;
            case "OrderSubscription":
                System.out.println("CreateTemplate");
                break;
        }
        
        //ps.createTemplate(requestOrder, template);
    }
    
    public PostSubscription(String[] args) throws Exception {
        options = Arrays.asList("CreateTemplate","OrderSubscription");
        parseArguments(args);
    }
    
    void parseArguments(String[] args) throws Exception {
        arguments = new HashMap<>();
        String errorMessage = "";
        for ( int i = 0; i < args.length ; i++ ) {
            int j = args[i].indexOf('=');
            if ( j < 1 ) { 
                errorMessage = errorMessage.concat(String.format("%nIncorrect argument format: %s", args[i]));
            }
            else {
                String name = args[i].substring(0, j);
                String value = args[i].substring(j+1);
                arguments.put(name, value);
            }
        }
        if ( errorMessage.length() > 0 ) throw new RuntimeException(errorMessage);
    }
    
    void howToUse(String errorMessage) throws Exception {
        throw new RuntimeException(errorMessage);
    }
    
    
    String getOption() throws Exception {
        String opt = arguments.get("option");
        if (opt == null)
            howToUse("Argument \"option\" is null");
        else if (!options.contains(opt))
            howToUse(String.format("Incorrect option used \"%1$s\"", opt));
        return opt;
    }
    
    String getInputFile() throws Exception {
        String file = arguments.get("inputFile");
        return file;
    }
    
    String getOutputFile() throws Exception {
        String file = arguments.get("outputFile");
        if (file == null)
            howToUse(String.format("Output file name is empty"));
        return file;
    }
    
    
    void createTemplateFromOrder(String file, String template) throws Exception {
        JsonObject order = readJsonFile(file);
        ExcelUtility excel = new ExcelUtility(template);
        excel.addSheet("order", 0);
        String categoryName = order.getJsonObject("category").getString("name");
        String offeringId = order.getJsonObject("service").getString("id");
        String offeringVersion = order.getJsonObject("service").getString("offeringVersion");
        String displayName = order.getJsonObject("service").getString("displayName");
        JsonArray fields = order.getJsonArray("fields");
        Iterator<JsonValue> fi = fields.iterator();
        System.out.println("No of fields: " + fields.size());
        int r = 0;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            excel.addContent(0, 0, r, id);
            String name = fld.getString("name");
            excel.addContent(0, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            excel.addContent(0, 2, r, descriptiveName);
            JsonValue vl = fld.get("value");
            String value = "";
            if (vl.getValueType() == JsonValue.ValueType.STRING) 
                value = String.format("\"%s\"", ((JsonString) vl).getString());
            else if (vl.getValueType() == JsonValue.ValueType.NUMBER)
                value = String.format("%s", ((JsonNumber) vl).toString());
            else if (vl.getValueType() == JsonValue.ValueType.TRUE)
                value = "true";
            else if (vl.getValueType() == JsonValue.ValueType.FALSE)
                value = "false";
            else if (vl.getValueType() == JsonValue.ValueType.NULL)
                value = ""; 
            excel.addContent(0, 3, r, value);
            r++;
        };
        excel.save();
    }
    
    void createTemplateFromOoffering() {
        
    }
            
    JsonObject readJsonFile(String fileName) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        StringReader reader = new StringReader(content);
        JsonReader jreader = Json.createReader(reader);
        JsonObject job = jreader.readObject(); 
        return job;
    }
    
    
    
}
