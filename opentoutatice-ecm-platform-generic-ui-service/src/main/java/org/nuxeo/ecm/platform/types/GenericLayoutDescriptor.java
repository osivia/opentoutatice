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
 *    
 */
package org.nuxeo.ecm.platform.types;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * 
 * @author david
 *
 */
@XObject("layout")
public class GenericLayoutDescriptor implements Serializable {

    private static final long serialVersionUID = 2178639105074542595L;
    
    @XNode("@name")
    String name;

    public String getName() {
        return name;
    }
    
    @XNode("position")
    PositionLayoutDescriptor positionDescriptor;
    
    public PositionLayoutDescriptor getPositionLayoutDescriptor(){
        return positionDescriptor;
    }
    
    
    @XNodeList(value = "excludedTypes/type", type = String[].class, componentType = String.class)
    String[] excludedTypes = new String[0];
    
    public String[] getExcludedTypes(){
        return excludedTypes;
    }

}
