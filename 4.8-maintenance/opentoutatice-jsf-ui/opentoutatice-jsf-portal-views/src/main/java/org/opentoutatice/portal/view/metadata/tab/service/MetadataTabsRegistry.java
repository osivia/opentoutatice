/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service;

import org.nuxeo.runtime.model.SimpleContributionRegistry;
import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabs;

/**
 * @author david
 */
// FIXME: repplace SimpleContributionRegistry by ContributionFragmentRegistry to manage merge!!
public class MetadataTabsRegistry extends SimpleContributionRegistry<MetadataTabs> {

    @Override
    public String getContributionId(MetadataTabs contrib) {
        return contrib.getId();
    }

    public MetadataTabs getTabs(String id) {
        return super.getContribution(id);
    }

}
