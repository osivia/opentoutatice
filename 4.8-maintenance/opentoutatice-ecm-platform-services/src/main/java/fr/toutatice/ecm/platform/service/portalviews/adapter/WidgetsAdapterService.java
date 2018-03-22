/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *   lbillon
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.service.portalviews.adapter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.platform.forms.layout.api.Widget;


/**
 * Service to map a nuxeo widget to an adapted widget for portalViews.
 * 
 * @author david chevrier
 *
 */
public interface WidgetsAdapterService extends Serializable {
    
    /**
     * @return current Portal View.
     */
    String getCurrentPortalView();
    
    /**
     * @return the Nuxeo/Portal widgets mapping.
     */
    Map<String, String> getWidgetsMappings();
    
    /**
     * @return true if facelet is in PortalView context.
     */
    boolean isInPortalViewContext();
    
    /**
     * Add a portal view id. 
     */
    void addPortalViewId(String viewId);
    
    /**
     * Add list of portal views ids.
     */
    void addPortalViewsIds(String... viewIds);
    
    /**
     * Gives the mapped nuxeo widget for portalView.
     * 
     * @param nxWidget
     * @return portalview widget
     */
    Widget getPortalViewWidget(Widget nxWidget) throws Exception;
    
    /**
     * @param nxWidgetName
     * @return metada fields of given Nuxeo widget.
     */
    List<String> getNxFields(String nxWidgetName);
    
    /**
     * @param pvWidgetName
     * @return metada fields of given portal view widget.
     */
    List<String> getPvFields(String pvWidgetName);

}
