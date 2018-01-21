package nucleus.dm;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class AlertBox {
    public static void display(String msg) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Debug Console");
        window.setMinWidth(250);
        
        TextField t1 = new TextField();
        t1.setText(msg);
        Button closebutton = new Button("Close me!");
        closebutton.setOnAction(e->window.close());
        
        VBox layout = new VBox(200);
        layout.getChildren().addAll(t1,closebutton);
        layout.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
                
    }
}
