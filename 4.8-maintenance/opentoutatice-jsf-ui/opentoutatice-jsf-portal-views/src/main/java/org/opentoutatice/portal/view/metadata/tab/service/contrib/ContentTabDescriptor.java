/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service.contrib;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @author david
 */
@XObject("tab")
public class ContentTabDescriptor implements Serializable {

    private static final long serialVersionUID = -980722729212465428L;

    @XNode("@titleKey")
    protected String titleKey;

    @XNodeList(value = "excludedWidgets/widget", type = String[].class, componentType = String.class)
    protected String[] excludedWidgets = new String[0];

    public String getTitleKey() {
        return titleKey;
    }

    public List<String> getExcludedWidgets() {
        return Arrays.asList(this.excludedWidgets);
    }

}
