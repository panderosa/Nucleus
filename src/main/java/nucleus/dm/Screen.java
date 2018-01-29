/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Iterator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Screen extends Application {
    
    private Stage window;
    private Subscription sub;
    private ObservableList<Action> observableActions;
    private HBox topPane;
    private VBox actionPane;
    private GridPane parametersPane;
    private GridPane configurationPane;
    private BorderPane outputPane;
    private TextArea outputArea = null;
    private TableView table = null;
    private TextArea logArea;
    Action currentAction;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void init() {
        sub = new Subscription(this);
        observableActions = FXCollections.observableArrayList(sub.getActions());   
    }
    
   
    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        FileChooser fileChooser = new FileChooser();
        BorderPane border = new BorderPane();
        topPane = addTopPane();
        actionPane = addActionsPane();
        parametersPane = addParametersPane();
        configurationPane = addConfigurationPane();
        outputPane = addBottom();
        
        border.setTop(topPane);
        border.setLeft(actionPane);
        border.setCenter(parametersPane);
        border.setRight(configurationPane);
        border.setBottom(outputPane);
        
        Scene scene = new Scene(border);
        scene.getStylesheets().add("styleSheets.css");
        
        
        window.setScene(scene);                   
        window.setTitle("DADA");
        window.show();
    }
    
    public HBox addTopPane() {
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15,12,15,12));
        hbox.setSpacing(10);
        hbox.getStyleClass().add("hbox");
        Button button2 = new Button("Run Action");
        button2.setPrefSize(150, 20);
        Button button3 = new Button("Parse JSON File");
        button3.setPrefSize(150, 20);
        Button button4 = new Button("Clear Token");
        button4.setPrefSize(150, 20);
        
        button2.setOnAction(e->{
                Task task = new Task<Void>() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            currentAction.getParameters(parametersPane);
                            currentAction.runAction(sub);
                        }
                        catch(Exception exp) {
                            String out = processException(exp);
                            outputLog(out, true);
                        }
                        return null;
                    }
                };
                new Thread(task).start();
        });
        
        button3.setOnAction(e->{
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(window);
            if(file != null) {
                String out = parseJSON(file);
                outputText(out);
            }
        });
        
        button4.setOnAction(e->{
            sub.clearToken();
        });
        
        hbox.getChildren().addAll(button2,button3,button4);
        return hbox;
    }
    
    
    public VBox addActionsPane() {
        VBox vbox = new VBox();
        Label title = new Label("List of Actions");
        title.getStyleClass().add("title");
        ListView<Action> actionlist = new ListView<Action>(observableActions);
        actionlist.setPrefHeight(400);
        actionlist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); 
        actionlist.setCellFactory(new Callback<ListView<Action>,ListCell<Action>>() {
            @Override
            public ListCell<Action> call(ListView<Action> param) {
                ListCell<Action> cell = new ListCell<Action>() {
                    @Override
                    protected void updateItem(Action t, boolean bln) {
                        super.updateItem(t, bln);
                        if ( t != null) {
                            setText(t.getDisplayName());
                        }
                    }
                };
                return cell;
            };
        });
        
        actionlist.getSelectionModel().selectedItemProperty().addListener((ob,oa,na)->{
            //System.out.println(newAction);
            currentAction = na;
            na.buildMyPane(parametersPane, window);
        }); 
        
        VBox.setMargin(title, new Insets(5));
        VBox.setMargin(actionlist, new Insets(5));
        
        vbox.getChildren().add(title);
        vbox.getChildren().add(actionlist);
        vbox.setPrefWidth(400);
        vbox.setPrefHeight(500);
        vbox.getStyleClass().add("vbox");
        return vbox;
    }
    
    public GridPane addParametersPane() {
        GridPane pp  = new GridPane();
        //pp.setPrefWidth(Double.MAX_VALUE);
        
        pp.setAlignment(Pos.TOP_LEFT);
        pp.setPadding(new Insets(10));
        pp.setVgap(5);
        pp.setHgap(5);
        pp.getStyleClass().add("grid");
        //Label title = new Label("Action Parameters");
        //title.getStyleClass().add("title");
        //GridPane.setConstraints(title, 0, 0, 2, 1);
        //pp.getChildren().add(0, title);
        ColumnConstraints column1 = new ColumnConstraints(120);
        ColumnConstraints column2 = new ColumnConstraints(200);
        ColumnConstraints column3 = new ColumnConstraints(100);
        
        Label title = new Label("Action Parameters");
        title.getStyleClass().add("title");
        GridPane.setConstraints(title, 0, 0, 3, 1);
        pp.getChildren().add(0, title);
        
        pp.getColumnConstraints().addAll(column1,column2,column3);
        pp.setAlignment(Pos.TOP_LEFT);
        return pp;
    }
    
    public GridPane addConfigurationPane() {
        GridPane cp = new GridPane();
        cp.setPadding(new Insets(10));
        cp.setVgap(5);
        cp.setHgap(10);
        cp.getStyleClass().add("grid");
        Label title = new Label("Infrastructure Configuration");
        title.getStyleClass().add("title");
        GridPane.setConstraints(title, 0, 0, 2, 1);
        cp.getChildren().add(0, title);
        ColumnConstraints column1 = new ColumnConstraints(150);
        ColumnConstraints column2 = new ColumnConstraints(200);
        cp.getColumnConstraints().addAll(column1,column2);
        
        Label label1 = new Label("IDM Transport User");
        TextField field1 = new TextField();
        field1.setId("csaIdmUser");
        cp.addRow(1, label1,field1);
        
        Label label2 = new Label("IDM User Password");
        PasswordField pf2 = new PasswordField();
        pf2.setId("csaIdmPassword");
        cp.addRow(2, label2,pf2);
        
        Label label3 = new Label("CSA Transport User");
        TextField field3 = new TextField();
        field3.setId("csaTransportUser");
        cp.addRow(3, label3,field3); 

        Label label4 = new Label("Transport User Password");
        PasswordField pf4 = new PasswordField();
        pf4.setId("csaTransportPassword");
        cp.addRow(4, label4,pf4);
        
        Label label5 = new Label("CSA Default Consumer");
        TextField field5 = new TextField();
        field5.setId("csaConsumer");
        cp.addRow(5, label5,field5); 

        Label label6 = new Label("CSA Consumer Password");
        PasswordField pf6 = new PasswordField();
        pf6.setId("csaConsumerPassword");
        cp.addRow(6, label6,pf6);
        
        Label label7 = new Label("CSA Consumer Tenant");
        TextField field7 = new TextField();
        field7.setId("csaConsumerTenant");
        cp.addRow(7, label7,field7);
        
        Label label8 = new Label("Manage OnBehalf User");
        TextField field8 = new TextField();
        field8.setId("csaOnBehalfConsumer");
        cp.addRow(8, label8,field8);
        
        Label label9 = new Label("CSA Administrator");
        TextField field9 = new TextField();
        field9.setId("csaAdminUser");
        cp.addRow(9, label9,field9); 

        Label label10 = new Label("CSA Administrator Password");
        PasswordField pf10 = new PasswordField();
        pf10.setId("csaAdminPassword");
        cp.addRow(10, label10,pf10);
        
        Label label11 = new Label("CSA Provider Org");
        TextField field11 = new TextField();
        field11.setId("csaProviderOrg");
        cp.addRow(11, label11,field11); 

        Label label12 = new Label("CSA Server");
        TextField field12 = new TextField();
        field12.setId("csaServer");
        cp.addRow(12, label12,field12); 
        
        Label label13 = new Label("CSA Transport Protocol");
        TextField field13 = new TextField();
        field13.setId("csaProtocol");
        cp.addRow(13, label13,field13); 
        
        Label label14 = new Label("CSA TCP Port");
        TextField field14 = new TextField();
        field14.setId("csaPort");
        cp.addRow(14, label14,field14);
        
        Button button1 = new Button("Load From File");
        button1.setId("loadConfiguration");
        GridPane.setHalignment(button1, HPos.LEFT);
        cp.add(button1, 1, 15);
        
        Button button2 = new Button("Apply Changes");
        button2.setId("apply");
        GridPane.setHalignment(button2, HPos.RIGHT);
        cp.add(button2, 1, 15);

        
        cp.setAlignment(Pos.TOP_CENTER);
        
        button1.setOnAction(e->{
            FileChooser fc = new FileChooser();
            File file = fc.showOpenDialog(window);
            if (file != null) {
                /*String txt = sub.readConfiguration(file);
                configuration = sub.getConfiguration();*/
                try {
                    sub.setupConfiguration(file);
                    Configuration configuration = sub.getConfiguration();
                    updateConfigurationPane(configuration);
                }
                catch(Exception exp) {
                    outputLog(processException(exp),true);
                }
            }                    
        });
        
        button2.setOnAction( v -> {
            Configuration configuration = sub.getConfiguration();
            configuration = updateConfiguration(configuration);
            sub.setConfiguration(configuration);
        });
        
        return cp;
    }
    
    Configuration updateConfiguration (Configuration configuration) {
    ObservableList<Node> lista = configurationPane.getChildren();
        for(Node item: lista) {
            if(item.getId() != null) {
                switch (item.getId()) {
                    case "csaIdmUser":
                        configuration.setCsaIdmUser(((TextField) item).getText());
                        break;
                    case "csaIdmPassword":
                        configuration.setCsaIdmPassword(((PasswordField) item).getText());
                        break;
                    case "csaTransportUser": 
                        configuration.setCsaTransportUser(((TextField) item).getText());
                        break;
                    case "csaTransportPassword":
                        configuration.setCsaTransportPassword(((PasswordField) item).getText());
                        break;
                    case "csaConsumer": 
                        configuration.setCsaConsumer(((TextField) item).getText());
                        break;
                    case "csaConsumerPassword":
                        configuration.setCsaConsumerPassword(((PasswordField) item).getText());
                        break;  
                    case "csaConsumerTenant": 
                        configuration.setCsaConsumerTenant(((TextField) item).getText());
                        break;    
                    case "csaOnBehalfConsumer": 
                        configuration.setCsaOnBehalfConsumer(((TextField) item).getText());
                        break;      
                    case "csaAdminUser": 
                        configuration.setCsaAdminUser(((TextField) item).getText());
                        break;
                    case "csaAdminPassword":
                       configuration.setCsaAdminPassword(((PasswordField) item).getText());
                        break; 
                    case "csaProviderOrg": 
                        configuration.setCsaProviderOrg(((TextField) item).getText());
                        break;
                    case "csaServer": 
                        configuration.setCsaServer(((TextField) item).getText());
                        break;
                    case "csaProtocol": 
                        configuration.setCsaProtocol(((TextField) item).getText());
                        break; 
                    case "csaPort": 
                        configuration.setCsaPort(Integer.parseInt(((TextField) item).getText()));
                        break;     
                        
                }
            }
        }
        return configuration;
    }
    
    void updateConfigurationPane(Configuration configuration) {
        ObservableList<Node> lista = configurationPane.getChildren();
        for(Node item: lista) {
            if(item.getId() != null) {
                switch (item.getId()) {
                    case "csaIdmUser":
                        ((TextField) item).setText(configuration.getCsaIdmUser());
                        break;
                    case "csaIdmPassword":
                        ((PasswordField) item).setText(configuration.getCsaIdmPassword());
                        break;
                    case "csaTransportUser": 
                        ((TextField) item).setText(configuration.getCsaTransportUser());
                        break;
                    case "csaTransportPassword":
                        ((PasswordField) item).setText(configuration.getCsaTransportPassword());
                        break;
                    case "csaConsumer": 
                        ((TextField) item).setText(configuration.getCsaConsumer());
                        break;
                    case "csaConsumerPassword":
                        ((PasswordField) item).setText(configuration.getCsaConsumerPassword());
                        break;  
                    case "csaConsumerTenant": 
                        ((TextField) item).setText(configuration.getCsaConsumerTenant());
                        break;    
                    case "csaOnBehalfConsumer": 
                        ((TextField) item).setText(configuration.getCsaOnBehalfConsumer());
                        break;      
                    case "csaAdminUser": 
                        ((TextField) item).setText(configuration.getCsaAdminUser());
                        break;
                    case "csaAdminPassword":
                        ((PasswordField) item).setText(configuration.getCsaAdminPassword());
                        break; 
                    case "csaProviderOrg": 
                        ((TextField) item).setText(configuration.getCsaProviderOrg());
                        break;
                    case "csaServer": 
                        ((TextField) item).setText(configuration.getCsaServer());
                        break;
                    case "csaProtocol": 
                        ((TextField) item).setText(configuration.getCsaProtocol());
                        break; 
                    case "csaPort": 
                        ((TextField) item).setText(Integer.toString(configuration.getCsaPort()));
                        break;     
                        
                }
            }
        }
    }
    
    
    public BorderPane addBottom() {
        BorderPane bp = new BorderPane();
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5));
        Button bclearL = new Button("Clear Log");
        Button bclearO = new Button("Clear Output");
        Button bsave = new Button("Save to file");
        Button testMe = new Button("testMe");
        hbox.getChildren().addAll(bclearL,bclearO,bsave,testMe);
        
        bclearL.setOnAction(e->{
            logArea.clear();
        });
        
        bclearO.setOnAction(e->{
            outputArea.clear();
        });
        
        bsave.setOnAction(e->{           
            FileChooser fc = new FileChooser();
            File file = fc.showSaveDialog(window);
            try {
                if (file != null) {
                    String out = getOutput();
                    FileWriter fw = new FileWriter(file);
                    fw.write(out);
                    fw.close();
                }
            }
            catch(Exception exp) {
                outputLog(processException(exp),true);
            }
        });
        
        testMe.setOnAction(e->{
           try {
               String out = sub.filterOfferings(null, null, null);
               this.outputText(out);
           }
           catch(Exception exp) {
               this.outputLog(this.processException(exp), true);
           }
        });
        
        VBox vbox = new VBox();
        vbox.setId("output");
        vbox.setPadding(new Insets(5)); 
        
        logArea = new TextArea();
        logArea.setId("log");
        logArea.getStyleClass().add("log-area");

        outputArea = new TextArea();    
        outputArea.setPrefWidth(800);
        outputArea.setPrefHeight(400);
        outputArea.setWrapText(true);
        outputArea.setEditable(false);
        outputArea.setId("text");
        outputArea.getStyleClass().add("outputArea");
        outputArea.textProperty().addListener((o,oldv,newv)->{
            outputArea.setScrollTop(Double.MIN_VALUE);
        });
        
        vbox.getChildren().add(0, logArea);
        vbox.getChildren().add(1, outputArea); 
        
        bp.setTop(hbox);
        bp.setCenter(vbox);
        return bp;
    }
    
    
    void setTableView(TableView table) {
        this.table = table;  
    }
    
    void switchOutput(String format) {
        ObservableList<Node> children = ((VBox) outputPane.lookup("#output")).getChildren();
        children.remove(1);
        switch(format) {
            case "table":
                children.add(1, table);
                break;
           case "plain":
                children.add(1, outputArea);
               break;
       }
    }
    
    void setToPlain() {
        ObservableList<Node> children = ((VBox) outputPane.lookup("#output")).getChildren();
        if(!children.get(1).getClass().toString().equalsIgnoreCase("javafx.scene.control.TextArea")) {
            children.remove(1);
            TextArea ta = new TextArea();    
            ta.setPrefWidth(800);
            ta.setPrefHeight(400);
            ta.setWrapText(true);
            ta.setEditable(false);
            ta.setId("text");
            ta.getStyleClass().add("outputArea");
            ta.textProperty().addListener((o,oldv,newv)->{
                ta.setScrollTop(Double.MIN_VALUE);
            });
            outputArea = ta;
            children.add(1, outputArea);
        }
    }
     
    void fillTable(ObservableList<PersonSubscriptions> data) {
        table.setItems(data);
    }
    
    
    private String processException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter pw = new PrintWriter(baos,true);
        e.printStackTrace(pw);
        String out = baos.toString();
        return out;
    }
    
    private String printTimestamp() {
        return String.format("-------------- %1$tH:%1$tM:%1$tS %1$tY/%1$tm/%1$td ---------------%n", Calendar.getInstance());
    }
    
    void outputLog(String txt, boolean useTimestamp) {
        if (useTimestamp)
            logArea.appendText(printTimestamp());
        logArea.appendText(txt);
        logArea.appendText("\n");
    }
    
    String getOutput() {
        return outputArea.getText();
    }
    
    void outputText(String txt) {        
        outputArea.appendText(txt);
    }
    
    String parseJSON(File file) {
        String out;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(file);
            out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        }
        catch(Exception e) {
            out = processException(e);
        }
        return out;
    }
    
    
}
