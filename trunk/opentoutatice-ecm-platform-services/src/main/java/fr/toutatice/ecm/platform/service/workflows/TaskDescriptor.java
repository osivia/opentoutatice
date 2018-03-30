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

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * To define tasks used by ToutaticeWorkflowService.
 * 
 * @author david chevrier
 *
 */
@XObject("task")
public class TaskDescriptor implements Serializable {
    
    private static final long serialVersionUID = 3371712167362896281L;
    
    @XNode("@name")
    public String taskName;
    
    /**
     * @return names of task.
     */
    public String getTaskName(){
        return this.taskName;
    }
    
    @XNode("@permission")
    public String permission = StringUtils.EMPTY;
   
    /**
     * @return the permission to manage Task.
     */
    public String getPermission(){
        return this.permission;
    }

}
