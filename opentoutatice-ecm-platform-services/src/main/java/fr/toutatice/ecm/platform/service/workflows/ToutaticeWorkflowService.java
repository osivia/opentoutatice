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

import org.nuxeo.ecm.core.api.DocumentModel;



/**
 * @author david chevrier
 *
 */
public interface ToutaticeWorkflowService {
    
    /** Validation worflow's category. */
    String VALIDATION_WF_CATEGORY = "validation";
    /** Key infos of running workflows of category validation. */
    String VALIDATION_WF_RUNNING_INFOS_KEY = "isValidationWfRunning";
    
    /**
     * Indicates if ToutaticeWorkflowService has contributions.
     * 
     * @return boolean
     */
    public boolean hasContributions();
    
    /**
     * @param category category of workflow(s)
     * @param document studied document
     * @return true if there is at least one workflow of given category running on given document
     */
    public boolean isWorkflowOfCategoryRunning(String category, DocumentModel document);
    
}
 