/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 * Contributors:
 * Thomas Roger
 */

package fr.toutatice.ecm.platform.core.listener;

import static org.nuxeo.ecm.core.api.CoreSession.ALLOW_VERSION_WRITE;
import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.UPDATE_PICTURE_VIEW_EVENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureResourceAdapter;

/**
 * Listener generating picture views.
 *
 * @since 5.7.2
 */
public class ToutaticePictureViewListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {

        if (UPDATE_PICTURE_VIEW_EVENT.equals(event.getName()) && event.getContext() instanceof DocumentEventContext) {

            EventContext ctx = event.getContext();
            if (!(ctx instanceof DocumentEventContext)) {
                return;
            }

            DocumentEventContext docCtx = (DocumentEventContext) ctx;
            DocumentModel doc = docCtx.getSourceDocument();

            // do the generation now instead of scheduling a PictureViewsGenerationWork
            Property fileProp = doc.getProperty("file:content");

            ArrayList<Map<String, Object>> pictureTemplates = null;
            PictureResourceAdapter picture = doc.getAdapter(PictureResourceAdapter.class);

            Blob blob = (Blob) fileProp.getValue();
            if (blob == null) {
                // do not compute views
                return;
            }

            String filename = blob.getFilename();
            String title = doc.getTitle();
            try {
                picture.fillPictureViews(blob, filename, title, pictureTemplates);
            } catch (IOException e) {
                throw new ClientException(e);
            }

            if (doc.isVersion()) {
                doc.putContextData(ALLOW_VERSION_WRITE, Boolean.TRUE);
            }
            // doc.putContextData("disableNotificationService", Boolean.TRUE);
            // doc.putContextData("disableAuditLogger", Boolean.TRUE);
            // session.saveDocument(workingDocument);

            // launch work doing the actual views generation
            // PictureViewsGenerationWork work = new PictureViewsGenerationWork(doc.getRepositoryName(), doc.getId(), "file:content");
            // WorkManager workManager = Framework.getLocalService(WorkManager.class);
            // workManager.schedule(work, WorkManager.Scheduling.IF_NOT_SCHEDULED);
        }
    }
}
