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
package fr.toutatice.ecm.platform.service.portalviews.adapter;

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

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david chevrier.
 *
 */
public class WidgetsAdapterListener implements EventListener {

    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DEFAULT_TIME = "12:00";

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (event.getContext() instanceof DocumentEventContext) {
            EventContext ctx = event.getContext();
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel document = docCtx.getSourceDocument();

            String eventName = event.getName();
            CoreSession session = ctx.getCoreSession();

            if (document != null && !document.isImmutable()) {
                if (document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TTC_EVENT)) {
                    try {
                        mergeDateNTime(document, session, eventName);
                    } catch (ParseException e) {
                        throw new ClientException(e);
                    }
                }
            }
        }

    }

    public void mergeDateNTime(DocumentModel document, CoreSession session, String eventName) throws ParseException {
        boolean isChangeableDocument = DocumentEventTypes.DOCUMENT_CREATED.equals(eventName);
        boolean toManage = false;

        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);

        String dateBegin = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_BEGIN);
        String timeBegin = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_BEGIN);
        if (StringUtils.isNotBlank(dateBegin)) {
            if (StringUtils.isBlank(timeBegin)) {
                timeBegin = DEFAULT_TIME;
            }
            Date begin = format.parse(new StringBuffer(3).append(dateBegin).append(" ").append(timeBegin).toString());
            document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN, begin);
            toManage = true;
        }

        String dateEnd = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_END);
        String timeEnd = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_END);
        if (StringUtils.isNotBlank(dateEnd)) {
            if (StringUtils.isBlank(timeEnd)) {
                timeEnd = DEFAULT_TIME;
            }
            Date end = format.parse(new StringBuffer(3).append(dateEnd).append(" ").append(timeEnd).toString());
            document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END, end);
            toManage = true;
        }

        if (toManage && isChangeableDocument) {
            session.saveDocument(document);
        }

    }


}
