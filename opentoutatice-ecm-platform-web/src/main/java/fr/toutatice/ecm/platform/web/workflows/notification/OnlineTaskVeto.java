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
package fr.toutatice.ecm.platform.web.workflows.notification;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.ec.notification.NotificationListenerVeto;


/**
 * @author David Chevrier.
 *
 */
public class OnlineTaskVeto implements NotificationListenerVeto {

    /**
     * Disable "workflowTaskAssigned for Online Workflow.
     */
    @Override
    public boolean accept(Event event) throws Exception {
        boolean accepted = true;

        if ("workflowTaskAssigned".equals(event.getName())) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            Map<String, Serializable> properties = docCtx.getProperties();

            if (MapUtils.isNotEmpty(properties)) {
                String taskId = (String) properties.get("nodeId");

                if ("Task4bf".equals(taskId)) {
                    accepted = false;
                }

            }
        }
        return accepted;
    }

}
