/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.model;

import java.util.LinkedList;
import java.util.List;

/**
 * @author david
 */
public class MetadataTab {

    private String name;

    private String title;

    private List<MetadataTabGroup> groups;

    public MetadataTab() {
        super();
        this.groups = new LinkedList<>();
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public List<MetadataTabGroup> getGroups() {
        return groups;
    }


    public void setGroups(List<MetadataTabGroup> groups) {
        this.groups = groups;
    }


}
