/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;

/**
 * @author david
 */
public class MetadataTabGroup {

    private String name;
    private String title;

    private Set<String> widgetsNames;
    private Set<WidgetDefinition> widgetsDefs;

    public MetadataTabGroup() {
        super();
        this.widgetsNames = new LinkedHashSet<>();
        this.widgetsDefs = new LinkedHashSet<>();
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


    public Set<String> getWidgetsNames() {
        return widgetsNames;
    }


    public void setWidgetsNames(Set<String> widgetsNames) {
        this.widgetsNames = widgetsNames;
    }


    public Set<WidgetDefinition> getWidgetsDefs() {
        return widgetsDefs;
    }


    public void setWidgetsDefs(Set<WidgetDefinition> widgetsDefs) {
        this.widgetsDefs = widgetsDefs;
    }

}
