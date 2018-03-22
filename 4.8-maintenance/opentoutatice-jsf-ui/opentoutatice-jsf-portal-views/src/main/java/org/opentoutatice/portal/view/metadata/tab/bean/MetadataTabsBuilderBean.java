/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.bean;

import java.io.Serializable;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.service.LayoutStore;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabs;
import org.opentoutatice.portal.view.metadata.tab.service.MetadataTabsService;
import org.opentoutatice.portal.view.metadata.tab.service.MetadataTabsServiceImpl;


/**
 * @author david
 */
@Name("metadataTabsBuilder")
@Scope(ScopeType.CONVERSATION)
public class MetadataTabsBuilderBean implements Serializable {

    public static final String CONTENT_TAB = "content";

    private static final long serialVersionUID = 4200470320973486016L;

    private MetadataTabsService tabsService;

    public MetadataTabsBuilderBean() {
        super();
        this.tabsService = Framework.getService(MetadataTabsService.class);
    }

    public MetadataTabs getTabs() {
        return this.tabsService.getTabs();
    }

    public boolean showWidget(String widget, String tabName) {
        boolean show = false;

        // We are on content tab
        if (CONTENT_TAB.equals(tabName)) {
            Set<String> widgets = ((MetadataTabsServiceImpl) this.tabsService).getExcludedWidgetsNamesFromContentTab();
            show = !widgets.contains(widget);
        } else {
            // Metadata tab
            Set<String> metadataWidgets = ((MetadataTabsServiceImpl) this.tabsService).getMetadataWidgets();
            show = metadataWidgets.contains(widget);
        }

        return show;
    }

    public LayoutDefinition getMetatdataLayout() {
        LayoutStore store = Framework.getService(LayoutStore.class);
        return store.getLayoutDefinition("jsf", "ottc_metadata");
    }

}
