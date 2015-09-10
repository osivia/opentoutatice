/*
 * (C) Copyright 2015 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.service.workflows;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author David Chevrier.
 *
 */
@XObject("workflow")
public class WorkflowDescriptor {
    
    /** Name of workflow. */
    @XNode("@name")
    public String wfName;
    
    /** Category of workflow. */
    @XNode("@category")
    public String category;
    
    /**
     * @return name of the workflow.
     */
    public String getWfName(){
        return this.wfName;
    }
    
    /**
     * @return the category of workflow.
     */
    public String getWfCategory(){
        return this.category;
    }

}
