/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service.contrib;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTab;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabs;

/**
 * @author david
 */

@XObject("tabs")
public class MetadataTabsDescriptor implements Serializable {

    private static final long serialVersionUID = -789119339722631075L;

    @XNode("@id")
    protected String id;

    @XNode("@titleKey")
    protected String titleKey;

    @XNodeList(value = "tab", type = MetadataTabDescriptor[].class, componentType = MetadataTabDescriptor.class)
    protected MetadataTabDescriptor[] tabs = new MetadataTabDescriptor[0];

    public String getId() {
        return this.id;
    }

    public String getTitleKey() {
        return titleKey;
    }

    /**
     * Converts to model.
     * 
     * @return list of tab model.
     */
    public MetadataTabs getTabs() {
        MetadataTabs tabs = new MetadataTabs();
        tabs.setId(this.id);

        for (MetadataTabDescriptor tabDesc : this.tabs) {
            MetadataTab tab = new MetadataTab();
            tab.setName(tabDesc.getName());
            tab.setTitle(tabDesc.getTitleKey());
            tab.setGroups(tabDesc.getGroups());

            tabs.add(tab);
        }

        return tabs;
    }

}
