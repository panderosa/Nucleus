/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.JsonArray;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import static javax.management.Query.value;


/**
 *
 * @author Administrator
 */
public class PostSubscription {
    
    final private List<String> options;
    private HashMap<String,String> arguments;
    private ExcelUtility excel;
    private HashMap<String,ArrayList<String>> input;
    
    public static void main(String[] args) throws Exception {
        PostSubscription ps = new PostSubscription(args);
        switch (ps.getOption()) {
            case "CreateTemplate":
                ps.createTemplate(ps.getRequest(),ps.getService(),ps.getSubscription());              
                break;
            case "OrderSubscription":
                System.out.println("OrderSubscription");
                break;
        }
        
        //ps.createTemplate(requestOrder, template);
    }
    
    public PostSubscription(String[] args) throws Exception {
        options = Arrays.asList("CreateTemplate","OrderSubscription");
        parseArguments(args);
        input = new HashMap<>();
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
    
    String getRequest() throws Exception {
        String file = arguments.get("request");
        return file;
    }
    
    String getService() throws Exception {
        String file = arguments.get("service");
        return file;
    }
    
    String getSubscription() throws Exception {
        String file = arguments.get("subscription");
        return file;
    }
    
    String getOutputFile() throws Exception {
        String file = arguments.get("output");
        if (file == null)
            howToUse(String.format("Provide output workbook name"));
        return file;
    }
    
    void createTemplate(String request, String service, String subscription) throws Exception {
        Map<String,String> meta = null;
        String out = getOutputFile();
        excel = new ExcelUtility(out);
        excel.addSheet("Metadata", 0);
        // Start from sheet 1
        int seq = 1;
        if ( request != null) {
            excel.addSheet("Request Order", seq);
            meta = extractFieldsFromRequest(request,seq);
            seq++;
        }
        if ( service != null) {
            excel.addSheet("Service Offering", seq);
            meta = extractFieldsFromService(service,seq);
            seq++;
        }
         if ( subscription != null) {
            excel.addSheet("Subscription Fields", seq);
            extractFieldsFromSubscription(subscription,seq);
            seq++;
        }       
        if ( request != null || service != null ) {
            int meta_sheet = 0;
            createMetaData(meta, meta_sheet); 
            excel.addSheet("Input Fields", seq);
            generateTemplate(seq);
        }
        excel.save();
    }
    
    
    void createMetaData(Map<String,String> meta, int seq) throws Exception {
        Set<String> keys = meta.keySet();
        int col = 0;
        for ( String key : keys) {
            excel.addHeader(seq, col, 0, key);
            excel.addContent(seq, col, 1, meta.get(key));
            col++;
        }
    }
    
    void updateInputFields(String name, String id) {
        if ( input.get(name) != null ) {
            ArrayList<String> attributes = input.get(name);
            attributes.add(id);
            input.put(name, attributes);
        }
    }
    
    void addToInputFields(String name, ArrayList<String> attrs) {
        input.put(name, attrs);
    }
    
    Map<String,String> extractFieldsFromRequest(String file, int seq) throws Exception {
        Map<String,String> meta = new HashMap<>();
        JsonObject order = readJsonObjectFromFile(file);

        // Meta data
        meta.put("Service Offering Id", order.getJsonObject("service").getString("id"));
        meta.put("Category Name", order.getJsonObject("category").getString("name"));
        meta.put("Display Name", order.getJsonObject("service").getString("displayName"));
        meta.put("Offering Version", order.getJsonObject("service").getString("offeringVersion"));
        
        // Add header
        excel.addHeader(seq, 0, 0, "Field Identifier");
        excel.addHeader(seq, 1, 0, "Field Name");
        excel.addHeader(seq, 2, 0, "Display Name");
        excel.addHeader(seq, 3, 0, "Field Value");
        JsonArray fields = order.getJsonArray("fields");
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            excel.addContent(seq, 0, r, id);
            String name = fld.getString("name");
            excel.addContent(seq, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            excel.addContent(seq, 2, r, descriptiveName);
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
            excel.addContent(seq, 3, r, value);
            // Build Order input
            ArrayList<String> attrs = new ArrayList();
            attrs.add(0,descriptiveName);
            attrs.add(1,value);
            addToInputFields(name,attrs);
            r++;
        };
        
        return meta;
    }
    
    Map<String,String> extractFieldsFromService(String file, int seq) throws Exception {
        Map<String,String> meta = new HashMap<>();
        JsonObject offering = readJsonObjectFromFile(file);
        // Meta data
        meta.put("Service Id", offering.getString("id"));
        meta.put("Category Name", offering.getJsonObject("category").getString("name"));
        meta.put("Display Name", offering.getString("displayName"));
        meta.put("Service Version", offering.getString("offeringVersion"));
        meta.put("Catalog Id", offering.getString("catalogId"));
        meta.put("Service Name", offering.getString("name"));
        
        // Add header
        excel.addHeader(seq, 0, 0, "Field Identifier");
        excel.addHeader(seq, 1, 0, "Field Name");
        excel.addHeader(seq, 2, 0, "Display Name");
        excel.addHeader(seq, 3, 0, "Field Value");
        JsonArray fields = offering.getJsonArray("fields");
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            excel.addContent(seq, 0, r, id);
            String name = fld.getString("name");
            excel.addContent(seq, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            excel.addContent(seq, 2, r, descriptiveName);
            JsonValue vl = fld.get("value");
            String value = "";
            if ( vl != null ) {
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
            }
            else 
                value = "";
            excel.addContent(seq, 3, r, value);
            updateInputFields(name,id);
            r++;
        };
        return meta;
    }
    
    void extractFieldsFromSubscription(String file, int seq) throws Exception {
        JsonArray fields = readJsonArrayFromFile(file);       
        // Add header
        excel.addHeader(seq, 0, 0, "Field Identifier");
        excel.addHeader(seq, 1, 0, "Field Name");
        excel.addHeader(seq, 2, 0, "Display Name");
        excel.addHeader(seq, 3, 0, "Field Value");
        
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            excel.addContent(seq, 0, r, id);
            String name = fld.getString("name");
            excel.addContent(seq, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            excel.addContent(seq, 2, r, descriptiveName);
            JsonValue vl = fld.get("value");
            String value = "";
            if ( vl != null ) {
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
            }
            else 
                value = "";
            excel.addContent(seq, 3, r, value);
            r++;
        };
    }
    
    void generateTemplate(int seq) throws Exception {
        Set<String> names = input.keySet();
        
        // Add header
        excel.addHeader(seq, 0, 0, "Field Identifier");
        excel.addHeader(seq, 1, 0, "Field Name");
        excel.addHeader(seq, 2, 0, "Display Name");
        excel.addHeader(seq, 3, 0, "Field Value");      
        
        int r = 1;
        for ( String name : names) {
            List<String> attributes = input.get(name);

            String dn = attributes.get(0);
            String value = attributes.get(1);
            String id = attributes.get(2);
          
            excel.addContent(seq, 0, r, id);
            excel.addContent(seq, 1, r, name);
            excel.addContent(seq, 2, r, dn);
            excel.addContent(seq, 3, r, value);
            r++;
        }
    }
    
            
    JsonObject readJsonObjectFromFile(String fileName) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        StringReader reader = new StringReader(content);
        JsonReader jreader = Json.createReader(reader);
        JsonObject job = jreader.readObject();
        return job;
    }
    
    JsonArray readJsonArrayFromFile(String fileName) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        StringReader reader = new StringReader(content);
        JsonReader jreader = Json.createReader(reader);
        JsonArray jar = jreader.readArray();
        return jar;
    }
    
    
    
}
