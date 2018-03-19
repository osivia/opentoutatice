/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTab;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabGroup;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabs;
import org.opentoutatice.portal.view.metadata.tab.service.contrib.ContentTabDescriptor;
import org.opentoutatice.portal.view.metadata.tab.service.contrib.MetadataTabsDescriptor;

/**
 * @author david
 */
public class MetadataTabsServiceImpl extends DefaultComponent implements MetadataTabsService {

    private static final String CONTENT = "contentTab";
    private static final String METADATA = "metadataTabs";
    private static final String DEFAULT_CONTRIB = "default";

    private MetadataTabsRegistry registry;

    private Set<String> excludedWidgetsNames;
    private Set<String> metadataWidgets;

    @Override
    public MetadataTabs getTabs() {
        return this.registry.getTabs(DEFAULT_CONTRIB);
    }

    @Override
    public MetadataTabs getTabs(String id) {
        return this.registry.getTabs(id);
    }

    // FIXME: takes contribution updated into account to recompute this list?
    public Set<String> getExcludedWidgetsNamesFromContentTab() {
        if (this.excludedWidgetsNames == null) {
            this.excludedWidgetsNames = new HashSet<>();
        }

        this.excludedWidgetsNames.addAll(getMetadataWidgets());

        return this.excludedWidgetsNames;
    }

    public Set<String> getMetadataWidgets() {
        if (this.metadataWidgets == null) {
            this.metadataWidgets = new HashSet<>();
        }

        for (MetadataTab tab : getTabs()) {
            for (MetadataTabGroup group : tab.getGroups()) {
                this.metadataWidgets.add(group.getName());
                this.metadataWidgets.addAll(group.getWidgetsNames());
            }
        }

        return this.metadataWidgets;
    }


    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (StringUtils.equals(METADATA, extensionPoint)) {
            this.registry.addContribution(((MetadataTabsDescriptor) contribution).getTabs());
        } else if (StringUtils.equals(CONTENT, extensionPoint)) {
            if (this.excludedWidgetsNames == null) {
                this.excludedWidgetsNames = new HashSet<>();
            }

            this.excludedWidgetsNames.addAll(((ContentTabDescriptor) contribution).getExcludedWidgets());
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (StringUtils.equals(METADATA, extensionPoint)) {
            this.registry.removeContribution(((MetadataTabsDescriptor) contribution).getTabs());
        } else if (StringUtils.equals(CONTENT, extensionPoint)) {
            this.excludedWidgetsNames = null;
        }
    }

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
        this.registry = new MetadataTabsRegistry();
    }

    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
        this.registry = null;
    }

}
