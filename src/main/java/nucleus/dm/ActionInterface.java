/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.util.Map;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author Administrator
 */
interface ActionInterface {
    public void buildMyPane(GridPane gp, Stage stage);
    public void getParameters(GridPane gp) throws Exception;
    public void runAction(Subscription ss) throws Exception;
}
