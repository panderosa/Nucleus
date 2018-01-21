/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class Subscription {
    private final String BOUNDARY = "AlaMaKota123";
    private HashMap<String,String> arguments;
    private WriteExcel wexcel;
    private ReadExcel rexcel;
    private Map<String,String> meta;
    private ArrayList<String> dic;
    public String csaServer;
    public int csaPort;
    public String csaProtocol;
    private String idmUser;
    private String idmPassword;
    private String transportUser;
    private String transportPassword;
    private String csaConsumer;
    private String csaConsumerPassword;
    private String csaAdmin;
    private String csaAdminPassword;
    private String csaAdminOrg;
    private String csaTenant;
    private String onBehalf;
    private Net net = null;
    private JsonNode subscription;
    private long start;
    private Map<String,String> input;
    private List<Action> actions;
    private Map<String,Conf> config;
    
    public static void main(String[] args) throws Exception {
        Subscription ps = new Subscription(args);
        switch (ps.getActionName()) {
            case "order":
                ps.orderSubscription();
                break;
            case "cancel":
                ps.cancelSubscription();
                break;
            case "template":
                ps.CreateTemplate();
                break;
            case "availableValues":
                ps.availableValues();
                break;
            case "viewSubscriptionDetails":
                ps.viewSubscriptionDetails();
                break;
            case "getRequest":
                ps.getRequestDetails();
                break;
            case "getErrorInfo":
                ps.getErrorForSubscription();
                break;
            case "getServiceComponentsProperties":
                ps.getServiceComponentsProperties();
                break;
             case "updateComponentProperty":
                ps.updateComponentProperty();
                break;
            default:
                System.out.println("default");
        }
    }
    
    public Subscription(String[] args) throws Exception {
        parseArguments(args);
        input = new HashMap<>();
    }    
    
    public Subscription() {   
        initializeActionsList();
        input = new HashMap<>();
    }  
    
    public List<Action> getActions() {
        return actions;
    }
    
    private void initializeActionsList() {       
        Action a1 = new Action("orderSubscription","Order Subscription");
        a1.addParameter("template", "XLS template with order request");
        
        Action a2 = new Action("viewSubscriptionDetails","View Subscription Details");
        a2.addParameter("subscriptionId", "Subscription Id");
        a2.addParameter("apiName", "CSA API, use mpp|service");
        
        Action a3 = new Action("getAvailableValues","Get Available Values");
        a3.addParameter("fieldId", "Field Id (without prefix)");
        a3.addParameter("inputFieldId", "Input Field Name");
        a3.addParameter("inputFieldValue", "Input Field Value");
        
        Action a4 = new Action("viewRequestDetails","View Request Details");
        a4.addParameter("requestId", "Request Id");
        
        Action a5 = new Action("viewSubscriptionErrorInfo","View Subscription Error Info");
        a5.addParameter("subscriptionId", "Subscription Id");
        
        Action a6 = new Action("viewServiceComponentsProperties","View Service Components Properties");
        a6.addParameter("subscriptionId", "Subscription Id");
        
        Action a7 = new Action("updateComponentProperty","Update Components Property Value");
        a7.addParameter("componentId", "Service Component Id");
        a7.addParameter("propertyName", "Property Name");
        a7.addParameter("propertyDisplayName", "Property Display Name");
        a7.addParameter("propertyValueType", "Property Value Type");
        a7.addParameter("propertyVisibility", "Property Visibility, use true|false");
        a7.addParameter("propertyValue", "Property Value");
        
        Action a8 = new Action("updateProcessInstance","Update Process Instance");
        a8.addParameter("csaProcessId", "CSA Process Id");
        a8.addParameter("processState", "State, e.g. COMPLETED...");
        a8.addParameter("processReturnCode", "Return Code, e.g. SUCCESS, FAILURE");
        a8.addParameter("processStatus", "Status Information");
        
        actions = Arrays.asList(a1,a2,a3,a4,a5,a6,a7,a8);   
    }
      
    public String wrapperMethodForGui(String[] args) {
        String out;
        try {
            parseArguments(args);
            initCSAClient();
            String action = getActionName();
            switch (action) {
                case "orderSubscription":
                    out = "orderSubscription";
                    break;
                 case "viewSubscriptionDetails":
                    out = viewSubscriptionDetails();
                    break;
                 case "getAvailableValues":
                    out = "getAvailableValues";
                    break;
                 case "viewRequestDetails":
                    out = "viewRequestDetails";
                    break;
                 case "viewSubscriptionErrorInfo":
                    out = "viewSubscriptionErrorInfo";
                    break;
                 case "viewServiceComponentsProperties":
                    out = "viewServiceComponentsProperties";
                    break;
                 case "updateComponentProperty":
                    out = "updateComponentProperty";
                    break;
                 case "updateProcessInstance":
                    out = "updateProcessInstance";
                    break;    
                default:
                    out = "No method selected";
            }
        }
        catch(Exception e) {
            out = processException(e);
        }
        return out;
    }
    
    public String readConfiguration(File file) {
        String out = null;
        try {
            ObjectMapper mapper = new ObjectMapper();   
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            JsonNode root = mapper.readTree(file);
            setUpConfiguration(root);
            out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);        
        }
        catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            PrintWriter pw = new PrintWriter(baos,true);
            e.printStackTrace(pw);
            out = baos.toString();
        }
        return out;    
    }
    
    private void setUpConfiguration(JsonNode root) throws Exception {  
        config = new HashMap<>();
        idmUser = root.get("idmUser").get("name").asText();
        config.put("idmUser", new Conf("IDM Transport User",idmUser,"STRING","plain"));
        idmPassword = root.get("idmUser").get("password").asText();
        config.put("idmPassword", new Conf("IDM Transport Password",idmPassword,"STRING","hidden"));
        transportUser = root.get("transportUser").get("name").asText();
        config.put("transportUser", new Conf("CSA Transport User",transportUser,"STRING","plain"));
        transportPassword = root.get("transportUser").get("password").asText();
        config.put("transportPassword", new Conf("CSA Transport Password",transportPassword,"STRING","hidden"));
        csaConsumer = root.get("defaultConsumer").asText();
        config.put("csaConsumer", new Conf("Consumer User",csaConsumer,"STRING","plain"));
        csaConsumerPassword = root.get(csaConsumer).asText();
        config.put("csaConsumerPassword", new Conf("Consumer Password",csaConsumerPassword,"STRING","hidden"));
        csaTenant = root.get("defaultTenant").asText();
        config.put("csaTenant", new Conf("Consumer Tenant",csaTenant,"STRING","plain"));
        onBehalf = root.get("onBehalf").asText();
        config.put("onBehalf", new Conf("Manage On Behalf User",onBehalf,"STRING","plain"));
        csaAdmin = root.get("defaultPrivilegedUser").asText();
        config.put("csaAdmin", new Conf("CSA Privileged User",csaAdmin,"STRING","plain"));
        csaAdminPassword = root.get(csaAdmin).asText();
        config.put("csaAdminPassword", new Conf("Priviliged Password",csaAdminPassword,"STRING","hidden"));
        csaAdminOrg = root.get("defaultAdminOrganization").asText();
        config.put("csaAdminOrg", new Conf("Provider Organization",csaAdminOrg,"STRING","plain"));       
        csaServer = root.get("csaAS").get("Server").asText();
        config.put("csaServer", new Conf("CSA Server",csaServer,"STRING","plain"));     
        csaPort = root.get("csaAS").get("Port").asInt();
        config.put("csaPort", new Conf("TCP Port",Integer.toString(csaPort),"INT","plain"));  
        csaProtocol = root.get("csaAS").get("Protocol").asText();
        config.put("csaProtocol", new Conf("Transport Protocol",csaProtocol,"STRING","plain")); 
    } 
    
    Map<String,Conf> getConfiguration() {
        return config;
    }
    
    void setConfiguration(Map<String,Conf> config) {
        this.config = config;
    }
    
    void initCSAClient() throws Exception {
        if (net == null)
            net = new Net(csaServer,csaPort,csaProtocol);
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
    
    void CreateTemplate() throws Exception {
        JsonNode offering = getOffering();
        JsonNode request = getOrder();
        
        String out = getWorkbookName();       
        wexcel = new WriteExcel(out);
        
        // Service Offering sheet
        wexcel.addSheet("Service Offering", 0);       
        addOfferingFields(0,offering); 
        
        // Request Order sheet
        wexcel.addSheet("Request Order", 1);       
        addRequestFields(1,request);      
        
        // Meta sheet
        wexcel.addSheet("Metadata", 2);         
        addMetaFields(2,offering); 
        
        // New Order Template
        wexcel.addSheet("New Order Template", 3);
        addTemplateFieldsToSheet(3,request);        
        
        // Save Workbook
        wexcel.save();
    }
    
    JsonNode getOrder() throws Exception {
        JsonNode out = null;
        ObjectMapper mapper = new ObjectMapper();
        if (arguments.get("source").equalsIgnoreCase("file")) {
            String file = getOrderFileName();
            String jc = new String(Files.readAllBytes(Paths.get(file)));
            out = mapper.readTree(jc);
            
        }
        else if (arguments.get("source").equalsIgnoreCase("csa")) {
            if (subscription == null) viewSubscription();
            String id = subscription.get("requestId").asText();
            out = mapper.readTree(getRequestDetails(id));
        }
        return out;    
    }
    
    JsonNode getOffering() throws Exception {
        JsonNode out = null;
        ObjectMapper mapper = new ObjectMapper();
        if (arguments.get("source").equalsIgnoreCase("file")) {
            String file = getOfferingFileName();
            String jc = new String(Files.readAllBytes(Paths.get(file)));
            out = mapper.readTree(jc);
        }
        else if (arguments.get("source").equalsIgnoreCase("csa")) {
            if (subscription == null) viewSubscription();
            out = mapper.readTree(viewOffering());
        }
        return out;
    }
    
    
    
    public String getDefaultConfiguration(File file) throws Exception {
            ObjectMapper mapper = new ObjectMapper();        
            HashMap<String,Object> cnf = mapper.readValue(file, HashMap.class);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            idmUser = ((ArrayList<String>)cnf.get("idmUser")).get(0);
            idmPassword = ((ArrayList<String>)cnf.get("idmUser")).get(1);
            transportUser = ((ArrayList<String>)cnf.get("transportUser")).get(0);
            transportPassword = ((ArrayList<String>)cnf.get("transportUser")).get(1);
            csaConsumer = (String) cnf.get("defaultConsumer");
            csaConsumerPassword = (String) cnf.get(csaConsumer);
            csaAdmin = (String) cnf.get("defaultPrivilegedUser");
            csaAdminPassword = (String) cnf.get(csaAdmin);
            csaAdminOrg = (String) cnf.get("defaultAdminOrganization");
            csaTenant = (String) cnf.get("defaultTenant");
            csaServer = (String) ((HashMap<String,Object>) cnf.get("csaAS")).get("Server");
            csaPort = ((Integer) ((HashMap<String,Object>) cnf.get("csaAS")).get("Port")).intValue();
            csaProtocol = (String) ((HashMap<String,Object>) cnf.get("csaAS")).get("Protocol");
            onBehalf = "";
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cnf);
    }
    
    private void setCSAParams(String file, String host, String consumer, String tenant, String onBehalf) throws Exception {
        byte[] content = Files.readAllBytes(Paths.get(file));
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,Object> cnf = mapper.readValue(content, HashMap.class);
        idmUser = ((ArrayList<String>)cnf.get("idmUser")).get(0);
        idmPassword = ((ArrayList<String>)cnf.get("idmUser")).get(1);
        transportUser = ((ArrayList<String>)cnf.get("transportUser")).get(0);
        transportPassword = ((ArrayList<String>)cnf.get("transportUser")).get(1);
        csaConsumer = (consumer == null)? (String) cnf.get("defaultConsumer"): consumer;
        csaConsumerPassword = (String) cnf.get(csaConsumer);
        csaAdmin = (String) cnf.get("defaultPrivilegedUser");
        csaAdminPassword = (String) cnf.get(csaAdmin);
        csaAdminOrg = (String) cnf.get("defaultAdminOrganization");
        csaTenant = (tenant == null)? (String) cnf.get("defaultTenant"): tenant;
        csaServer = (host == null)? (String) ((HashMap<String,Object>) cnf.get("csaAS")).get("Server"): host;
        csaPort = ((Integer) ((HashMap<String,Object>) cnf.get("csaAS")).get("Port")).intValue();
        csaProtocol = (String) ((HashMap<String,Object>) cnf.get("csaAS")).get("Protocol");
        this.onBehalf = onBehalf;
    } 
    
    
    void initCSA() throws Exception {
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
        net = new Net(csaServer,csaPort,csaProtocol);  
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
    }
     
    String viewSubscription() throws Exception {
        String uuid = arguments.get("uuid");
        if (uuid == null) raiseError("Argument subscription \"uuid\" is null");
        if (net == null) initCSA();
        String token = requestToken();
        String uri = "/csa/api/mpp/mpp-subscription/" + uuid;
        System.out.print("Retrieving subscription details from CSA.");
        stoper();
        String out = net.getHttp(token, null, null, uri, "application/json");
        ObjectMapper mapper = new ObjectMapper();
        subscription = mapper.readTree(out);
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return out;
    }    
    
    public void getRequestDetails() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Request \"id\" is null");
        System.out.println(getRequestDetails(id));
    }
    
    String getRequestDetails(String id) throws Exception {
        if (net == null) initCSA();
        String token = requestToken();   
        String uri = "/csa/api/mpp/mpp-request/" + id;
        uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
        System.out.print("Retrieving request details from CSA.");
        stoper();
        String out = net.getHttp(token, null, null, uri, "application/json");
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return out;
    }
    
    String viewOffering() throws Exception {
        if (net == null) initCSA();
        if (net.getRawToken() == null) net.requestToken(this.idmUser, this.idmPassword, this.csaConsumer, this.csaConsumerPassword, this.csaTenant);   
        String serviceId = subscription.get("serviceId").asText();
        String catalogId = subscription.get("catalogId").asText();
        String categoryName = subscription.get("category").get("name").asText();
        String uri = String.format("/csa/api/mpp/mpp-offering/%s?catalogId=%s&category=%s",serviceId,catalogId,categoryName);
        uri = (onBehalf != null)? uri + "&onBehalf=" + onBehalf: uri;
        String token = requestToken();
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
    
    void raiseError(String errorMessage) throws Exception {
        throw new RuntimeException(errorMessage);
    }
    
    
    String getActionName() throws Exception {
        String opt = (arguments.get("action") != null)? arguments.get("action"): "default";
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
    
    
    void addMetaFields(int seq,JsonNode offering) throws Exception {
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
    
    void addRequestFields(int seq, JsonNode order) throws Exception {
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);        
        wexcel.addHeader(seq, 0, 0, "Field Identifier");
        wexcel.addHeader(seq, 1, 0, "Field Name");
        wexcel.addHeader(seq, 2, 0, "Display Name");
        wexcel.addHeader(seq, 3, 0, "Field Value");
        Iterator<JsonNode> fields = order.get("fields").elements();
        // Data start from row 1
        int r = 1;
        while (fields.hasNext()) {
            JsonNode field = fields.next();

            String id = field.get("id").asText();
            wexcel.addContent(seq, 0, r, id);
            String name = field.get("name").asText();
            wexcel.addContent(seq, 1, r, name);
            String displayName = field.get("displayName").asText();
            wexcel.addContent(seq, 2, r, displayName);
            String value = "";
            if ( field.get("value") != null ) {
                if (field.get("value").getNodeType() == JsonNodeType.STRING) 
                    value = String.format("\"%s\"", field.get("value").asText());
                else if (field.get("value").getNodeType() == JsonNodeType.NUMBER)
                    value = String.format("%d", field.get("value").asInt());
                else if (field.get("value").getNodeType() == JsonNodeType.BOOLEAN)
                    value = Boolean.toString(field.get("value").asBoolean());
                else 
                    value = ""; 
            }
            else 
                value = "";
            wexcel.addContent(seq, 3, r, value);
            r++;
        };
    }
    
    void _addRequestFields(int seq, JsonNode order) throws Exception {
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);        
        wexcel.addHeader(seq, 0, 0, "Field Identifier");
        wexcel.addHeader(seq, 1, 0, "Field Name");
        wexcel.addHeader(seq, 2, 0, "Display Name");
        wexcel.addHeader(seq, 3, 0, "Field Value");
        Iterator<JsonNode> fields = order.get("fields").elements();
        // Data start from row 1
        int r = 1;
        while (fields.hasNext()) {
            JsonNode field = fields.next();

            String id = field.get("id").asText();
            wexcel.addContent(seq, 0, r, id);
            String name = field.get("name").asText();
            wexcel.addContent(seq, 1, r, name);
            String displayName = field.get("displayName").asText();
            wexcel.addContent(seq, 2, r, displayName);
            String value = "";
            if ( field.get("value") != null ) {
                if (field.get("value").getNodeType() == JsonNodeType.STRING)
                    wexcel.addString(seq, 3, r, field.get("value").asText(), WriteExcel.CellFormats.DataNoWrap);
                else if (field.get("value").getNodeType() == JsonNodeType.NUMBER)
                    wexcel.addNumber(seq, 3, r, field.get("value").asInt(), WriteExcel.CellFormats.DataNoWrap);
                else if (field.get("value").getNodeType() == JsonNodeType.BOOLEAN)
                    wexcel.addBoolean(seq, 3, r, field.get("value").asBoolean(), WriteExcel.CellFormats.DataNoWrap);
            }
            else 
                wexcel.addString(seq, 3, r, "", WriteExcel.CellFormats.DataNoWrap);
            r++;
        };
    }
    
    void getMetaFields(JsonNode offering) {
        meta = new HashMap<>();
        meta.put("serviceId", offering.get("id").asText());
        meta.put("categoryName", offering.get("category").get("name").asText());
        meta.put("offeringVersion", offering.get("offeringVersion").asText());
        meta.put("catalogId", offering.get("catalogId").asText());
        meta.put("serviceName", offering.get("name").asText());
        meta.put("subscriptionName", "");
        meta.put("subscriptionDescription", "");
        meta.put("startDate", "");
    }
    
    void addOfferingFields(int seq, JsonNode offering) throws Exception {                   
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);  
        wexcel.addHeader(seq, 0, 0, "Field Identifier");
        wexcel.addHeader(seq, 1, 0, "Field Name");
        wexcel.addHeader(seq, 2, 0, "Display Name");
        wexcel.addHeader(seq, 3, 0, "Field Value");
        Iterator<JsonNode> fields = offering.get("fields").elements();
        // Data start from row 1
        int r = 1;
        while (fields.hasNext()) {
            JsonNode field = fields.next();
            String id = field.get("id").asText();
            wexcel.addContent(seq, 0, r, id);
            String name = field.get("name").asText();
            wexcel.addContent(seq, 1, r, name);
            String displayName = field.get("displayName").asText();
            wexcel.addContent(seq, 2, r, displayName);
            String value = "";
            if ( field.get("value") != null ) {
                if (field.get("value").getNodeType() == JsonNodeType.STRING) 
                    value = String.format("\"%s\"", field.get("value").asText());
                else if (field.get("value").getNodeType() == JsonNodeType.NUMBER)
                    value = String.format("%d", field.get("value").asInt());
                else if (field.get("value").getNodeType() == JsonNodeType.BOOLEAN)
                    value = Boolean.toString(field.get("value").asBoolean());
                else 
                    value = ""; 
            }
            else 
                value = "";
            wexcel.addContent(seq, 3, r, value);
            addFieldId(name,id);
            r++;
        };
    }
    
    void addTemplateFieldsToSheet(int seq, JsonNode order) throws Exception {
        
        // Add header
        wexcel.setColumnSize(seq, 0, 0);
        wexcel.setColumnSize(seq, 1, 0);
        wexcel.setColumnSize(seq, 2, 0);
        wexcel.setColumnSize(seq, 3, 0);  
        wexcel.addHeader(seq, 0, 0, "Field Identifier");
        wexcel.addHeader(seq, 1, 0, "Field Name");
        wexcel.addHeader(seq, 2, 0, "Display Name");
        wexcel.addHeader(seq, 3, 0, "Field Value");
        Iterator<JsonNode> fields =  order.get("fields").elements();
        // Data start from row 1
        int r = 1;
        while (fields.hasNext()) {
            JsonNode field = fields.next();          
            String name = field.get("name").asText();
            wexcel.addContent(seq, 1, r, name);
            String id = input.get(name);
            wexcel.addContent(seq, 0, r, id);
            String displayName = field.get("displayName").asText();
            wexcel.addContent(seq, 2, r, displayName);
            String value = "";
            if ( field.get("value") != null ) {
                if (field.get("value").getNodeType() == JsonNodeType.STRING) 
                    value = String.format("\"%s\"", field.get("value").asText());
                else if (field.get("value").getNodeType() == JsonNodeType.NUMBER)
                    value = String.format("%d", field.get("value").asInt());
                else if (field.get("value").getNodeType() == JsonNodeType.BOOLEAN)
                    value = Boolean.toString(field.get("value").asBoolean());
                else 
                    value = ""; 
            }
            else 
                value = "";
            wexcel.addContent(seq, 3, r, value);
            r++;
        }
    }
    
    void initMetaFromExcel() throws Exception {
        String out = getWorkbookName();
        if (rexcel == null) rexcel = new ReadExcel(out);
        rexcel.setSheet("Metadata", 0);
        String[] params = rexcel.readColumn(0);
        String[] paramValues = rexcel.readColumn(1);
        meta = new HashMap<>();
        for ( int j = 0; j < params.length; j++ ) {
            meta.put(params[j], paramValues[j]);
        }
    }
    
    
    String buildJsonFromExcel() throws Exception {   
        // Format JSON Body

        StringBuilder sb = new StringBuilder();
        sb.append("--"+BOUNDARY+"\n");
        sb.append("Content-Disposition: form-data; name=\"requestForm\"\n");
        sb.append("Content-Type: text/json\n\n");
       
        sb.append("{\n")
                .append(String.format("\"action\": \"%s\",%n", "ORDER"))
                .append(String.format("\"categoryName\": \"%s\",%n", meta.get("categoryName")))
                .append(String.format("\"subscriptionName\": \"%s\",%n", meta.get("subscriptionName")))
                .append(String.format("\"subscriptionDescription\": \"%s\",%n", meta.get("subscriptionDescription")))
                .append(String.format("\"startDate\": \"%s\",%n", meta.get("startDate")))
                .append("\"fields\": {\n");                  
        // Read fields from workbook
        if (rexcel == null) {
            String out = getWorkbookName();
            rexcel = new ReadExcel(out);
        }
        rexcel.setSheet("New Order Template", 0);
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
                .append("--"+BOUNDARY+"--");        
        return sb.toString();
    }
    
    String buildOrderPayload() throws Exception {   
        // Format JSON Body

        StringBuilder sb = new StringBuilder();
        sb.append("--"+BOUNDARY+"\n");
        sb.append("Content-Disposition: form-data; name=\"requestForm\"\n");
        sb.append("Content-Type: text/json\n\n");
       
        Map<String,Object> content = new HashMap<>();
        content.put("action","ORDER");
        content.put("categoryName", meta.get("categoryName"));
        content.put("subscriptionName", meta.get("subscriptionName"));
        content.put("subscriptionDescription",meta.get("subscriptionDescription"));
        content.put("startDate", meta.get("startDate"));
        
        Map<String,String> fields = new HashMap<>();
        // Read fields from workbook
        if (rexcel == null) {
            String out = getWorkbookName();
            rexcel = new ReadExcel(out);
        }
        rexcel.setSheet("New Order Template", 0);
        String[] ids = rexcel.readColumn(0);
        String[] values = rexcel.readColumn(3);
        for ( int i = 0; i < ids.length; i++ ) {
            fields.put(ids[i], values[i]);
        }      
        content.put("fields", fields);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String cs = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(content);
        
        sb.append(cs).append("\n\n").append("--"+BOUNDARY+"--");        
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
    
    public void orderSubscription() throws Exception {
        initMetaFromExcel();
        String pyl = buildJsonFromExcel();
        initCSA();
        String token = requestToken();
        String uri = "/csa/api/mpp/mpp-request/" + meta.get("serviceId") + "?catalogId=" + meta.get("catalogId");
        String ac = "application/json";
        String cnt = "multipart/form-data; boundary=" + BOUNDARY;
        System.out.print("Ordering subscription.");
        stoper();
        String output = net.postHttp(token, null, null, uri, pyl, ac, cnt);
        int del = stoper();
        System.out.format(" Duration(ms) %d%n",del);
        //System.out.println(output);
        ObjectMapper mapper = new ObjectMapper();
        String orderId = mapper.readTree(output).get("id").asText();
        System.out.format("Request Order ID: %s%n", orderId);
    }
    
    public void getErrorForSubscription() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Subscription \"id\" is null");
        String sub = viewSubscriptionDetails(id,"service");
        ObjectMapper mapper = new ObjectMapper();
        String sid = mapper.readTree(sub).get("ext").get("csa_service_instance_id").asText();
        String service = getServiceDetails(sid);
        getErrorForSubscription(service);
    }
    
    void getErrorForSubscription(String content) throws Exception {
        Map<String,Object> obj = new HashMap<String,Object>();
        //String content = new String(Files.readAllBytes(Paths.get("instance.json")),"UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        ArrayList<Map<String,Object>> components = (ArrayList<Map<String,Object>>) mapper.readValue(content, HashMap.class).get("components");
        components.forEach(component -> {
            String name = (String) component.get("name");
            if (name.startsWith("SERVICE_COMPOSITE")) {
                ArrayList<Map<String,Object>> properties = (ArrayList<Map<String,Object>>) component.get("properties");
                properties.forEach(property -> {
                    String pn = (String) property.get("name");
                    switch(pn) {
                        case "ERRORCOMPONENT":
                        case "ERRORMSG":    
                        case "ERRORDATE":
                        sb.append(String.format("%-18s-> %s%n", pn, Arrays.toString(((ArrayList<String>) property.get("value")).toArray())));
                        break;
                    }                    
                });
                System.out.println(sb.toString());
            }
        });       
    }
    
    
    void cancelSubscription() throws Exception {
        String uuid = arguments.get("uuid");
        if (uuid == null) raiseError("Argument subscription \"uuid\" is null");
        initCSA();
        String uri = "/csa/api/service/subscription/" + uuid + "/cancel";
        String ac = "*/*";
        String cnt = "application/json"; 
        String payload = "";
        String output = net.postHttp(null, this.csaAdmin, this.csaAdminPassword, uri, payload, ac, cnt);
        System.out.println(output);
    }
    
    class SelectedValue {
        Object value;
        String name;
        String displayName;
    };
    
    public void availableValues() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Argument field \"id\" is null");
        boolean filter = (arguments.get("filter") == null)? false: Boolean.valueOf(arguments.get("filter"));
        initCSA();
        String input = arguments.get("input");
        String fields = (input != null)? input.replaceAll("::", "="): "name=value";
        String uid = getUserId();
        String uri = "/csa/rest/availablevalues/" + id + "?userIdentifier=" + uid;
        System.out.println(uri);
        String cnt = "application/json";
        String output = net.postHttp(null, transportUser, transportPassword, uri, fields, cnt, cnt);
        ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        if (filter) {
            Iterator<JsonNode> availableValues = mapper.readTree(output).get("availableValues").elements();
            while(availableValues.hasNext()) {
                String memo = "";
                JsonNode av = availableValues.next();
                String description = av.get("description").asText();
                String displayName = av.get("displayName").asText();
                memo = String.format("Display Name: %s%nDescription: %s", displayName, description);
                if ( av.get("value") != null ) {
                    if (av.get("value").getNodeType() == JsonNodeType.STRING)
                       memo = String.format("%s%nValue: \"%s\"%n", memo, av.get("value").asText());
                    else if (av.get("value").getNodeType() == JsonNodeType.NUMBER)
                       memo = String.format("%s%nValue: %d%n", memo, av.get("value").asInt()); 
                    else if (av.get("value").getNodeType() == JsonNodeType.BOOLEAN)
                        memo = String.format("%s%nValue: %s%n", memo, Boolean.toString(av.get("value").asBoolean()).toLowerCase());
                }
                else 
                    memo = String.format("%s%nValue: %s%n", memo, "null");
                System.out.println(memo);
            }
        }
        else {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            HashMap<String,Object> av = (HashMap<String,Object>) mapper.readValue(output, HashMap.class);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(av));
        }
    }
    
    public String viewSubscriptionDetails() throws Exception {
        String id = arguments.get("subscriptionId");
        if (id == null) raiseError("Subscription \"id\" is null");
        String api = arguments.get("apiName");
        
        //initCSAClient();
        String output = viewSubscriptionDetails(id,api);
        return output;
    }
    
    String viewSubscriptionDetails(String id, String api) throws Exception {       
        String uri;
        String output = null;
        String acceptContent = "application/json";
        //initCSA();
        if (api.equalsIgnoreCase("service")) {
            uri = "/csa/api/service/subscription/" + id;
            String ac = "application/json";
            output = net.getHttp(null, config.get("csaAdmin").getValue(), config.get("csaAdminPassword").getValue(), uri, acceptContent);
        }
        else {
            uri = "/csa/api/mpp/mpp-subscription/" + id;
            String onBehalf = config.get("onBehalf").getValue();
            uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
            String token = requestToken();
            output = net.getHttp(token, null, null, uri, acceptContent);
        }
        return output;
    }
    
    // https://csaf-dev.devp.dbn.hpe.com:8444/csa/api/service/offering/022e2ce75eee2728015f24530b1141fc/subscription/filter
    //{"summaryTotals":true,"organizationIds":["022e2ce75b337e52015b33862d730009"]}
    // Get Organization subscriptions for a given service offering 
    
    public String updateComponentProperty() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Component \"id\" is null");
        String name = arguments.get("name");
        if (name == null) raiseError("Property name \"name\" is null");
        String displayName = arguments.get("displayName");
        if (displayName == null) raiseError("Property display name \"displayName\" is null");
        String valueType = arguments.get("valueType");
        if (valueType == null) raiseError("Property value type \"valueType\" is null");
        String value = arguments.get("value");
        if (value == null) raiseError("Property value \"value\" is null");
        String visible = arguments.get("visible");
        if (visible == null || !(visible.equalsIgnoreCase("true") || visible.equalsIgnoreCase("false")) ) raiseError("Property visibility attribute \"visible\" must be set to true or false");
        initCSA();
        String userId = getUserId();
        String uri = "/csa/rest/artifact/" + id + "?userIdentifier=" + userId + "&view=propertyinfo&scope=view&_action_=merge&property_values_action_=update";
        String payload = "<ServiceComponent><id>" + id + "</id><property><name>" + name + "</name><displayName>" + displayName + "</displayName><valueType><name>" + valueType + "</name></valueType><values><value>" + value + "</value></values><consumerVisible>" + visible + "</consumerVisible></property></ServiceComponent>";      
        String output = net.putHttp(null, csaAdmin, csaAdminPassword, uri, payload, "application/xml", "application/xml");
        return output;
    }
    
    public String getServiceComponentsProperties() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Subscription \"id\" is null");
        String sub = viewSubscriptionDetails(id,"service");
        ObjectMapper mapper = new ObjectMapper();
        String sid = mapper.readTree(sub).get("ext").get("csa_service_instance_id").asText();
        String service = getServiceDetails(sid);
        Map<String,Map> components = new HashMap<>();
        mapper.readTree(service).get("components").forEach(item->{
            Map<String,JsonNode> properties = new HashMap<>();
            String dn = item.get("displayName").asText();
            item.get("properties").forEach(props-> {
                properties.put(props.get("name").asText(), props.get("value").get(0));
            });
            components.put(dn, properties);            
        });
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(components);
    }
    
    public String getServiceDetails() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Subscription \"id\" is null");
        String sub = viewSubscriptionDetails(id,"service");
        ObjectMapper mapper = new ObjectMapper();
        String sid = mapper.readTree(sub).get("ext").get("csa_service_instance_id").asText();
        String out = getServiceDetails(sid);
        return out;
    }
    
    String getServiceDetails(String id) throws Exception {
        String acceptContent = "application/json";
        String uri = "/csa/api/mpp/mpp-instance/" + id;
        uri = (onBehalf != null)? uri + "?onBehalf=" + onBehalf: uri;
        if (net == null) initCSA();
        String token = requestToken();
        System.out.print("Retrieving Service Details.");
        stoper();
        String output = net.getHttp(token, null, null, uri, acceptContent);
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        return output;
    }
    
    public String updateProcessInstance() throws Exception {
        String id = arguments.get("id");
        if (id == null) raiseError("Process Instance \"id\" is null");
        String state = arguments.get("state");
        if (state == null) raiseError("Process Instance \"state\" is null. E.g. use COMPLETED.");
        String returnCode = arguments.get("returnCode");
        if (returnCode == null) raiseError("Process Instance \"returnCode\" is null. E.g. use [FAILURE|SUCCESS]");
        String status = arguments.get("status");
        if (status == null) raiseError("Process Instance \"status\" is null");
        initCSA();
        String userId = getUserId();
        String uri = "/csa/rest/processinstances/" + id + "?userIdentifier=" + userId + "&scope=view&view=processinstancestate&action=merge";	
        String payload = "<ProcessInstance><id>"+id+"</id><processInstanceState><name>"+state+"</name></processInstanceState><processReturnCode><name>"+returnCode+"</name></processReturnCode><status>"+status+"</status></ProcessInstance>";
        String output = net.putHttp(null, csaAdmin, csaAdminPassword, uri, payload, "application/xml", "application/xml");
        return output;
    }
    
    String getUserId() throws Exception {
        System.out.print("Retrieving CSA User Identifier for REST Operations.");
        String ac = "application/json";
        String uri = "/csa/rest/login/" + csaAdminOrg + "/" + csaAdmin + "/";
        stoper(); 
        String output = net.getHttp(null, transportUser, transportPassword, uri, ac);
        int delta = stoper();
        System.out.format(" Duration(ms) %d%n",delta);
        ObjectMapper mapper = new ObjectMapper();        
        return mapper.readTree(output).get("id").asText();
    }
    
    String requestToken() throws Exception {
        String rtoken = net.getRawToken();
        if (rtoken == null) {
            System.out.print("Requesting CSA Token...");
            stoper();
            net.requestToken(idmUser, idmPassword, csaConsumer, csaConsumerPassword, csaTenant);
            int delta = stoper();
            System.out.format(" Duration(ms) %d%n",delta);
            rtoken = net.getRawToken();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(rtoken).get("token").get("id").asText();
    }
    
    private String processException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter pw = new PrintWriter(baos,true);
        e.printStackTrace(pw);
        String out = baos.toString();
        return out;
    }
            
}
