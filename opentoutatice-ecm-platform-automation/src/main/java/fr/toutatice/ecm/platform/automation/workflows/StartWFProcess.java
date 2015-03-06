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
package fr.toutatice.ecm.platform.automation.workflows;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;

@Operation(id = StartWFProcess.ID, category = Constants.CAT_SERVICES, label = "Start a workflow", since = "2.0.0", description = "Enable to start a workflow bound to the document.")
public class StartWFProcess {
	public static final String ID = "Workflow.CreateProcess";

	@Context
	protected CoreSession documentManager;

	@Context
	protected transient DocumentRoutingService routingService;

	@Param(name = "workflow name", required = true, order = 0)
	protected String workflowName;

	@OperationMethod()
	public DocumentModel run(DocumentModel document) throws Exception {
	    
	    List<String> docIds = new ArrayList<String>(1);
	    docIds.add(document.getId());
	    
	    routingService.createNewInstance(workflowName,
	            docIds, documentManager, true);
        
		return document;
	}

}
