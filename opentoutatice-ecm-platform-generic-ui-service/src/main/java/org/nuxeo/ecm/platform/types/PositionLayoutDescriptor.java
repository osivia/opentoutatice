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
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david
 *
 */
@XObject("position")
public class PositionLayoutDescriptor implements Serializable {

    private static final long serialVersionUID = 2178639105074542595L;
    
    public static final int POS_UNDEFINED = -999;
    public static final String LAST_POSITION = "end";
    
    @XNode("before")
    String beforeLayout;
    
    public String getBeforeLayout(){
        return beforeLayout;
    }
    
    @XNode("after")
    String afterLayout;
    
    public String getAfterLayout(){
        return afterLayout;
    }
    
    @XNode("at")
    String positionLayout = String.valueOf(POS_UNDEFINED);
    
    public String getPositionLayout(){
        return positionLayout;
    }
    
}
