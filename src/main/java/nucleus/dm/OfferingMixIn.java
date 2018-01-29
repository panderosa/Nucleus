/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public abstract class OfferingMixIn {
    OfferingMixIn(@JsonProperty("name") String name,
                  @JsonProperty("id") String id,
                  @JsonProperty("displayName") String displayName,
                  @JsonProperty("catalogId") String catalogId,
                  @JsonProperty("catalogName") String catalogName,
                  @JsonProperty("offeringVersion") String offeringVersion,
                  @JsonProperty("category") Map<String,String> category) {
    }
}
