/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class Parameter {
    private String displayName;
    private String guiClass;
    private String dataClass;
    private ArrayList<Object> values = null;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGuiClass() {
        return guiClass;
    }

    public void setGuiClass(String guiClass) {
        this.guiClass = guiClass;
    }

    public String getDataClass() {
        return dataClass;
    }

    public void setDataClass(String dataClass) {
        this.dataClass = dataClass;
    }

    public ArrayList<Object> getValues() {
        return values;
    }

    public void setValues(ArrayList<Object> values) {
        this.values = values;
    }
    
    
}
