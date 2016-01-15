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

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david chevrier
 *
 */
@XObject("mapping")
public class WidgetMappingDescriptor implements Serializable {

    private static final long serialVersionUID = -5553269939903120670L;
    
    @XNode("@nxWidget")
    String nxWidget;
    
    @XNode("@pvWidget")
    String pvWidget;

    public String getNxWidget() {
        return nxWidget;
    }

    public String getPvWidget() {
        return pvWidget;
    }

}
