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

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author David Chevrier.
 *
 */
public class ToutaticeLocalPublishLiveListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws NuxeoException {

        DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
        CoreSession coreSession = docCtx.getCoreSession();
        DocumentModel document = docCtx.getSourceDocument();
        
        if (ToutaticeDocumentHelper.isInPublishSpace(coreSession, document)) {
            if (!document.isProxy() && !document.isVersion()) {
                if (!document.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_LOCAL_LIVE)) {
                    document.addFacet(ToutaticeNuxeoStudioConst.CST_FACET_LOCAL_LIVE);
                    ToutaticeDocumentHelper.saveDocumentSilently(coreSession, document, true);
                }
            }
        }

    }
    
}
