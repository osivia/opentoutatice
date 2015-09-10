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
 * lbillon
 * dchevrier
 */
package fr.toutatice.ecm.platform.core.listener;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.webapp.helpers.EventNames;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author David Chevrier.
 *
 */
public class ToutaticePublishLocalStateListener implements EventListener {

    public enum States {
        live, published;
    }

    @Override
    public void handleEvent(Event event) throws ClientException {

        String eventName = event.getName();
        DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
        CoreSession coreSession = docCtx.getCoreSession();
        DocumentModel sourceDocument = docCtx.getSourceDocument();

        if (DocumentEventTypes.DOCUMENT_CREATED.equals(eventName) || DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(eventName)
                || DocumentEventTypes.DOCUMENT_IMPORTED.equals(eventName) || DocumentEventTypes.DOCUMENT_MOVED.equals(eventName)) {

            if (ToutaticeDocumentHelper.isInPublishSpace(coreSession, sourceDocument) && !sourceDocument.isProxy()) {
                setState(coreSession, sourceDocument, States.live.name());
            } else {
                if(DocumentEventTypes.DOCUMENT_IMPORTED.equals(eventName) || DocumentEventTypes.DOCUMENT_MOVED.equals(eventName)){
                    setState(coreSession, sourceDocument, StringUtils.EMPTY);
                }
            }

        } else if (ToutaticeGlobalConst.CST_EVENT_DOC_LOCALLY_PUBLISHED.equals(eventName)) {
            setState(coreSession, sourceDocument, States.published.name());
        } else if (EventNames.DOCUMENT_PUBLISHED.equals(eventName)) {

            if (ToutaticeDocumentHelper.isInPublishSpace(coreSession, sourceDocument) && sourceDocument.isProxy()) {
                setState(coreSession, sourceDocument, StringUtils.EMPTY);
            }

        }

    }
    
    /**
     * Set the publish local state of document.
     */
    protected void setState(CoreSession coreSession, DocumentModel document, String state){
        document.setPropertyValue("ttcpls:state", state);
        if(States.live.name().equals(state)){
            // Facet is better for NXQL search
            document.addFacet("isLocalPublishLive");
        }
        ToutaticeDocumentHelper.saveDocumentSilently(coreSession, document, true);
    }

}
