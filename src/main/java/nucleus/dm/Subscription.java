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
import java.lang.reflect.Method;
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
import java.util.Map.Entry;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Subscription {
    private final String BOUNDARY = "AlaMaKota123";
    private HashMap<String,String> arguments;
    private WriteExcel wexcel;
    private ReadExcel rexcel;
    private Map<String,String> meta;
    private ArrayList<String> dic;
    private Net csa = null;
    private long start;
    private Map<String,String> input;
    private List<Action> actions;
    //private Map<String,Conf> configuration;
    private Screen screen;
    private Subscription subscription;
    private Configuration configuration;
    
    public static void main(String[] args) throws Exception {
     
    }
    
    
    public Subscription(String[] args) throws Exception {
        parseArguments(args);
        input = new HashMap<>();
    }    
    
    public Subscription(Screen screen) {   
        this.screen = screen;
        initializeActionsList();
        input = new HashMap<>();
    }  
    
    public List<Action> getActions() {
        return actions;
    }
    
    // (1) Add new action definition
    /*private void initializeActionsList() {       
        
        Action a110 = new Action("cancelSubscription","Cancel Subscription");
        a110.addParameter("subscriptionId", "Subscription Id");
        
        //getServiceInstanceForSubscription
        Action a210 = new Action("viewServiceForSubscription","View Service Instance For Subscription");
        a210.addParameter("subscriptionId", "Subscription Id");
        
        //viewSubscriptionOrder
        Action a220 = new Action("viewSubscriptionOrder","View Request Order For Subscription");
        a210.addParameter("subscriptionId", "Subscription Id");
        
        //viewOfferingForSubscription
        Action a230 = new Action("viewOfferingForSubscription","View Service Offering For Subscription");
        a230.addParameter("subscriptionId", "Subscription Id");
        
        

        

        
        Action a500 = new Action("viewSubscriptionErrorInfo","View Subscription Error Info");
        a500.addParameter("subscriptionId", "Subscription Id");
        
        
        
        Action a700 = new Action("updateComponentProperty","Update Components Property Value");
        a700.addParameter("componentId", "Service Component Id");
        a700.addParameter("propertyName", "Property Name");
        a700.addParameter("propertyDisplayName", "Property Display Name");
        a700.addParameter("propertyValueType", "Property Value Type");
        a700.addParameter("propertyVisibility", "Property Visibility, use true|false");
        a700.addParameter("propertyValue", "Property Value");
        
        
        
        
        actions = Arrays.asList(a100,a110,a200,a210,a220,a230,a240,a300,a400,a500,a600,a700,a800,a900);   
    }*/
    
    void initializeActionsList() {
        
        Action a100 = new Action("orderSubscription","Order Subscription") {
            
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                Label label = new Label("XLS Template Name");
                TextField field = new TextField();
                field.setEditable(false);
                field.setPromptText("Enter to select template");
                field.setId("template");
                
                FileChooser fc = new FileChooser();
                field.setOnMouseClicked(e->{
                    File file = fc.showOpenDialog(stage);
                    field.setText(file.getAbsolutePath());
                });                
                gp.addRow(1, label,field);                
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());       
                params.put("template", ((TextField) gp.lookup("#template")).getText());
                setParameters(params);
            }
            
        };
        
        Action a150 = new Action("listSubscriptionsByUserInOrganization","List Subscriptions For CSA Organization") {           
            String selected;
            
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                
                Label label = new Label("Select Organization");
                ComboBox cb = new ComboBox();
                cb.setItems(null);
                cb.setId("organizationId");
                // Set default Value
                cb.setMaxWidth(Double.MAX_VALUE);
                cb.setDisable(true);
                
                //cb.setItems(getCSAOrganizations());
                Task task = new Task<Void>() {
                    @Override
                    public Void call() throws Exception {
                        cb.setItems(getCSAOrganizations());
                        cb.setDisable(false);
                        return null;
                    }
                };
                new Thread(task).start(); 
                
                cb.setCellFactory(new Callback<ListView<Organization>,ListCell<Organization>>(){
                    @Override
                    public ListCell<Organization> call(ListView<Organization> param) {
                        ListCell<Organization> cell = new ListCell<Organization>() {
                            @Override
                            protected void updateItem(Organization itm, boolean bln) {                               
                                super.updateItem(itm, bln); //To change body of generated methods, choose Tools | Templates.
                                if (itm!= null) {
                                    setText(itm.getName());
                                }
                            }                      
                        };
                       return cell;
                    };
                });
                
                cb.getSelectionModel().selectedItemProperty().addListener((ob,oo,no)->{
                    selected = ((Organization) no).getId();
                });
                               
                gp.addRow(1, label,cb); 
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());       
                params.put("organizationId", selected);
                
                setParameters(params);
            }

        };
        
        
        Action a200 = new Action("viewSubscriptionDetails","View Subscription Details") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                Label label = new Label("Subscription Id");
                TextField field = new TextField();
                field.setPromptText("Enter subscription id");
                field.setId("subscriptionId");
                Label label1 = new Label("CSA API Type");
                ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList("mpp","service"));
                cb.setId("apiName");
                // Set default Value
                cb.setValue("mpp");
                cb.setMaxWidth(Double.MAX_VALUE);
                               
                gp.addRow(1, label,field); 
                gp.addRow(2, label1,cb);
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());       
                params.put("subscriptionId", ((TextField) gp.lookup("#subscriptionId")).getText());
                params.put("apiName", ((ChoiceBox<String>) gp.lookup("#apiName")).getValue());
                setParameters(params);
            }

        };
        
        

        Action a210 = new Action("viewServiceInstance","View Service Instance") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                
                ToggleGroup group = new ToggleGroup();               
                RadioButton rb1 = new RadioButton("Use Service Id");
                rb1.setUserData("service");
                rb1.setSelected(true);
                GridPane.setConstraints(rb1, 1, 1);
                RadioButton rb2 = new RadioButton("Use Subscription Id");
                rb2.setUserData("subscription");
                GridPane.setConstraints(rb2, 1, 2);
                group.getToggles().addAll(rb1,rb2);
                
                Label label = new Label("Service Instance Id");
                TextField field = new TextField();
                field.setPromptText("Enter service id");
                field.setId("serviceInstanceId");
                
                Label label1 = new Label("Service Subscription Id");
                TextField field1 = new TextField();
                field1.setPromptText("Enter subscription id");
                field1.setId("serviceSubscriptionId");
                field1.setDisable(true);
                
                Label label2 = new Label("Output Data");
                ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList("detailed","components properties"));
                cb.setId("output");
                // Set default Value
                cb.setValue("detailed");
                cb.setMaxWidth(Double.MAX_VALUE);
                
                
                gp.getChildren().addAll(rb1,rb2);
                gp.addRow(3, label,field);
                gp.addRow(4, label1,field1); 
                gp.addRow(5, label2,cb); 

                group.selectedToggleProperty().addListener((ov,oldv,newv)->{
                    if (newv.getUserData().equals("service")) {
                        field.setDisable(false);
                        field1.setDisable(true);
                    }
                    else if(newv.getUserData().equals("subscription")) {
                        field.setDisable(true);
                        field1.setDisable(false);
                    }
                });
                
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());    
                params.put("serviceInstanceId", ((TextField) gp.lookup("#serviceInstanceId")).getText());
                params.put("serviceSubscriptionId", ((TextField) gp.lookup("#serviceSubscriptionId")).getText());
                params.put("output", ((ChoiceBox<String>) gp.lookup("#output")).getValue());
                setParameters(params);
            }

        };
        
        Action a215 = new Action("viewServiceOfferingBySubscription","View Subscription Blueprint") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                
                Label label = new Label("Subscription Id");
                TextField field = new TextField();
                field.setPromptText("Enter subscription id");
                field.setId("subscriptionId");
                
                Label label1 = new Label("Output Data");
                ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList("detailed","fields"));
                cb.setId("output");
                // Set default Value
                cb.setValue("detailed");
                cb.setMaxWidth(Double.MAX_VALUE);
                
                gp.addRow(1, label,field);
                gp.addRow(2, label1,cb); 
                
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());    
                params.put("subscriptionId", ((TextField) gp.lookup("#subscriptionId")).getText());
                params.put("output", ((ChoiceBox<String>) gp.lookup("#output")).getValue());
                setParameters(params);
            }

        };
        
        
        Action a220 = new Action("viewRequestDetails","View Request Details") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                Label label = new Label("Request Id");
                TextField field = new TextField();
                field.setPromptText("Enter request id");
                field.setId("requestId");
                                                             
                gp.addRow(1, label,field); 

            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());       
                params.put("requestId", ((TextField) gp.lookup("#requestId")).getText());
                setParameters(params);
            }

        };
        
        //Action a900 = new Action("generateToken","Request and Print Consumer Token");
        //a900.addParameter("format", "Token Format (short)");
        
        Action a230 = new Action("printToken","Print CSA Token") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                Label label = new Label("Token format");
                ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList("short","long"));
                cb.setId("format");
                // Set default Value
                cb.setValue("short"); 
                cb.setMaxWidth(Double.MAX_VALUE);
                
                gp.addRow(1, label,cb); 

            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName());       
                params.put("format", ((ChoiceBox<String>) gp.lookup("#format")).getValue());
                setParameters(params);
            }

        };
        
        Action a240 = new Action("getAvailableValues","Get Available Values for Field") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                Label label = new Label("Field Id");
                TextField field = new TextField();
                field.setPromptText("Enter field id (without prefix)");
                field.setId("fieldId");
                
                Label label1 = new Label("Input Field Name");
                TextField field1 = new TextField();
                field1.setPromptText("Enter input field name");
                field1.setId("inputFieldName");
                
                Label label2 = new Label("Input Field Value");
                TextField field2 = new TextField();
                field2.setPromptText("Enter input field value");
                field2.setId("inputFieldValue");
                
                Label label3 = new Label("Output Format");
                ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList("short","long"));
                cb.setId("format");
                // Set default Value
                cb.setValue("short");
                cb.setMaxWidth(Double.MAX_VALUE);
                                                             
                gp.addRow(1, label,field); 
                gp.addRow(2, label1,field1); 
                gp.addRow(3, label2,field2);
                gp.addRow(4, label3,cb);
                
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName()); 
                params.put("fieldId", ((TextField) gp.lookup("#fieldId")).getText());
                params.put("inputFieldName", ((TextField) gp.lookup("#inputFieldName")).getText());
                params.put("inputFieldValue", ((TextField) gp.lookup("#inputFieldValue")).getText());
                params.put("format", ((ChoiceBox<String>) gp.lookup("#format")).getValue());
                setParameters(params);
            }

        };
        

        Action a260 = new Action("updateProcessInstance","Update CSA Process Instance") {           
         
            @Override
            public void buildMyPane(GridPane gp, Stage stage) {
                ObservableList<Node> children = gp.getChildren();
                children.remove(1, children.size());
                
                Label label = new Label("Process Instance Id");
                TextField field = new TextField();
                field.setPromptText("Enter process id");
                field.setId("processInstanceId");
                
                Label label1 = new Label("Process Instance State");
                ChoiceBox<String> cb1 = new ChoiceBox<>(FXCollections.observableArrayList("INITIALIZED","PENDING","READY","ACTIVE","COMPLETED","ERROR","CANCELED"));
                cb1.setValue("COMPLETED");
                cb1.setId("processInstanceState");
                cb1.setMaxWidth(Double.MAX_VALUE);
                
                Label label2 = new Label("Return Code");
                ChoiceBox<String> cb2 = new ChoiceBox<>(FXCollections.observableArrayList("SUCCESS","FAILURE","RUNNING","TIMEOUT"));
                cb2.setValue("SUCCESS");
                cb2.setId("processInstanceReturnCode");
                cb2.setMaxWidth(Double.MAX_VALUE);
                
                Label label3 = new Label("Status");
                TextField field3 = new TextField();
                field3.setPromptText("Enter status information");
                field3.setId("processInstanceStatus");
                                                             
                gp.addRow(1, label,field); 
                gp.addRow(2, label1,cb1); 
                gp.addRow(3, label2,cb2);
                gp.addRow(4, label3,field3);
                
            }
            
            @Override
            public void getParameters(GridPane gp) throws Exception {
                Map<String,String> params = new HashMap<>();
                params.put("action", getName()); 
                params.put("processInstanceId", ((TextField) gp.lookup("#processInstanceId")).getText());
                params.put("processInstanceState", ((ChoiceBox<String>) gp.lookup("#processInstanceState")).getValue());
                params.put("processInstanceReturnCode", ((ChoiceBox<String>) gp.lookup("#processInstanceReturnCode")).getValue());
                params.put("processInstanceStatus", ((TextField) gp.lookup("#processInstanceStatus")).getText());
                setParameters(params);
            }

        };
                 
        actions = Arrays.asList(a100,a150,a200,a215,a210,a220,a230,a240,a260);
    }
     
    
    public String wrapperMethodForGui(Map<String,String> parameters) {
        String out;
        try {
            String action = parameters.get("action");
            Method method = this.getClass().getMethod(action, java.util.Map.class);
            out = (String) method.invoke(this, parameters);
        }
        catch(Exception e) {
            out = processException(e);
        }
        return out;
    }
    
    /*public String readConfiguration(File file) {
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
    }*/
    
    private String decryptPassword(String encpassword) throws Exception {
        Decryptor decryptor = new Decryptor();
        return decryptor.decryptPassword(encpassword);
    }
    
    public void setupConfiguration(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        configuration = new Configuration();
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configuration = mapper.readValue(file, Configuration.class);
    }
    
    /*private void setUpConfiguration(JsonNode root) throws Exception {  
        configuration = new HashMap<>();
        String idmUser = root.get("idmUser").get("name").asText();
        configuration.put("idmUser", new Conf("IDM Transport User",idmUser,"STRING","plain"));
        String idmPassword = decryptPassword(root.get("idmUser").get("password").asText());
        configuration.put("idmPassword", new Conf("IDM Transport Password",idmPassword,"STRING","hidden"));
        String transportUser = root.get("transportUser").get("name").asText();
        configuration.put("transportUser", new Conf("CSA Transport User",transportUser,"STRING","plain"));
        String transportPassword = decryptPassword(root.get("transportUser").get("password").asText());
        configuration.put("transportPassword", new Conf("CSA Transport Password",transportPassword,"STRING","hidden"));
        String csaConsumer = root.get("defaultConsumer").asText();
        configuration.put("csaConsumer", new Conf("Consumer User",csaConsumer,"STRING","plain"));
        String csaConsumerPassword = decryptPassword(root.get(csaConsumer).asText());
        configuration.put("csaConsumerPassword", new Conf("Consumer Password",csaConsumerPassword,"STRING","hidden"));
        String csaTenant = root.get("defaultTenant").asText();
        configuration.put("csaTenant", new Conf("Consumer Tenant",csaTenant,"STRING","plain"));
        String onBehalf = root.get("onBehalf").asText();
        configuration.put("onBehalf", new Conf("Manage On Behalf User",onBehalf,"STRING","plain"));
        String csaAdmin = root.get("defaultPrivilegedUser").asText();
        configuration.put("csaAdmin", new Conf("CSA Privileged User",csaAdmin,"STRING","plain"));
        String csaAdminPassword = decryptPassword(root.get(csaAdmin).asText());
        configuration.put("csaAdminPassword", new Conf("Priviliged Password",csaAdminPassword,"STRING","hidden"));
        String csaAdminOrg = root.get("defaultAdminOrganization").asText();
        configuration.put("csaAdminOrg", new Conf("Provider Organization",csaAdminOrg,"STRING","plain"));       
        String csaServer = root.get("csaAS").get("Server").asText();
        configuration.put("csaServer", new Conf("CSA Server",csaServer,"STRING","plain"));     
        int csaPort = root.get("csaAS").get("Port").asInt();
        configuration.put("csaPort", new Conf("TCP Port",Integer.toString(csaPort),"INT","plain"));  
        String csaProtocol = root.get("csaAS").get("Protocol").asText();
        configuration.put("csaProtocol", new Conf("Transport Protocol",csaProtocol,"STRING","plain")); 
    }
    
    
    Map<String,Conf> getConfiguration() {
        return configuration;
    }
    
    void setConfiguration(Map<String,Conf> configuration) {
        this.configuration = configuration;
    }*/
    
    Configuration getConfiguration() {
        return configuration;
    }
    
    void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
        
    void initCSAClient() throws Exception {
        if (csa == null) {
            screen.outputLog("Initializing CSA Client", true);
            csa = new Net(configuration.getCsaServer(),configuration.getCsaPort(),configuration.getCsaProtocol());
        }
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
        JsonNode offering = getServiceOffering();
        JsonNode request = getRequestOrder();
        
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
    
    JsonNode getRequestOrder() throws Exception {
        JsonNode out = null;
        ObjectMapper mapper = new ObjectMapper();
        if (arguments.get("source").equalsIgnoreCase("file")) {
            String file = getOrderFileName();
            String jc = new String(Files.readAllBytes(Paths.get(file)));
            out = mapper.readTree(jc);
            
        }
        else if (arguments.get("source").equalsIgnoreCase("csa")) {
            out = mapper.readTree(viewSubscriptionOrder());
        }
        return out;    
    }
    
    JsonNode getServiceOffering() throws Exception {
        JsonNode out = null;
        ObjectMapper mapper = new ObjectMapper();
        if (arguments.get("source").equalsIgnoreCase("file")) {
            String file = getOfferingFileName();
            String jc = new String(Files.readAllBytes(Paths.get(file)));
            out = mapper.readTree(jc);
        }
        else if (arguments.get("source").equalsIgnoreCase("csa")) {
            out = mapper.readTree(viewOfferingForSubscription());
        }
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
    
    String orderSubscription() throws Exception {
        initMetaFromExcel();
        String pyl = buildJsonFromExcel();
        initCSAClient();
        String token = requestToken();
        String uri = "/csa/api/mpp/mpp-request/" + meta.get("serviceId") + "?catalogId=" + meta.get("catalogId");
        String ac = "application/json";
        String cnt = "multipart/form-data; boundary=" + BOUNDARY;
        screen.outputLog("ordering subscription", true);
        String output = csa.postHttp(token, null, null, uri, pyl, ac, cnt);
        return output;
    }
    
    public String updateComponentProperty() throws Exception {
        String componentId = arguments.get("componentId");
        if (componentId == null) raiseError("Component \"componentId\" is null");
        String propertyName = arguments.get("propertyName");
        if (propertyName == null) raiseError("Property name \"propertyName\" is null");
        String propertyDisplayName = arguments.get("propertyDisplayName");
        if (propertyDisplayName == null) raiseError("Property display name \"propertyDisplayName\" is null");
        String propertyValueType = arguments.get("propertyValueType");
        if (propertyValueType == null) raiseError("Property value type \"propertyValueType\" is null");
        String propertyValue = arguments.get("propertyValue");
        if (propertyValue == null) raiseError("Property value \"propertyValue\" is null");
        String propertyVisibility = arguments.get("propertyVisibility");
        if (propertyVisibility == null || !(propertyVisibility.equalsIgnoreCase("true") || propertyVisibility.equalsIgnoreCase("false")) ) raiseError("Property visibility attribute \"propertyVisibility\" must be set to true or false");
        initCSAClient();
        String userId = getUserId();
        String uri = "/csa/rest/artifact/" + componentId + "?userIdentifier=" + userId + "&view=propertyinfo&scope=view&_action_=merge&property_values_action_=update";
        String payload = "<ServiceComponent><id>" + componentId + "</id><property><name>" + propertyName + "</name><displayName>" + propertyDisplayName + "</displayName><valueType><name>" + propertyValueType + "</name></valueType><values><value>" + propertyValue + "</value></values><consumerVisible>" + propertyVisibility + "</consumerVisible></property></ServiceComponent>";      
        screen.outputLog("update service component property", true);
        String output = csa.putHttp(null, configuration.getCsaAdminUser(), configuration.getCsaAdminPassword(), uri, payload, "application/xml", "application/xml");
        return output;
    }
    
    String viewOfferingForSubscription() throws Exception {        
        String subscriptionId = arguments.get("subscriptionId");
        if (subscriptionId == null) raiseError("Subscription \"subscriptionId\" is null");
        String subscription = viewSubscriptionDetails(subscriptionId,"mpp");
        ObjectMapper mapper = new ObjectMapper();
        String serviceId = mapper.readTree(subscription).get("serviceId").asText();
        String catalogId = mapper.readTree(subscription).get("catalogId").asText();
        String categoryName = mapper.readTree(subscription).get("category").get("name").asText();
        String uri = String.format("/csa/api/mpp/mpp-offering/%s?catalogId=%s&category=%s",serviceId,catalogId,categoryName);
        String onBehalf = configuration.getCsaOnBehalfConsumer();
        uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
        String token = requestToken();
        screen.outputLog("requesting service offering for subscription", true);
        String out = csa.getHttp(token, null, null, uri, "application/json");
        return out;
    }
    
    
    
    public String viewServiceForSubscription() throws Exception {
        String subscriptionId = arguments.get("subscriptionId");
        if (subscriptionId == null) raiseError("Subscription \"subscriptionId\" is null");
        String sub = viewSubscriptionDetails(subscriptionId,"service");
        ObjectMapper mapper = new ObjectMapper();
        String sid = mapper.readTree(sub).get("ext").get("csa_service_instance_id").asText();
        String out = viewServiceInstanceDetails(sid);
        return out;
    }
    
    
    String viewSubscriptionErrorInfo() throws Exception {
        String subscriptionId = arguments.get("subscriptionId");
        if (subscriptionId == null) raiseError("Subscription \"subscriptionId\" is null");
        String sub = viewSubscriptionDetails(subscriptionId,"service");
        ObjectMapper mapper = new ObjectMapper();
        String sid = mapper.readTree(sub).get("ext").get("csa_service_instance_id").asText();
        String service = viewServiceInstanceDetails(sid);
        return getErrorInfoFromServiceComposite(service);
    }
    
    String getErrorInfoFromServiceComposite(String service) throws Exception {
        screen.outputLog("Retrieving Subscription Error Info from Service Composite", true);
        Map<String,Object> obj = new HashMap<String,Object>();
        //String content = new String(Files.readAllBytes(Paths.get("instance.json")),"UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        ArrayList<Map<String,Object>> components = (ArrayList<Map<String,Object>>) mapper.readValue(service, HashMap.class).get("components");
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
            }
        });       
        return sb.toString();
    }
    
    
    String cancelSubscription() throws Exception {
        String subscriptionId = arguments.get("subscriptionId");
        if (subscriptionId == null) raiseError("Argument subscription \"subscriptionId\" is null");
        initCSAClient();
        String uri = "/csa/api/service/subscription/" + subscriptionId + "/cancel";
        String ac = "*/*";
        String cnt = "application/json"; 
        String payload = "";
        String output = csa.postHttp(null, configuration.getCsaAdminUser(), configuration.getCsaAdminPassword(), uri, payload, ac, cnt);
        return output;
    }
    
    String getUserId() throws Exception {
        String ac = "application/json";
        String uri = "/csa/rest/login/" + configuration.getCsaProviderOrg() + "/" + configuration.getCsaAdminUser() + "/";
        screen.outputLog("Getting CSA user identifier for REST calls", true);
        String output = csa.getHttp(null, configuration.getCsaTransportUser(), configuration.getCsaTransportPassword(), uri, ac);
        ObjectMapper mapper = new ObjectMapper();        
        return mapper.readTree(output).get("id").asText();
    }     
    
    public String viewServiceOfferingBySubscription(Map<String,String> args) throws Exception {
        String subscriptionId = args.get("subscriptionId");
        if (subscriptionId.isEmpty()) throw new RuntimeException("Subscription Id is empty");
        String dataType = args.get("output");       
        String out = viewServiceOfferingBySubscription(subscriptionId,dataType);
        return out;
    }
    
    String viewServiceOfferingBySubscription(String sid, String dataType) throws Exception {        
        ObjectMapper mapper = new ObjectMapper();
        String sub = viewSubscriptionDetails(sid,"mpp");
        // Get Service Meta Data
        String serviceId = mapper.readTree(sub).get("serviceId").asText();
        String catalogId = mapper.readTree(sub).get("catalogId").asText();
        String categoryName = mapper.readTree(sub).get("category").get("name").asText();    
        String sof = viewServiceOffering(serviceId,catalogId,categoryName);
        if (dataType.equalsIgnoreCase("fields")) {
            sof = getServiceOfferingFields(sof);
        }
        return sof;
    }
    
    String viewServiceOffering(String ssid, String catid, String cname) throws Exception {
        String uri = String.format("/csa/api/mpp/mpp-offering/%s?catalogId=%s&category=%s",ssid,catid,cname);
        String onBehalf = configuration.getCsaOnBehalfConsumer();
        uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
        String token = requestToken();
        screen.outputLog("Requesting service offering details", true);
        String out = csa.getHttp(token, null, null, uri, "application/json");      
        return out;
    }
    
    String getServiceOfferingFields(String inso) throws Exception {                   
        screen.outputLog("Retrievieng fields from service offering", true);
        ObjectMapper mapper = new ObjectMapper();
        Iterator<JsonNode> fields = mapper.readTree(inso).get("fields").elements();
        StringBuilder sb = new StringBuilder();
        while (fields.hasNext()) {
            JsonNode field = fields.next();
            sb.append(field.get("id").asText()).append(",");
            sb.append(field.get("name").asText()).append(",");
            String displayName = field.get("displayName").asText();
            sb.append(field.get("displayName").asText()).append(",");
            String value = "";
            if (field.get("value") != null && (field.get("value").getNodeType() == JsonNodeType.STRING)) 
                sb.append("\"").append(field.get("value").asText()).append("\"");
            else if (field.get("value") != null && (field.get("value").getNodeType() == JsonNodeType.NUMBER))
                sb.append(field.get("value").asInt());
            else if (field.get("value") != null && (field.get("value").getNodeType() == JsonNodeType.BOOLEAN))
                sb.append(Boolean.toString(field.get("value").asBoolean()));
            else 
                sb.append("");
            sb.append("\n");
        };
        return sb.toString();
    }
    
    
    
    public String viewServiceInstance(Map<String,String> args) throws Exception {
        String serviceInstanceId = args.get("serviceInstanceId");
        String serviceSubscriptionId = args.get("serviceSubscriptionId");
        String output = args.get("output");
        String out = viewServiceInstance(serviceInstanceId,serviceSubscriptionId,output);
        return out;
    }
    
    String viewServiceInstance(String ssid,String snid,String output) throws Exception {       
        ObjectMapper mapper = new ObjectMapper();
        if (ssid == null || ssid.isEmpty()) {
            String sub = viewSubscriptionDetails(snid,"service");
            ssid = mapper.readTree(sub).get("ext").get("csa_service_instance_id").asText();
        }
        
        String out = viewServiceInstanceDetails(ssid);
        if (output.equalsIgnoreCase("components properties")) {       
            Map<String,Map> components = new HashMap<>();
            screen.outputLog("retrieving service components properties", true);
            mapper.readTree(out).get("components").forEach(item->{
                Map<String,JsonNode> properties = new HashMap<>();
                String dn = item.get("displayName").asText();
                item.get("properties").forEach(props-> {
                    properties.put(props.get("name").asText(), props.get("value").get(0));
                });
                components.put(dn, properties);            
            });
            out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(components);
        }
        return out;
    }
    
    String viewServiceInstanceDetails(String id) throws Exception {
        String acceptContent = "application/json";
        String uri = "/csa/api/mpp/mpp-instance/" + id;
        String onBehalf = configuration.getCsaOnBehalfConsumer();
        uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
        initCSAClient();
        String token = requestToken();
        screen.outputLog("Requesting service instance details", true);
        String output = csa.getHttp(token, null, null, uri, acceptContent);
        return output;
    }
  
    
    public String updateProcessInstance(Map<String,String> args) throws Exception {
        String processInstanceId = args.get("processInstanceId");
        if (processInstanceId == null) throw new RuntimeException("Process Instance Id is empty");
        String processInstanceState = args.get("processInstanceState");
        String processInstanceReturnCode = args.get("processInstanceReturnCode");
        String processInstanceStatus = args.get("processInstanceStatus");
        if (processInstanceStatus == null) throw new RuntimeException("Process Instance Status is empty");
        return updateProcessInstance(processInstanceId,processInstanceState,processInstanceReturnCode,processInstanceStatus);
    }

    
    String updateProcessInstance(String csaProcessId, String processState, String processReturnCode, String processStatus) throws Exception {        
        initCSAClient();
        String userId = getUserId();
        String uri = "/csa/rest/processinstances/" + csaProcessId + "?userIdentifier=" + userId + "&scope=view&view=processinstancestate&action=merge";	
        String payload = "<ProcessInstance><id>"+csaProcessId+"</id><processInstanceState><name>"+processState+"</name></processInstanceState><processReturnCode><name>"+processReturnCode+"</name></processReturnCode><status>"+processStatus+"</status></ProcessInstance>";
        screen.outputLog("Updating Process Instance", true);
        String output = csa.putHttp(null, configuration.getCsaAdminUser(), configuration.getCsaAdminPassword(), uri, payload, "application/xml", "application/xml");
        return output;
    }
    
    public String getAvailableValues(Map<String,String> args) throws Exception {
        String fieldId = args.get("fieldId");
        String format = args.get("format");
        String inputFieldName = args.get("inputFieldName");
        String inputFieldValue = args.get("inputFieldValue");
        return getAvailableValues(fieldId,inputFieldName,inputFieldValue,format);
    }
    
    String getAvailableValues(String fieldId,String inputFieldName,String inputFieldValue, String format) throws Exception {
        String result = "";
        
        String fields = "";
        if (inputFieldName != null && !inputFieldName.isEmpty() && inputFieldValue != null && !inputFieldValue.isEmpty())
            fields = String.format("%s=%s", inputFieldName,inputFieldValue);
        initCSAClient();
        String userId = getUserId();
        String uri = "/csa/rest/availablevalues/" + fieldId + "?userIdentifier=" + userId;
        screen.outputLog("Getting a field available values", true);
        String output = csa.postHttp(null, configuration.getCsaTransportUser(), configuration.getCsaTransportPassword(), uri, fields, "application/json", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        if (format.equalsIgnoreCase("short")) {
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
                result = result + memo;
            }
        }
        else if (format.equalsIgnoreCase("long")) {
            HashMap<String,Object> av = (HashMap<String,Object>) mapper.readValue(output, HashMap.class);
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(av);
        }
        return result;
    }
    
    public String printToken(Map<String,String> args) throws Exception {
        String format = args.get("format");
        String output = printToken(format);
        return output;
    }
    
    String printToken(String format) throws Exception {
        String out = null;
        initCSAClient();
        ObjectMapper mapper = new ObjectMapper();
        screen.outputLog("requesting CSA token", true);
        csa.requestToken(configuration.getCsaIdmUser(), configuration.getCsaIdmPassword(), configuration.getCsaConsumer(), configuration.getCsaConsumerPassword(), configuration.getCsaConsumerTenant());
        String rtoken = csa.getRawToken();
        if (format.equalsIgnoreCase("short")) {            
            out = mapper.readTree(rtoken).get("token").get("id").asText();
        }
        else if (format.equalsIgnoreCase("long")) {
            JsonNode jn = mapper.readTree(rtoken);
            out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jn);
        }
        return out;
    }
    
    public String listSubscriptionsByUserInOrganization(Map<String,String> args) throws Exception {
        String organizationId = args.get("organizationId");
        if (organizationId == null) throw new RuntimeException("Organization Id is empty");
        screen.outputLog("Select Organization Id: " + organizationId, true);
        String output = listSubscriptionsByUserInOrganization(organizationId);
        return output;
    }
    
    String listSubscriptionsByUserInOrganization(String id) throws Exception {
        initCSAClient();
  
        String uri = "/csa/api/person/organization/" + id + "?start-index=0&page-size=30&sort=userName:ascending";
        screen.outputLog("Listing Subscriptions for Organization", true);
        //screen.outputLog(uri, true);
        String payload = "{}";
        String out = csa.postHttp(null, configuration.getCsaAdminUser(), configuration.getCsaAdminPassword(), uri, payload, "application/json", "application/json");
        return out;
    }
    
    public ObservableList<Organization> getCSAOrganizations() {
        ObjectMapper mapper = new ObjectMapper();
        //ObservableList<Map<String,String>> items = FXCollections.observableArrayList();
        List<Organization> items = new ArrayList<>();
        try {
            String out = listCSAOrganizations();
            Iterator<JsonNode> orgs = mapper.readTree(out).get("members").elements();
            while(orgs.hasNext()) {
                JsonNode org = orgs.next();
                String name = org.get("ext").get("csa_name_key").asText();
                String orgid = org.get("@self").asText().replaceFirst("/csa/api/organization/", "");
                items.add(new Organization(name,orgid));
            }
        }
        catch(Exception exp) {
            screen.outputLog(this.processException(exp), true);
        }
        return FXCollections.observableArrayList(items);
    }
    
    String listCSAOrganizations() throws Exception {
        initCSAClient();
        String uri = "/csa/api/organization/";
        screen.outputLog("Listing CSA Organizations", true);
        String out = csa.getHttp(null, configuration.getCsaAdminUser(), configuration.getCsaAdminPassword(), uri, "application/json");
        return out;
    }
    
    
    public String viewRequestDetails(Map<String,String> args) throws Exception {
        String id = args.get("requestId");
        if (id == null) raiseError("Request \"id\" is null");
        String output = viewRequestDetails(id);
        return output;
    }
    
    String viewRequestDetails(String id) throws Exception {
        initCSAClient();
        String token = requestToken();   
        String uri = "/csa/api/mpp/mpp-request/" + id;
        String onBehalf = configuration.getCsaOnBehalfConsumer();
        uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
        screen.outputLog("Getting CSA Request Details", true);
        String out = csa.getHttp(token, null, null, uri, "application/json");
        return out;
    }
    
    //nucleus.dm.Subscription.viewSubscriptionDetails(java.util.HashMap)
    public String viewSubscriptionDetails(Map<String,String> args) throws Exception {
        String id = args.get("subscriptionId");
        if (id == null) throw new RuntimeException("subscription id is empty");
        String api = args.get("apiName");
        String output = viewSubscriptionDetails(id,api);
        return output;
    }
    
    String viewSubscriptionDetails(String id, String api) throws Exception {       
        String uri;
        String output = null;
        String acceptContent = "application/json";
        initCSAClient();
        if (api.equalsIgnoreCase("service")) {
            uri = "/csa/api/service/subscription/" + id;
            String ac = "application/json";
            screen.outputLog("Requesting subscription details with service API", true);
            output = csa.getHttp(null, configuration.getCsaAdminUser(), configuration.getCsaAdminPassword(), uri, acceptContent);
        }
        else {
            uri = "/csa/api/mpp/mpp-subscription/" + id;
            String onBehalf = configuration.getCsaOnBehalfConsumer();
            uri = (onBehalf != null && !onBehalf.isEmpty())? uri + "?onBehalf=" + onBehalf: uri;
            String token = requestToken();
            screen.outputLog("Requesting subscription details with mpp API", true);
            output = csa.getHttp(token, null, null, uri, acceptContent);
        }
        return output;
    }
    
    String viewSubscriptionOrder() throws Exception {
        String subscriptionId = arguments.get("subscriptionId");
        if (subscriptionId == null) raiseError("Subscription \"subscriptionId\" is null");
        String out = viewSubscriptionDetails(subscriptionId,"service");
        ObjectMapper mapper = new ObjectMapper();
        String oid = mapper.readTree(out).get("ext").get("XXXXX").asText();
        return viewRequestDetails(oid);
    }
    
    
    
     
    String requestToken() throws Exception {
        String rtoken = csa.getRawToken();
        if (rtoken == null) {
            screen.outputLog("Requesting CSA Token", true);
            csa.requestToken(configuration.getCsaIdmUser(), configuration.getCsaIdmPassword(), configuration.getCsaConsumer(), configuration.getCsaConsumerPassword(), configuration.getCsaConsumerTenant());
            rtoken = csa.getRawToken();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(rtoken).get("token").get("id").asText();
    }
    
    // 3 implement method
    
    
    void clearToken() {
        if ( csa != null)
            csa.clearToken();
    }
    
    String processException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter pw = new PrintWriter(baos,true);
        e.printStackTrace(pw);
        String out = baos.toString();
        return out;
    }
            
}
