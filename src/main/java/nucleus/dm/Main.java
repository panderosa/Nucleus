/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Administrator
 */
public class Main extends Application {
    
    Subscription sub;
    
    public static void main(String[] args) throws Exception {              
        launch(args);
    }
    
    @Override
    public void init() {
        sub = new Subscription();
    }
    
    @Override
    public void start(Stage primary) throws Exception {
        
        FileChooser fileChooser = new FileChooser();
        Label label1 = new Label("Welcome to first step");
        Button button1 = new Button("Select Configuration File");   
        TextField text = new TextField();

        
        
        VBox layout = new VBox(6,label1,button1,text);
        Scene scene = new Scene(layout,200,200);
        primary.setScene(scene);
        
        
        //Stage browser = new Stage();
        //browser.initModality(Modality.APPLICATION_MODAL);
        
        
        button1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = fileChooser.showOpenDialog(primary);
                    if (file != null) {
                        try {
                            String output = sub.getDefaultConfiguration(file);
                            text.setText(sub.csaProtocol+"//"+sub.csaServer+":"+sub.csaPort);
                        }
                        catch(Exception e) {
                            text.setText(e.toString());
                        };
                    }
            }
        
            
        });
        
        primary.show();
    }
    
  
    
}
