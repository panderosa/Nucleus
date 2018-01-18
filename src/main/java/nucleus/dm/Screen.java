/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;


import java.io.File;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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
import jxl.format.Border;


/**
 *
 * @author Administrator
 */
public class Screen extends Application {
    
    Subscription sub;
    Button button1;
    ListView<String> actionlist;
    GridPane gridpane;
    TextArea area;
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override
    public void init() {
        sub = new Subscription();
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        BorderPane border = new BorderPane();
        HBox hbox = addHBox();
        border.setTop(hbox);
        border.setLeft(addVBox());
        border.setCenter(addGridPane());
        border.setBottom(addScroll());
        Scene scene = new Scene(border);
        scene.getStylesheets().add("styleSheets.css");
        stage.setScene(scene);       
        
        actionlist.getSelectionModel().selectedItemProperty().addListener((ob,ov,nv)->{
            System.out.println(nv);
            updateGridLine(nv);
        });  
        
        button1.setOnAction(e->{
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                String txt = sub.readFile(file);
                area.setText(txt);
            }                    
        });
        
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
        Button button2 = new Button("Clear Configuration");
        button2.setPrefSize(150, 20);
        hbox.getChildren().addAll(button1,button2);
        
        return hbox;
    }
    
    public VBox addVBox() {
        VBox vbox = new VBox();
        Label title = new Label("List of Actions");
        title.getStyleClass().add("title");
        actionlist = new ListView<String>();
        ObservableList<String> names = FXCollections.observableArrayList("Order Subscription","View Subscription Details","Get Available Values","View Request Details","View Subscription Error","View Service Properties","Update Component Property Value","Update Process Instance");
        actionlist.setItems(names);
        actionlist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        //actionlist.getSelectionModel().select("View Subscription Details");        
        actionlist.setPrefHeight(200);
        VBox.setMargin(title, new Insets(5));
        VBox.setMargin(actionlist, new Insets(5));
        
        vbox.getChildren().add(title);
        vbox.getChildren().add(actionlist);
        vbox.setPrefWidth(250);
        vbox.setPrefHeight(300);
        vbox.getStyleClass().add("vbox");
        return vbox;
    }
    
    public GridPane addGridPane() {
        gridpane = new GridPane();
        gridpane.setVgap(10);
        gridpane.setHgap(10);
        gridpane.getStyleClass().add("grid");
        Label title = new Label("Action Parameters");
        title.getStyleClass().add("title");
        GridPane.setConstraints(title, 0, 0, 2, 1);
        gridpane.getChildren().add(0, title);
        gridpane.setPrefWidth(500);
        //gridpane.setGridLinesVisible(true);
        return gridpane;
    }
    
    public void updateGridLine(String a) {
        ObservableList<Node> children = gridpane.getChildren();                
        
        //GridPane.clearConstraints(gridpane.getChildren().get(1));
        //GridPane.clearConstraints(gridpane.getChildren().get(2));
        children.remove(1, children.size());
        
        Label nl = new Label(a);
        nl.setPrefWidth(200);
        TextField tf = new TextField();
        tf.setPromptText(a);
        tf.setPrefWidth(200);
        gridpane.addRow(1, nl,tf);
    }
    
    public ScrollPane addScroll() {
        ScrollPane scrollpane = new ScrollPane();
        
        area = new TextArea();
        area.setPrefWidth(800);
        area.setPrefHeight(600);
        area.setWrapText(true);
        area.setMaxWidth(2000);
        
        scrollpane.setContent(area);
        scrollpane.setFitToWidth(true);
        scrollpane.setPrefWidth(800);
        scrollpane.setPrefHeight(600);
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollpane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        Group root = new Group();
        root.getChildren().add(scrollpane);
        return scrollpane;
    }
    
    
    
}
