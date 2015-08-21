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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.LayoutRowDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetDefinition;
import org.nuxeo.ecm.platform.forms.layout.api.WidgetReference;
import org.nuxeo.ecm.platform.forms.layout.service.WebLayoutManager;
import org.nuxeo.ecm.platform.types.Layouts;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;
import fr.toutatice.ecm.platform.service.portalviews.adapter.WidgetsAdapterService;


/**
 * @author david chevrier.
 *
 */
public class WidgetsDatesAdapterListener implements EventListener {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String PV_DATE_START_PROPERTY = "ttcevt:dateBegin";
    public static final String PV_DATE_END_PROPERTY = "ttcevt:dateEnd";

    protected static WidgetsAdapterService waSrv;
    protected static TypeManager typeMng;
    protected static WebLayoutManager wlMng;

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

                        DateNTimeSilentFiller runner = new DateNTimeSilentFiller(session, document, eventName);
                        runner.silentRun(false);

                    }
                }
            }
        }

    }

    private class DateNTimeSilentFiller extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;
        private String eventName;

        public DateNTimeSilentFiller(CoreSession session, DocumentModel document, String eventName) {
            super(session);
            this.document = document;
            this.eventName = eventName;
        }

        @Override
        public void run() throws ClientException {
            boolean isChangeableDocument = DocumentEventTypes.DOCUMENT_CREATED.equals(eventName);

            WidgetsAdapterService widgetsAdapterService = getWidgetsAdapterService();
            Map<String, String> widgetsMappings = widgetsAdapterService.getWidgetsMappings();

            if (MapUtils.isNotEmpty(widgetsMappings)) {
                Set<Entry<String, String>> widgetsMapEntries = widgetsMappings.entrySet();

                for (Entry<String, String> widgetMapEntry : widgetsMapEntries) {

                    String nxWidgetName = widgetMapEntry.getKey();
                    String pvWidgetName = widgetMapEntry.getValue();

                    boolean docHasWidget = documentHasWidget(this.document, nxWidgetName);

                    if (docHasWidget && hasDateLikeSchema(nxWidgetName)) {

                        if (widgetsAdapterService.isInPortalViewContext()) {
                            fromTTCToNxDate(nxWidgetName, pvWidgetName);
                        } else {
                            fromNxToTTCDate(nxWidgetName, pvWidgetName);
                        }

                        if (isChangeableDocument) {
                            this.session.saveDocument(this.document);
                        }

                    }
                }

            }
        }

        /**
         * Checks if current document has given widget in its layout.
         * 
         * @param nxWidgetName
         */
        private boolean documentHasWidget(DocumentModel document, String nxWidgetName) {
            boolean has = false;

            org.nuxeo.ecm.platform.types.Type type = getTypeManager().getType(document.getType());
            Map<String, Layouts> layoutsByMode = type.getLayouts();

            Set<Entry<String, Layouts>> entrySet = layoutsByMode.entrySet();
            Iterator<Entry<String, Layouts>> itLayouts = entrySet.iterator();

            while (itLayouts.hasNext() && !has) {
                Layouts layouts = itLayouts.next().getValue();
                String[] layoutsNames = layouts.getLayouts();

                for (String layoutName : layoutsNames) {
                    LayoutDefinition layoutDefinition = getWebLayoutManger().getLayoutDefinition(layoutName);
                    LayoutRowDefinition[] rows = layoutDefinition.getRows();

                    if (rows != null) {
                        List<LayoutRowDefinition> rowsAsList = Arrays.asList(rows);
                        Iterator<LayoutRowDefinition> itRows = rowsAsList.iterator();

                        while (itRows.hasNext() && !has) {
                            WidgetReference[] widgetReferences = itRows.next().getWidgetReferences();

                            if (widgetReferences != null) {
                                List<WidgetReference> widgetsAsList = Arrays.asList(widgetReferences);
                                Iterator<WidgetReference> itWidgets = widgetsAsList.iterator();
                                
                                while (itWidgets.hasNext() && !has) {
                                    if (nxWidgetName.equals(itWidgets.next().getName())) {
                                        has = true;
                                    }
                                }

                            }

                        }
                    }
                }
            }

            return has;
        }

        private void fromTTCToNxDate(String nxWidgetName, String pvWidgetName) {
            List<String> pvFields = getWidgetsAdapterService().getPvFields(pvWidgetName);

            if (CollectionUtils.isNotEmpty(pvFields)) {

                if (pvFields.contains(PV_DATE_START_PROPERTY)) {

                    Calendar ttcDateTimeStart = (GregorianCalendar) this.document
                            .getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN);
                    if (null != ttcDateTimeStart) {
                        List<String> nxFields = getWidgetsAdapterService().getNxFields(nxWidgetName);

                        if (CollectionUtils.isNotEmpty(nxFields) && nxFields.size() == 1) {
                            this.document.setPropertyValue(nxFields.get(0), ttcDateTimeStart.getTime());
                        }
                    }

                } else if (pvFields.contains(PV_DATE_END_PROPERTY)) {

                    Calendar ttcDateTimeEnd = (GregorianCalendar) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END);
                    if (null != ttcDateTimeEnd) {
                        List<String> nxFields = getWidgetsAdapterService().getNxFields(nxWidgetName);

                        if (CollectionUtils.isNotEmpty(nxFields) && nxFields.size() == 1) {
                            this.document.setPropertyValue(nxFields.get(0), ttcDateTimeEnd.getTime());
                        }
                    }
                }
            }
        }

        private void fromNxToTTCDate(String nxWidgetName, String pvWidgetName) {
            SimpleDateFormat formatDate = new SimpleDateFormat(DATE_FORMAT);
            SimpleDateFormat formatTime = new SimpleDateFormat(TIME_FORMAT);

            List<String> nxFields = getWidgetsAdapterService().getNxFields(nxWidgetName);
            List<String> pvFields = getWidgetsAdapterService().getPvFields(pvWidgetName);

            if (CollectionUtils.isNotEmpty(pvFields)) {

                if (pvFields.contains(PV_DATE_START_PROPERTY)) {

                    if (CollectionUtils.isNotEmpty(nxFields) && nxFields.size() == 1) {

                        Calendar nxDateTimeStart = (GregorianCalendar) this.document.getPropertyValue(nxFields.get(0));
                        if (null != nxDateTimeStart) {

                            Date ttcDateTimeBegin = nxDateTimeStart.getTime();
                            this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN, ttcDateTimeBegin);

                            String dateBegin = formatDate.format(ttcDateTimeBegin);
                            this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_BEGIN, dateBegin);

                            String timeBegin = formatTime.format(ttcDateTimeBegin);
                            if (StringUtils.isNotBlank(timeBegin)) {
                                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_BEGIN, timeBegin);
                            }
                        }
                    }
                } else if (pvFields.contains(PV_DATE_END_PROPERTY)) {

                    if (CollectionUtils.isNotEmpty(nxFields) && nxFields.size() == 1) {

                        Calendar nxDateTimeEnd = (GregorianCalendar) this.document.getPropertyValue(nxFields.get(0));
                        if (null != nxDateTimeEnd) {

                            Date ttcDateTimeEnd = nxDateTimeEnd.getTime();
                            this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END, ttcDateTimeEnd);

                            String dateEnd = formatDate.format(ttcDateTimeEnd);
                            this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_END, dateEnd);

                            String timeEnd = formatTime.format(ttcDateTimeEnd);
                            if (StringUtils.isNotBlank(timeEnd)) {
                                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_END, timeEnd);
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean hasDateLikeSchema(String nxWidgetName) {
        boolean has = false;

        List<String> nxFields = getWidgetsAdapterService().getNxFields(nxWidgetName);
        if (nxFields != null) {

            int nbFields = 0;
            for (String nxField : nxFields) {
                String[] split = StringUtils.split(nxField, ":");

                if (split != null && split.length > 0) {
                    String schemaPrefix = split[0];

                    SchemaManager schemaManager = (SchemaManager) Framework.getService(SchemaManager.class);
                    Schema schemaFromPrefix = schemaManager.getSchemaFromPrefix(schemaPrefix);

                    if (schemaFromPrefix != null) {
                        Field field = schemaFromPrefix.getField(nxField);
                        Type fieldType = field.getType();

                        if (nbFields == 0) {
                            has = "date".equals(fieldType.getName());
                        } else {
                            has &= "date".equals(fieldType.getName());
                        }
                    }

                }
                nbFields++;
            }


        }


        return has;
    }

    public static WidgetsAdapterService getWidgetsAdapterService() {
        if (waSrv == null) {
            waSrv = (WidgetsAdapterService) Framework.getService(WidgetsAdapterService.class);
        }
        return waSrv;
    }

    public static WebLayoutManager getWebLayoutManger() {
        if (wlMng == null) {
            wlMng = (WebLayoutManager) Framework.getService(WebLayoutManager.class);
        }
        return wlMng;
    }

    public static TypeManager getTypeManager() {
        if (typeMng == null) {
            typeMng = (TypeManager) Framework.getService(TypeManager.class);
        }
        return typeMng;
    }
}
