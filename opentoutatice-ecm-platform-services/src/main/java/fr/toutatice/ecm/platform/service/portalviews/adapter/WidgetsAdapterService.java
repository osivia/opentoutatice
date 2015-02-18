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

import org.nuxeo.ecm.platform.forms.layout.api.Widget;


/**
 * Service to map a nuxeo widget to an adapted widget for portalViews.
 * 
 * @author david chevrier
 *
 */
public interface WidgetsAdapterService extends Serializable {
    
    /**
     * Gives the mapped nuxeo widget for portalView.
     * 
     * @param nxWidget
     * @return portalview widget
     */
    Widget getPortalViewWidget(Widget nxWidget);

}
