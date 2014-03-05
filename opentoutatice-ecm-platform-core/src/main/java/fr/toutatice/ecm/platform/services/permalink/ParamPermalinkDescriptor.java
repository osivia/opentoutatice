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

package fr.toutatice.ecm.platform.services.permalink;

import org.nuxeo.common.xmap.annotation.XContent;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "param")
public class ParamPermalinkDescriptor
{


    @XNode("@name")
    protected String name;
    
    @XContent
    protected String paramValue;
    
    
    public String getName()
    {
        return name;
    }

    public String getParamValue()
    {
    	if (paramValue!=null) {
    		paramValue = paramValue.trim();
    		paramValue = paramValue.replace("\n", "");
    	
    	}  
        return paramValue;
    }

    public void setParamValue(String paramValue)
    {
        this.paramValue = paramValue;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    
}
