/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import java.io.StringReader;
import java.io.StringWriter;
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
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;



/**
 *
 * @author Administrator
 */
public class PostSubscription {
    
    final private List<String> options;
    private HashMap<String,String> arguments;
    private WriteExcel wexcel;
    private ReadExcel rexcel;
    private HashMap<String,String> input;
    private Map<String,String> meta;
    private ArrayList<String> dic;
    private String csaServer;
    private int csaPort;
    private String csaProtocol;
    private String idmUser;
    private String idmPassword;
    private String csaConsumer;
    private String csaConsumerPassword;
    private String csaTenant;
    private String onBehalfOfAnotherUser;
    private Net net;
    
    public static void main(String[] args) throws Exception {
        PostSubscription ps = new PostSubscription(args);
        switch (ps.getOption()) {
            case "CreateTemplateFromFiles":
                ps.createTemplateFromFiles();              
                break;
            case "Order":
                String jos = ps.buildJsonForOrder();
                System.out.print(jos);
                break;
            case "ViewSubscription":
                ps.connectToCSA();
                ps.viewSubscription();
                break;
            default:
                System.out.println("Option not supported");
        }
        
        //ps.createTemplate(requestOrder, template);
    }
    
    public PostSubscription(String[] args) throws Exception {
        options = Arrays.asList("CreateTemplateFromFiles","Order","ViewSubscription");
        parseArguments(args);
        input = new HashMap<>();
        initDictionary();
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
    
     void connectToCSA() throws Exception {
        String env = arguments.get("env");
        if (env == null)
            raiseError("Argument \"env\" is null");        
        String host = arguments.get("host");
        String consumer = arguments.get("consumer");
        String tenant = arguments.get("tenant");
        String onBehalf = arguments.get("onBehalf");
        setCSAParams(env, host, consumer, tenant, onBehalf);
        net = new Net(this.csaServer,this.csaPort,this.csaProtocol);
        net.requestToken(this.idmUser, this.idmPassword, this.csaConsumer, this.csaConsumerPassword, this.csaTenant);
        
    }
     
    void viewSubscription() throws Exception {
        String uuid = arguments.get("uuid");
        if (uuid == null)
            raiseError("Argument subscription \"uuid\" is null");
        String token = net.getToken();
        String uri = "/csa/api/mpp/mpp-subscription/" + uuid;
        String out = net.getHttp(token, null, null, uri, "application/json");
        System.out.println(out);
    } 
    
    void initDictionary() {
        dic = new ArrayList();
        dic.add(0,"Metadata");
        dic.add(1,"Service Offering");
        dic.add(2,"Subscription Fields");
        dic.add(3,"Request Order");
        dic.add(4,"Input Fields");
        dic.add(5,"Field Identifier");
        dic.add(6,"Field Name");
        dic.add(7,"Display Name");
        dic.add(8,"Field Value");
        dic.add(9,"serviceId");
        dic.add(10,"categoryName");
        dic.add(11,"displayName");
        dic.add(12,"offeringVersion");
        dic.add(13,"catalogId");
        dic.add(14,"serviceName");
        dic.add(15,"subscriptionName");
        dic.add(16,"subscriptionDescription");
        dic.add(17,"startDate");
        dic.add(18,"ORDER");
    }
    
    private void setCSAParams(String file, String csaHost, String consumer, String tenant, String onBehalf) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(file)));
        StringReader reader = new StringReader(content);
        JsonReader jreader = Json.createReader(reader);
        JsonObject cnf = jreader.readObject();
        idmUser = cnf.getJsonArray("idmUser").getString(0);
        idmPassword = cnf.getJsonArray("idmUser").getString(1);
        csaConsumer = (consumer == null)? cnf.getString("defaultConsumer"): consumer;
        csaConsumerPassword = cnf.getString(this.csaConsumer);
        csaTenant = (tenant == null)? cnf.getString("defaultTenant"): tenant;
        csaServer = (csaHost == null)? cnf.getJsonObject("csaAS").getString("Server"): csaHost;
        csaPort = cnf.getJsonObject("csaAS").getInt("Port");
        csaProtocol = cnf.getJsonObject("csaAS").getString("Protocol");
        onBehalfOfAnotherUser = onBehalf;
    }
    
    void raiseError(String errorMessage) throws Exception {
        throw new RuntimeException(errorMessage);
    }
    
    
    String getOption() throws Exception {
        String opt = arguments.get("option");
        if (opt == null)
            raiseError("Argument \"option\" is null");
        else if (!options.contains(opt))
            raiseError(String.format("Incorrect option used \"%1$s\"", opt));
        return opt;
    }
    
    String getRequest() throws Exception {
        String file = arguments.get("request");
        if ( file == null)
            raiseError("Argument \"request\" is null.");
        return file;
    }
    
    String getService() throws Exception {
        String file = arguments.get("service");
        if ( file == null)
            raiseError("Argument \"service\" is null.");
        return file;
    }
    
    String getSubscription() throws Exception {
        String file = arguments.get("subscription");
        return file;
    }
    
    
    String getWorkbook() throws Exception {
        String file = arguments.get("workbook");
        if ( file == null)
            raiseError("Argument \"workbook\" is null.");
        return file;
    }
    
    void createTemplateFromFiles() throws Exception {
        String out = getWorkbook();       
        wexcel = new WriteExcel(out);
        // Start from sheet 1
        wexcel.addSheet(dic.get(1), 0);
        addServiceFieldsToSheet(0); 
        wexcel.addSheet(dic.get(3), 1);
        addRequestOrderFieldsToSheet(1);      
        wexcel.addSheet(dic.get(0), 2);
        addMetaDataFieldsToSheet(2); 
        wexcel.addSheet(dic.get(4), 3);
        addTemplateFieldsToSheet(3);        
        wexcel.save();
    }
    
    
    void addMetaDataFieldsToSheet(int seq) throws Exception {
        Set<String> keys = meta.keySet();
        int row = 0;
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        
        for ( String key : keys) {
            wexcel.addHeader(seq, 0, row, key);
            wexcel.addContent(seq, 1, row, meta.get(key));
            row++;
        }
    }
    
    void addFieldId(String name, String id) {
        input.put(name, id);        
    }
    
    String getFieldId(String name) {
        return input.get(name);
    }
    
    void addRequestOrderFieldsToSheet(int seq) throws Exception {
        JsonObject order = readJsonObjectFromFile(getRequest());
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);        
        wexcel.addHeader(seq, 0, 0, dic.get(5));
        wexcel.addHeader(seq, 1, 0, dic.get(6));
        wexcel.addHeader(seq, 2, 0, dic.get(7));
        wexcel.addHeader(seq, 3, 0, dic.get(8));
        JsonArray fields = order.getJsonArray("fields");
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            wexcel.addContent(seq, 0, r, id);
            String name = fld.getString("name");
            wexcel.addContent(seq, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            wexcel.addContent(seq, 2, r, descriptiveName);
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
            wexcel.addContent(seq, 3, r, value);
            r++;
        };
    }
    
    void initMetaData(JsonObject offering) {
        meta = new HashMap<>();
        meta.put(dic.get(9), String.format("\"%s\"", offering.getString("id")));
        meta.put(dic.get(10), String.format("\"%s\"", offering.getJsonObject("category").getString("name")));
        meta.put(dic.get(11), String.format("\"%s\"", offering.getString("displayName")));
        meta.put(dic.get(12), String.format("\"%s\"", offering.getString("offeringVersion")));
        meta.put(dic.get(13), String.format("\"%s\"", offering.getString("catalogId")));
        meta.put(dic.get(14), String.format("\"%s\"", offering.getString("name")));
        meta.put(dic.get(15), "");
        meta.put(dic.get(16), "");
        meta.put(dic.get(17), "");
    }
    
    void addServiceFieldsToSheet(int seq) throws Exception {
        JsonObject offering = readJsonObjectFromFile(getService());
        // Meta data
        initMetaData(offering);           
      
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);  
        wexcel.addHeader(seq, 0, 0, dic.get(5));
        wexcel.addHeader(seq, 1, 0, dic.get(6));
        wexcel.addHeader(seq, 2, 0, dic.get(7));
        wexcel.addHeader(seq, 3, 0, dic.get(8));
        JsonArray fields = offering.getJsonArray("fields");
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            wexcel.addContent(seq, 0, r, id);
            String name = fld.getString("name");
            wexcel.addContent(seq, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            wexcel.addContent(seq, 2, r, descriptiveName);
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
            wexcel.addContent(seq, 3, r, value);
            addFieldId(name,id);
            r++;
        };
    }
    
    void extractFieldsFromSubscription(int seq) throws Exception {
        JsonArray fields = readJsonArrayFromFile(getSubscription());       
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);  
        wexcel.addHeader(seq, 0, 0, dic.get(5));
        wexcel.addHeader(seq, 1, 0, dic.get(6));
        wexcel.addHeader(seq, 2, 0, dic.get(7));
        wexcel.addHeader(seq, 3, 0, dic.get(8));
        
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            String id = fld.getString("id");
            wexcel.addContent(seq, 0, r, id);
            String name = fld.getString("name");
            wexcel.addContent(seq, 1, r, name);
            String descriptiveName = fld.getString("displayName");
            wexcel.addContent(seq, 2, r, descriptiveName);
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
            wexcel.addContent(seq, 3, r, value);
            r++;
        };
    }
    
    void addTemplateFieldsToSheet(int seq) throws Exception {
        JsonObject order = readJsonObjectFromFile(getRequest());
        
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);  
        wexcel.addHeader(seq, 0, 0, dic.get(5));
        wexcel.addHeader(seq, 1, 0, dic.get(6));
        wexcel.addHeader(seq, 2, 0, dic.get(7));
        wexcel.addHeader(seq, 3, 0, dic.get(8));
        JsonArray fields = order.getJsonArray("fields");
        Iterator<JsonValue> fi = fields.iterator();
        // Data start from row 1
        int r = 1;
        while (fi.hasNext()) {
            JsonValue jv = fi.next();
            JsonObject fld = jv.asJsonObject();
            
            String name = fld.getString("name");
            wexcel.addContent(seq, 1, r, name);
            String id = input.get(name);
            wexcel.addContent(seq, 0, r, id);
            String descriptiveName = fld.getString("displayName");
            wexcel.addContent(seq, 2, r, descriptiveName);
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
            wexcel.addContent(seq, 3, r, value);
            r++;
        }
    }
    
    Map<String,String> parseMetaData() throws Exception {
        String out = getWorkbook();
        rexcel = new ReadExcel(out);
        rexcel.setSheet("Metadata", 0);
        String[] params = rexcel.readColumn(0);
        String[] paramValues = rexcel.readColumn(1);
        Map<String,String> meta = new HashMap<>();
        for ( int j = 0; j < params.length; j++ ) {
            meta.put(params[j], paramValues[j]);
        }
        return meta;
    }
    
    
    String buildJsonForOrder() throws Exception {
        Map<String,String> meta = parseMetaData();     
        
        // Format JSON Body
        StringBuilder sb = new StringBuilder().append("{\n")
                .append(String.format("\"action\": %s%n", dic.get(18)))
                .append(String.format("\"%s\": %s,%n", dic.get(10), meta.get(dic.get(10))))
                .append(String.format("\"%s\": %s,%n", dic.get(15), meta.get(dic.get(15))))
                .append(String.format("\"%s\": %s,%n", dic.get(16), meta.get(dic.get(16))))
                .append(String.format("\"%s\": %s,%n", dic.get(17), meta.get(dic.get(17))))
                .append("\"fields\": {\n");                  
        rexcel.setSheet(dic.get(4), 0);
        String[] ids = rexcel.readColumn(0);
        String[] values = rexcel.readColumn(3);
        for ( int i = 0; i < ids.length; i++ ) {
            if (i < ids.length - 1)
                sb = sb.append(String.format("  \"%s\": %s,%n", ids[i], values[i]));
            else
                sb = sb.append(String.format("  \"%s\": %s%n", ids[i], values[i]));
        }
        sb.append("}\n")
                .append("}");
        return sb.toString();
    }
    
    void serializeJson(JsonObject jo) {
        StringWriter writer = new StringWriter();
        JsonWriter jwriter = Json.createWriter(writer);
        jwriter.writeObject(jo);
        System.out.println(writer.toString());       
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
