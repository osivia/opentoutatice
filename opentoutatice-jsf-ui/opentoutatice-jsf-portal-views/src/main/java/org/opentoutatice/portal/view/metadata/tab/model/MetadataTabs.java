/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.model;

import java.util.LinkedList;


/**
 * @author david
 */
public class MetadataTabs extends LinkedList<MetadataTab> {

    private static final long serialVersionUID = -203269897008753681L;

    private String id;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

}
