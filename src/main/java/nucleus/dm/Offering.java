/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.util.Map;

/**
 *
 * @author Administrator
 */
public class Offering {
    String name;
    String id;
    String displayName;
    String catalogId;
    String catalogName;
    String offeringVersion;
    Map<String,String> category;

    public Offering(String name, String id, String displayName, String catalogId, String catalogName, String offeringVersion,Map<String,String> category) {
        this.name = name;
        this.id = id;
        this.displayName = displayName;
        this.catalogId = catalogId;
        this.catalogName = catalogName;
        this.offeringVersion = offeringVersion;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public String toString() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getOfferingVersion() {
        return offeringVersion;
    }

    public void setOfferingVersion(String offeringVersion) {
        this.offeringVersion = offeringVersion;
    }

    public Map<String, String> getCategory() {
        return category;
    }

    public void setCategory(Map<String, String> category) {
        this.category = category;
    }
    
}
