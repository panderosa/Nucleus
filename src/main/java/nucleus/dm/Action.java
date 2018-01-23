/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.util.HashMap;
import javafx.util.StringConverter;


/**
 *
 * @author Administrator
 */
public class Action {
    
    private String name;
    private String displayName;
    private String description;
    private HashMap<String,String> parameters = new HashMap<>();

    public Action(String name,String displayName) {
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

    HashMap<String, String> getParameters() {
        return parameters;
    }

    void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
    
    void addParameter(String a, String b) {
        this.parameters.put(a, b);
    }
    
}
