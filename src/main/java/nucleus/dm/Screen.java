/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
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
    
    private Subscription sub;
    private Button button1;
    private ObservableList<Action> observableActions;
    private GridPane parametersPane;
    private GridPane configurationPane;
    private TextArea area;
    Action currentAction;
    private Map<String,Conf> currentConfiguration;
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override
    public void init() {
        sub = new Subscription();
        observableActions = FXCollections.observableArrayList(sub.getActions());   
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        BorderPane border = new BorderPane();
        HBox hbox = addHBox();
        border.setTop(hbox);
        border.setLeft(addVBox());
        border.setCenter(addParametersPane());
        border.setRight(addConfigurationPane());
        border.setBottom(addBottom());
        Scene scene = new Scene(border);
        scene.getStylesheets().add("styleSheets.css");
        button1.setOnAction(e->{
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                String txt = sub.readConfiguration(file);
                currentConfiguration = sub.getConfiguration();
                updateConfigurationPane();
                area.appendText(txt);    
            }                    
        });
        
        stage.setScene(scene);                   
        stage.setTitle("DADA");
        stage.show();
    }
    
    public HBox addHBox() {
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15,12,15,12));
        hbox.setSpacing(10);
        hbox.getStyleClass().add("hbox");
        //hbox.setStyle("-fx-background-color: #336699;");
        button1 = new Button("Load Configuration");
        button1.setPrefSize(150, 20);
        Button button2 = new Button("Run Action");
        button2.setPrefSize(150, 20);
        
        
        
        button2.setOnAction(e->{
            String[] msg = processParameters();
            String out = sub.wrapperMethodForGui(msg);
            area.setText(out);
            //String txt = sub.wrapperMethod(msg);
            //AlertBox.display(msg);
        });
        
        hbox.getChildren().addAll(button1,button2);
        return hbox;
    }
    
    private String[] processParameters() {
        List<String> params = new ArrayList<>();
        params.add(String.format("action=%s", currentAction.getName()));       
        Iterator<Entry<String,String>> p = currentAction.getParameters().entrySet().iterator();        
        while(p.hasNext()) {
            Entry<String,String> pe = p.next();
            String ni = pe.getKey();
            System.out.println(ni);
            TextField tf = (TextField) parametersPane.lookup("#"+ni);
            if (tf != null) {
                params.add(String.format("%s=%s", ni,tf.getText()));
            }
           
        }
        String[] strArr = null;
        strArr = params.toArray(new String[params.size()]);
        return strArr;
    }
    
    
    public VBox addVBox() {
        VBox vbox = new VBox();
        Label title = new Label("List of Actions");
        title.getStyleClass().add("title");
        ListView<Action> actionlist = new ListView<Action>(observableActions);
        actionlist.setPrefHeight(200);
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
            System.out.println(newAction);
            currentAction = newAction;
            updateParametersPane(newAction);

        }); 
        
        VBox.setMargin(title, new Insets(5));
        VBox.setMargin(actionlist, new Insets(5));
        
        vbox.getChildren().add(title);
        vbox.getChildren().add(actionlist);
        vbox.setPrefWidth(250);
        vbox.setPrefHeight(300);
        vbox.getStyleClass().add("vbox");
        return vbox;
    }
    
    public GridPane addParametersPane() {
        parametersPane = new GridPane();
        parametersPane.setPadding(new Insets(10));
        parametersPane.setVgap(5);
        parametersPane.setHgap(10);
        parametersPane.getStyleClass().add("grid");
        Label title = new Label("Action Parameters");
        title.getStyleClass().add("title");
        GridPane.setConstraints(title, 0, 0, 2, 1);
        parametersPane.getChildren().add(0, title);
        //parametersPane.setPrefWidth(500);
        //parametersPane.setGridLinesVisible(true);
        return parametersPane;
    }
    
    public void updateParametersPane(Action a) {
        ObservableList<Node> children = parametersPane.getChildren();                
        children.remove(1, children.size());          
        int i = 1;
        Iterator<Entry<String,String>> as = a.getParameters().entrySet().iterator();
        while(as.hasNext()) {
            Entry<String,String> item = as.next();
            Label label = new Label(item.getValue());
            TextField field = new TextField();
            field.setId(item.getKey());
            //field.setPromptText(item.getValue());
            parametersPane.addRow(i, label,field);
            i++;
        }
    }
    
    public GridPane addConfigurationPane() {
        configurationPane = new GridPane();
        configurationPane.setPadding(new Insets(10));
        configurationPane.setVgap(5);
        configurationPane.setHgap(10);
        configurationPane.getStyleClass().add("grid");
        Label title = new Label("Infrastructure Configuration");
        title.getStyleClass().add("title");
        GridPane.setConstraints(title, 0, 0, 2, 1);
        configurationPane.getChildren().add(0, title);
        //parametersPane.setPrefWidth(500);
        return configurationPane;
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
            TextField field = new TextField();
            field.setId(key);
            field.setText(item.getValue());
            configurationPane.addRow(i, label,field);
            i++;
        }
        Button button = new Button("Apply");
        GridPane.setHalignment(button, HPos.RIGHT);
        configurationPane.add(button, 1, i);
        
        button.setOnAction( v -> {
            currentConfiguration.forEach((id,cf)->{
                TextField tf = (TextField) configurationPane.lookup("#"+id);
                String value = "";
                if (tf != null)
                    value = tf.getText();
                cf.setValue(value);
            });
            sub.setConfiguration(currentConfiguration);
        });
    }
    
    public BorderPane addBottom() {
        BorderPane bp = new BorderPane();
        
        area = new TextArea();
        area.setPrefWidth(800);
        area.setPrefHeight(400);
        area.setWrapText(true);
        area.setEditable(false);
        //area.setMaxWidth(2000);
        
        area.textProperty().addListener((o,oldv,newv)->{
            area.setScrollTop(Double.MIN_VALUE);
        });
        
        
        bp.setCenter(area);
        
        return bp;
    }
    
    private String processException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter pw = new PrintWriter(baos,true);
        e.printStackTrace(pw);
        String out = baos.toString();
        return out;
    }
}
