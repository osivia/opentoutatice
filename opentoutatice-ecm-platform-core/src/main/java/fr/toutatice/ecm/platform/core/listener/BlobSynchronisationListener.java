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
 * dchevrier
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * 
 * Synchronize dc:title and blob name
 * 
 * (Apply dc:title to blobname)
 * 
 * @author Jean-Sébastien Steux
 *
 */
public class BlobSynchronisationListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext ctx = (DocumentEventContext) event.getContext();
            CoreSession session = ctx.getCoreSession();
            DocumentModel document = ctx.getSourceDocument();


            if (document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_FILE_SCHEMA)) {
                Blob blob = (Blob) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_NUXEO_FILE_CONTENT);
                if (blob != null) {
                    String title = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_NUXEO_DC_TITLE);
                    if (StringUtils.isNotEmpty(title) && !StringUtils.equalsIgnoreCase(blob.getFilename(), title)) {

                        Property p = document.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_NUXEO_FILE_CONTENT);
                        Object o = p.getValue();
                        if (o instanceof Blob) {
                            Blob newBlob = (Blob) o;
                            newBlob.setFilename(title);
                            p.setValue(newBlob);
                        }

                        ToutaticeDocumentHelper.saveDocumentSilently(session, document, true);
                    }

                }
            }
        }


    }

}

