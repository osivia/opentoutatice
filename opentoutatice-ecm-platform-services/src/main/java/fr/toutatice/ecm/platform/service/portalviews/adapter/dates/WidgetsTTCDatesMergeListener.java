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
package fr.toutatice.ecm.platform.service.portalviews.adapter.dates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;
import fr.toutatice.ecm.platform.service.portalviews.adapter.WidgetsAdapterService;


/**
 * @author david chevrier.
 *
 */
public class WidgetsTTCDatesMergeListener implements EventListener {

    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DEFAULT_TIME = "12:00";

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (event.getContext() instanceof DocumentEventContext) {
            String eventName = event.getName();

            if (DocumentEventTypes.DOCUMENT_CREATED.equals(eventName) || DocumentEventTypes.BEFORE_DOC_UPDATE.equals(eventName)) {

                EventContext ctx = event.getContext();
                DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
                DocumentModel document = docCtx.getSourceDocument();

                CoreSession session = ctx.getCoreSession();

                if (document != null && !document.isImmutable()) {
                    if (document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TTC_EVENT)) {

                        TTCDateSilentMerger runner = new TTCDateSilentMerger(session, document, eventName);
                        runner.silentRun(false);

                    }
                }
            }
        }

    }


    private class TTCDateSilentMerger extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;
        private String eventName;

        public TTCDateSilentMerger(CoreSession session, DocumentModel document, String eventName) {
            super(session);
            this.document = document;
            this.eventName = eventName;
        }


        @Override
        public void run() throws ClientException {
            boolean isChangeableDocument = DocumentEventTypes.DOCUMENT_CREATED.equals(this.eventName); 
            boolean toManage = false;

            WidgetsAdapterService waService = Framework.getService(WidgetsAdapterService.class);
            
            if(waService.isInPortalViewContext()){
                toManage = mergeTTCDatesFields(toManage);
            } 
            
            if (isChangeableDocument && toManage) {
                this.session.saveDocument(this.document);
            }
        }


        private boolean mergeTTCDatesFields(boolean toManage) {
            SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);

            String dateBegin = (String) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_BEGIN);
            String timeBegin = (String) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_BEGIN);
            if (StringUtils.isNotBlank(dateBegin)) {
                if (StringUtils.isBlank(timeBegin)) {
                    timeBegin = DEFAULT_TIME;
                }
                Date begin;
                try {
                    begin = format.parse(new StringBuffer(3).append(dateBegin).append(" ").append(timeBegin).toString());
                } catch (ParseException e) {
                    throw new ClientException(e);
                }
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN, begin);
                toManage = true;
            }

            String dateEnd = (String) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_END);
            String timeEnd = (String) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_END);
            if (StringUtils.isNotBlank(dateEnd)) {
                if (StringUtils.isBlank(timeEnd)) {
                    timeEnd = DEFAULT_TIME;
                }
                Date end;
                try {
                    end = format.parse(new StringBuffer(3).append(dateEnd).append(" ").append(timeEnd).toString());
                } catch (ParseException e) {
                    throw new ClientException(e);
                }
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END, end);
                toManage = true;
            }
            return toManage;
        }
        
    }

}
