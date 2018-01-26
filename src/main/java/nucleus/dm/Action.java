/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;


/**
 *
 * @author Administrator
 */
public class Action implements ActionInterface {
    
    private String name;
    private String displayName;
    public Map<String,String> parameters;
    
    public Action(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    
   

    String getName() {
        return name;
    }
    
    String getDisplayName() {
        return displayName;
    }

    void setName(String name) {
        this.name = name;
    }
    
    void setParameters(Map<String,String> parameters) {
        this.parameters = parameters;
    }
    
    Map<String,String> getParameters() {
        return this.parameters;
    }
    
    private String processException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter pw = new PrintWriter(baos,true);
        e.printStackTrace(pw);
        String out = baos.toString();
        return out;
    }

    @Override
    public void buildMyPane(GridPane gp, Stage stage) {
        
    }   
        
    @Override
    public void getParameters(GridPane gp) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String runAction(Subscription ss) {
        String out = null;
        try {
            out = ss.wrapperMethodForGui(parameters);
        }
        catch(Exception e) {
            out = processException(e);
        };
        return out;
    }
    
}
