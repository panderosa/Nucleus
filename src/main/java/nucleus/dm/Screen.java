/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
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
import jxl.format.Border;


/**
 *
 * @author Administrator
 */
public class Screen extends Application {
    
    private Stage window;
    private Subscription sub;
    private Button button1;
    private ObservableList<Action> observableActions;
    private HBox topPane;
    private VBox actionPane;
    private GridPane parametersPane;
    private GridPane configurationPane;
    private BorderPane outputPane;
    private TextArea outputArea;
    private TextArea logArea;
    Action currentAction;
    private Map<String,Conf> currentConfiguration;
    
    public static void main(String[] args) {
        launch();
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
        button1.setOnAction(e->{
            File file = fileChooser.showOpenDialog(window);
            if (file != null) {
                String txt = sub.readConfiguration(file);
                currentConfiguration = sub.getConfiguration();
                updateConfigurationPane();
                outputText(txt);  
            }                    
        });
        
        window.setScene(scene);                   
        window.setTitle("DADA");
        window.show();
    }
    
    public HBox addTopPane() {
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15,12,15,12));
        hbox.setSpacing(10);
        hbox.getStyleClass().add("hbox");
        //hbox.setStyle("-fx-background-color: #336699;");
        button1 = new Button("Load Configuration");
        button1.setPrefSize(150, 20);
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
                            String out = currentAction.runAction(sub);
                            outputText(out);
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
        
        hbox.getChildren().addAll(button1,button2,button3,button4);
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
        
        actionlist.getSelectionModel().selectedItemProperty().addListener((ob,oldAction,newAction)->{
            //System.out.println(newAction);
            currentAction = newAction;
            updateParametersPane(newAction);

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
        pp.setPadding(new Insets(10));
        pp.setVgap(5);
        pp.setHgap(10);
        pp.getStyleClass().add("grid");
        Label title = new Label("Action Parameters");
        title.getStyleClass().add("title");
        GridPane.setConstraints(title, 0, 0, 2, 1);
        pp.getChildren().add(0, title);
        ColumnConstraints column1 = new ColumnConstraints(150);
        ColumnConstraints column2 = new ColumnConstraints(200);
        pp.getColumnConstraints().addAll(column1,column2);
        pp.setAlignment(Pos.TOP_CENTER);
        return pp;
    }
    
    public void updateParametersPane(Action a) {
        a.buildMyPane(parametersPane, window);
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
        cp.setAlignment(Pos.TOP_CENTER);
        return cp;
    }
    
    public void updateConfigurationPane() {
        ObservableList<Node> children = configurationPane.getChildren();
        children.remove(1, children.size());
        int i = 1;
        Iterator<String> keys = currentConfiguration.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            Conf item = currentConfiguration.get(key);
            Label label = new Label(item.getDisplayName());
            if (item.getGuiType().equalsIgnoreCase("plain")) {
                TextField field = new TextField();
                field.setId(key);
                field.setText(item.getValue());
                configurationPane.addRow(i, label,field);
            }
            else if (item.getGuiType().equalsIgnoreCase("hidden")) {
                PasswordField pw = new PasswordField();
                pw.setId(key);
                pw.setText(item.getValue());
                configurationPane.addRow(i, label,pw);
            }
            i++;
        }
        Button button = new Button("Apply");
        GridPane.setHalignment(button, HPos.RIGHT);
        configurationPane.add(button, 1, i);
        
        button.setOnAction( v -> {
            currentConfiguration.forEach((id,cf)->{
                String value = "";
                Node node = configurationPane.lookup("#"+id);
                if (node != null) {
                    if (cf.getGuiType().equalsIgnoreCase("plain")) {
                        value = ((TextField) node).getText();
                        System.out.println(value);
                    }
                    else if (cf.getGuiType().equalsIgnoreCase("hidden")) {
                        value = ((PasswordField) node).getText();
                        System.out.println(value);
                    }
                    cf.setValue(value);
                    currentConfiguration.put(id, cf);
                }
            });
            sub.setConfiguration(currentConfiguration);
        });
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
            StringBuilder sb = new StringBuilder();
            Method[] methods = sub.getClass().getMethods();
            for (Method method: methods) {
                String line;
                String name = method.getName();
                line = name;
                Class[] parameters = method.getParameterTypes();
                for(Class parameter: parameters) {
                    String mn = parameter.getName();
                    line = String.format("%s %s", line, mn);
                }
                line = line + "\n";
                this.outputText(line);
            }
        });
        
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(5)); 
        
        logArea = new TextArea();
        logArea.getStyleClass().add("log-area");
        
        outputArea = new TextArea();
        outputArea.setPrefWidth(800);
        outputArea.setPrefHeight(400);
        outputArea.setWrapText(true);
        outputArea.setEditable(false);
        outputArea.getStyleClass().add("outputArea");
        //outputArea.setMaxWidth(2000);
        
        vbox.getChildren().addAll(logArea,outputArea);
        
        
        outputArea.textProperty().addListener((o,oldv,newv)->{
            outputArea.setScrollTop(Double.MIN_VALUE);
        });
        
        bp.setTop(hbox);
        bp.setCenter(vbox);
        return bp;
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
