/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service.contrib;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.service.LayoutStore;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabGroup;

/**
 * @author david
 */

@XObject("group")
public class MetadataTabGroupDescriptor implements Serializable {

    private static final long serialVersionUID = -1841316023174112199L;

    @XNode("@name")
    protected String name;

    @XNode("@titleKey")
    protected String titleKey;

    @XNodeList(value = "widgets/widget", type = String[].class, componentType = String.class)
    protected String[] widgetsNames = new String[0];


    public String getName() {
        return name;
    }

    /**
     * Converts to model.
     * 
     * @return group model
     */
    public MetadataTabGroup getGroup() {
        MetadataTabGroup group = new MetadataTabGroup();
        group.setName(this.name);
        group.setTitle(this.titleKey);
        group.setWidgetsNames(new LinkedHashSet<>(Arrays.asList(this.widgetsNames)));
        group.setWidgetsDefs(getWidgetsDefinitions(this.widgetsNames));

        return group;
    }

    private Set<WidgetDefinition> getWidgetsDefinitions(String[] widgetsNames) {
        LayoutStore layoutStore = Framework.getService(LayoutStore.class);
        Set<WidgetDefinition> widgetsDefs = new LinkedHashSet<>();

        for (String widgetName : widgetsNames) {
            WidgetDefinition widgetDef = layoutStore.getWidgetDefinition("jsf", widgetName);
            widgetsDefs.add(widgetDef);
        }

        return widgetsDefs;
    }

}
