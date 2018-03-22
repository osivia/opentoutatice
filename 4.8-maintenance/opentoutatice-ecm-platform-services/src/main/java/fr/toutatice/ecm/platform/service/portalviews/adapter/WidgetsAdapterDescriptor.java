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

import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * Class to map portalView widget with nuxeo widget.
 * 
 * @author david chevrier
 *
 */
@XObject("mappings")
public class WidgetsAdapterDescriptor implements Serializable {

    private static final long serialVersionUID = -4387448031957793330L;
    
    @XNodeList(value = "mapping", type = WidgetMappingDescriptor[].class, componentType = WidgetMappingDescriptor.class)
    WidgetMappingDescriptor[] nameMappings = new WidgetMappingDescriptor[0];
    
    public WidgetMappingDescriptor[] getWidgetsNamesMapping(){
        return this.nameMappings;
    }
    
    @XNodeList(value = "mappingType", type = WidgetMappingTypeDescriptor[].class, componentType = WidgetMappingTypeDescriptor.class)
    WidgetMappingTypeDescriptor[] typeMappings = new WidgetMappingTypeDescriptor[0];
    
    public WidgetMappingTypeDescriptor[] getWidgetsTypesMapping(){
        return this.typeMappings;
    }

}
