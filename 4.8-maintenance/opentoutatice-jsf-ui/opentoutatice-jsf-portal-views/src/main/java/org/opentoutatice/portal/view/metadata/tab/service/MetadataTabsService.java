/**
 * 
 */
package org.opentoutatice.portal.view.metadata.tab.service;

import org.opentoutatice.portal.view.metadata.tab.model.MetadataTabs;

/**
 * @author david
 */
public interface MetadataTabsService {

    /**
     * @return default tabs.
     */
    MetadataTabs getTabs();

    /**
     * @return specified tabs.
     */
    MetadataTabs getTabs(String id);

}
