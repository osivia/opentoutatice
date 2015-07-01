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
package fr.toutatice.ecm.platform.core.notifications;

import java.security.Principal;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.ec.notification.NotificationListenerVeto;


/**
 * @author David Chevrier.
 *
 */
public class ToutaticeNotificationsVeto implements NotificationListenerVeto {

    @Override
    public boolean accept(Event event) throws Exception {
        
        DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
        
        NuxeoPrincipal currentUser = (NuxeoPrincipal) docCtx.getPrincipal();
        
        DocumentModel sourceDocument = docCtx.getSourceDocument();
        NuxeoPrincipal initiator = (NuxeoPrincipal) sourceDocument.getCoreSession().getPrincipal();
        
        boolean isSystemInitiator = StringUtils.equalsIgnoreCase("System", currentUser.getName());
        boolean isCurrentInitiator = StringUtils.equalsIgnoreCase(currentUser.getName(), initiator.getName());
        
        return !isSystemInitiator && !isCurrentInitiator;
    }
    
    /**
     * @param coreSession
     * @return true if initiator of Event is the System.
     */
    private boolean isSystemInitiator(CoreSession coreSession) {
        NuxeoPrincipal nxPrincipal = (NuxeoPrincipal) coreSession.getPrincipal();
        String actingUser = nxPrincipal.getActingUser();
        
        return StringUtils.equalsIgnoreCase("System", actingUser);
    }

    /**
     * @param accept
     * @param docCtx
     * @return true if current user is initiator of event.
     */
    private boolean isUserInitiator(CoreSession coreSession, DocumentEventContext docCtx) {
        Principal currentPrincipal = coreSession.getPrincipal();
        String currentPrincipalName = currentPrincipal.getName();
        
        String principalOfEventName = getInitiator(docCtx);
       
        return StringUtils.equals(currentPrincipalName, principalOfEventName);
    }

    /**
     * @param docCtx
     * @return initiator of event.
     */
    private String getInitiator(DocumentEventContext docCtx) {
        Principal principalOfEvent = docCtx.getPrincipal();
        return principalOfEvent.getName();
    }

}
