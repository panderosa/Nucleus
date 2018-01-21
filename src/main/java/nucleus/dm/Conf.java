/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

/**
 *
 * @author Administrator
 */
public class Conf {
    String displayName;
    String value;
    String type;
    String guiType;

    public Conf(String displayName, String value, String type, String guiType) {
        this.displayName = displayName;
        this.value = value;
        this.type = type;
        this.guiType = guiType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGuiType() {
        return guiType;
    }

    public void setGuiType(String guiType) {
        this.guiType = guiType;
    }
    
    
}
