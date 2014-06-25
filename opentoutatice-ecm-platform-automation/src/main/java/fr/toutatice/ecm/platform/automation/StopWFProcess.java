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
 * mberhaut1
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.automation;

import java.util.List;

import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.jbpm.JbpmEventNames;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.core.helper.AbandonProcessUnrestricted;
import org.nuxeo.ecm.platform.jbpm.core.helper.EndProcessUnrestricted;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;


/**
 * @author David Chevrier
 */
@Operation(id = StopWFProcess.ID, category = Constants.CAT_SERVICES, label = "Stop process", since = "5.3.2",
        description = "Stop (or cancel) a process bound to the document.")
public class StopWFProcess {

    public static final String ID = "Workflow.CancelProcess";

    @Context
    protected CoreSession documentManager;

    @Context
    protected transient JbpmService jbpmService;

    @Param(name = "workflow name", required = true, order = 0)
    protected String processName;

    @OperationMethod()
    public void run(DocumentModel document) throws Exception {
        NuxeoPrincipal currentUser = (NuxeoPrincipal) documentManager.getPrincipal();
        ProcessInstance currentProcess = getProcessByName(currentUser, ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE, document);
        if (currentProcess != null) {
            // remove wf acls
            Long pid = Long.valueOf(currentProcess.getId());
            if (document != null) {
                AbandonProcessUnrestricted runner = new AbandonProcessUnrestricted(documentManager, document.getRef(), pid);
                runner.runUnrestricted();
            }

            // end process and tasks using unrestricted session
            List<TaskInstance> tis = jbpmService.getTaskInstances(documentManager.getDocument(document.getRef()), (NuxeoPrincipal) null, null);

            EndProcessUnrestricted endProcessRunner = new EndProcessUnrestricted(documentManager, tis);
            endProcessRunner.runUnrestricted();

            jbpmService.deleteProcessInstance(currentUser, pid);
        }
    }
    
    /* FIXME: to move in Core */
    public ProcessInstance getProcessByName(NuxeoPrincipal currentUser, String processname, DocumentModel document) throws ClientException {
        ProcessInstance searchProcess = null;
        List<ProcessInstance> processes = jbpmService.getProcessInstances(document, currentUser, null);
        if (processes != null) {
            for (ProcessInstance process : processes) {
                if (process.getProcessDefinition().getName().equals(processname)) {
                    searchProcess = process;
                    break;
                }
            }
        }
        return searchProcess;
    }

}
