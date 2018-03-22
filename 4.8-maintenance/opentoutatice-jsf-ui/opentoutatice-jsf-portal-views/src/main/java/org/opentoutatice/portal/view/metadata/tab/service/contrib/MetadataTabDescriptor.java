/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service.contrib;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabGroup;

/**
 * @author david
 */

@XObject("tab")
public class MetadataTabDescriptor implements Serializable {

    private static final long serialVersionUID = -443513019417569370L;

    @XNode("@name")
    protected String name;

    @XNode("@titleKey")
    protected String titleKey;

    @XNodeList(value = "group", type = MetadataTabGroupDescriptor[].class, componentType = MetadataTabGroupDescriptor.class)
    protected MetadataTabGroupDescriptor[] groups = new MetadataTabGroupDescriptor[0];

    public String getName() {
        return name;
    }

    public String getTitleKey() {
        return titleKey;
    }

    /**
     * Converts to model.
     * 
     * @return list of tab model
     */
    public List<MetadataTabGroup> getGroups() {
        List<MetadataTabGroup> groups = new LinkedList<>();

        for (MetadataTabGroupDescriptor groupDesc : this.groups) {
            groups.add(groupDesc.getGroup());
        }

        return groups;
    }

}
