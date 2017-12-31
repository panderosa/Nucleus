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
import java.nio.file.StandardOpenOption;
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
    private String onBehalf;
    private Net net;
    private JsonObject subscription;
    private long start;
    
    public static void main(String[] args) throws Exception {
        PostSubscription ps = new PostSubscription(args);
        switch (ps.getOption()) {
            case "Order":
                String payload = ps.buildJsonForOrder();
                ps.writeStringToFile(payload);
                String np = ps.readStringFromFile();
                String out = ps.postSub(np);
                break;
            case "CreateTemplate":
                ps.createTemplateForOrder();
                break;
            default:
                String pyl = ps.readStringFromFile();
                Net net = new Net("uspnvucsa006.devp.dbn.hpe.com",8444,"https");
                net.requestToken("idmTransportUser", "ENC(n06qovMG761VhGVf44wDq2oY4yPfBhMDBQDtAlZePQk=)", "svc-afcsa", "ENC(1cWA2ObRUWoK20sEy4bECnLU/M2zlJAz/BDZxkyMIYI=)", "NUCLEUS");
                String token = net.getToken();
                String uri = "/csa/api/mpp/mpp-request/022e2ce75eee2728015f05ec8604323a?catalogId=022e2ce75b337e52015b33862e4e000b";
                String ac = "application/json";
                String cnt = "multipart/form-data; boundary=AlaMaKOta123";
                String output = net.postHttp(token, null, null, uri, pyl, ac, cnt);
                System.out.println(output);
                
        }
    }
    
    public PostSubscription(String[] args) throws Exception {
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
    
    void createTemplateForOrder() throws Exception {
        JsonObject offering = getOffering();
        JsonObject request = getOrder();
        
        String out = getWorkbookName();       
        wexcel = new WriteExcel(out);
        
        // Service Offering sheet
        wexcel.addSheet(dic.get(1), 0);       
        addOfferingFields(0,offering); 
        
        // Request Order sheet
        wexcel.addSheet(dic.get(3), 1);       
        addRequestFields(1,request);      
        
        // Meta sheet
        wexcel.addSheet(dic.get(0), 2);         
        addMetaFields(2,offering); 
        
        // New Order Template
        wexcel.addSheet(dic.get(4), 3);
        addTemplateFieldsToSheet(3,request);        
        
        // Save Workbook
        wexcel.save();
    }
    
    JsonObject getOrder() throws Exception {
        JsonObject out = null;
        if (arguments.get("source").equalsIgnoreCase("file"))
            out = readJsonObjectFromFile(getOrderFileName());
        else if (arguments.get("source").equalsIgnoreCase("csa")) {
            if (subscription == null) viewSubscription();
            out = stringToJson(viewOrder());
        }
        return out;    
    }
    
    JsonObject getOffering() throws Exception {
        JsonObject out = null;
        if (arguments.get("source").equalsIgnoreCase("file"))
            out = readJsonObjectFromFile(getOfferingFileName());
        else if (arguments.get("source").equalsIgnoreCase("csa")) {
            if (subscription == null) viewSubscription();
            out = stringToJson(viewOffering());
        }
        return out;
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
        System.out.print("Initializing connection to CSA.");
        stoper();
        net = new Net(this.csaServer,this.csaPort,this.csaProtocol);  
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
    }
     
    String viewSubscription() throws Exception {
        String uuid = arguments.get("uuid");
        if (uuid == null) raiseError("Argument subscription \"uuid\" is null");
        if (net == null) connectToCSA();
        if (net.getRawToken() == null) {
            System.out.print("Retrieving token from CSA.");
            stoper();
            net.requestToken(this.idmUser, this.idmPassword, this.csaConsumer, this.csaConsumerPassword, this.csaTenant);
            int delta = stoper();
            System.out.format(" Duration(ms) %d%n",delta);
        }       
        String uri = "/csa/api/mpp/mpp-subscription/" + uuid;
        String token = net.getToken();
        System.out.print("Retrieving subscription details from CSA.");
        stoper();
        String out = net.getHttp(token, null, null, uri, "application/json");
        subscription = stringToJson(out);
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return out;
    }    
    
    String viewOrder() throws Exception {
        if (net == null) connectToCSA();
        if (net.getRawToken() == null) net.requestToken(this.idmUser, this.idmPassword, this.csaConsumer, this.csaConsumerPassword, this.csaTenant);   
        String requestId = subscription.getString("requestId");        
        String uri = "/csa/api/mpp/mpp-request/" + requestId;
        uri = (onBehalf != null)? uri + "?onBehalf=" + onBehalf: uri;
        String token = net.getToken();
        System.out.print("Retrieving order details from CSA.");
        stoper();
        String out = net.getHttp(token, null, null, uri, "application/json");
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return out;
    }
    
    String viewOffering() throws Exception {
        if (net == null) connectToCSA();
        if (net.getRawToken() == null) net.requestToken(this.idmUser, this.idmPassword, this.csaConsumer, this.csaConsumerPassword, this.csaTenant);   
        String offeringId = subscription.getString("serviceId");
        String catalogId = subscription.getString("catalogId");
        String category = subscription.getJsonObject("category").getString("name");
        String uri = String.format("/csa/api/mpp/mpp-offering/%s?catalogId=%s&category=%s",offeringId,catalogId,category);
        uri = (onBehalf != null)? uri + "&onBehalf=" + onBehalf: uri;
        String token = net.getToken();
        System.out.print("Retrieving service offering details from CSA.");
        stoper();
        String out = net.getHttp(token, null, null, uri, "application/json");
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return out;
    }
     
    int stoper() {
        int delta = 0;
        long ts = System.nanoTime();
        if (start == 0) start = ts;
        else {
            delta = Math.round((ts - start)/1000000);
            start = 0;
        }
        return delta;
    }
    
    void initDictionary() {
        dic = new ArrayList();
        dic.add(0,"Metadata");
        dic.add(1,"Service Offering");
        dic.add(2,"Subscription Fields");
        dic.add(3,"Request Order");
        dic.add(4,"New Order Template");
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
        dic.add(19,"AlaMaKOta123");
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
        this.onBehalf = onBehalf;
    }
    
    void raiseError(String errorMessage) throws Exception {
        throw new RuntimeException(errorMessage);
    }
    
    
    String getOption() throws Exception {
        String opt = (arguments.get("option") != null)? arguments.get("option"): "default";
        return opt;
    }
    
    String getOrderFileName() throws Exception {
        String file = arguments.get("request");
        if ( file == null)
            raiseError("Argument \"request\" is null.");
        return file;
    }
    
    String getOfferingFileName() throws Exception {
        String file = arguments.get("service");
        if ( file == null)
            raiseError("Argument \"service\" is null.");
        return file;
    }
    
    
    String getWorkbookName() throws Exception {
        String file = arguments.get("workbook");
        if ( file == null)
            raiseError("Argument \"workbook\" is null.");
        return file;
    }
    
    
    void addMetaFields(int seq,JsonObject offering) throws Exception {
        getMetaFields(offering); 
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
    
    void addRequestFields(int seq, JsonObject order) throws Exception {
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
    
    void getMetaFields(JsonObject offering) {
        meta = new HashMap<>();
        meta.put(dic.get(9), offering.getString("id"));
        meta.put(dic.get(10), offering.getJsonObject("category").getString("name"));
        meta.put(dic.get(12), offering.getString("offeringVersion"));
        meta.put(dic.get(13), offering.getString("catalogId"));
        meta.put(dic.get(14), offering.getString("name"));
        meta.put(dic.get(15), "");
        meta.put(dic.get(16), "");
        meta.put(dic.get(17), "");
    }
    
    void addOfferingFields(int seq, JsonObject offering) throws Exception {                   
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
    
    void addTemplateFieldsToSheet(int seq, JsonObject order) throws Exception {
        
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
    
    Map<String,String> getMetaForTemplate() throws Exception {
        String out = getWorkbookName();
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
        Map<String,String> meta = getMetaForTemplate();     
        // Format JSON Body
        String boundary = dic.get(19);
        StringBuilder sb = new StringBuilder();
        sb.append("--"+boundary+"\n");
        sb.append("Content-Disposition: form-data; name=\"requestForm\"\n");
        sb.append("Content-Type: text/json\n\n");
       
        sb.append("{\n")
                .append(String.format("\"action\": \"%s\",%n", dic.get(18)))
                .append(String.format("\"%s\": \"%s\",%n", dic.get(10), meta.get(dic.get(10))))
                .append(String.format("\"%s\": \"%s\",%n", dic.get(15), meta.get(dic.get(15))))
                .append(String.format("\"%s\": \"%s\",%n", dic.get(16), meta.get(dic.get(16))))
                .append(String.format("\"%s\": \"%s\",%n", dic.get(17), meta.get(dic.get(17))))
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
                .append("}\n\n")
                .append("--"+boundary+"--");        
        return sb.toString();
    }
    
    void writeStringToFile(String content) throws Exception {
        byte[] bytes =  content.getBytes("UTF-8");
        Files.write(Paths.get("input.json"), bytes, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
    }
    
    String readStringFromFile() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("input.json"));
        return new String(bytes,"UTF-8");
    }
    
    String postSub(String payload) throws Exception {
        Map<String,String> meta = getMetaForTemplate();
        String uri = String.format("/csa/api/mpp/mpp-request/%s?catalogId=%s",meta.get("serviceId"),meta.get("catalogId"));
        String contentType = "multipart/form-data; boundary=" + dic.get(19);
        if (net == null) connectToCSA();
        if (net.getRawToken() == null) net.requestToken(this.idmUser, this.idmPassword, this.csaConsumer, this.csaConsumerPassword, this.csaTenant);
        String token = net.getToken();
        System.out.println(uri);
        System.out.println(contentType);
        System.out.println(payload);
        System.out.print("Ordering subscription in CSA.");
        stoper();
        String out = net.putHttp(token, null, null, uri, payload, "application/json", contentType);
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return out;
    }
    
    
    
    
    String jsonToString(JsonObject jo) {
        StringWriter writer = new StringWriter();
        JsonWriter jwriter = Json.createWriter(writer);
        jwriter.writeObject(jo);
        return writer.toString();       
    }
    
    JsonObject stringToJson(String txt) {
        StringReader reader = new StringReader(txt);
        JsonReader jreader = Json.createReader(reader);
        return jreader.readObject();
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
    
    String getField(String s,String f) {
        JsonObject sub = stringToJson(s);
        return sub.getString(f);
    }
  
}
