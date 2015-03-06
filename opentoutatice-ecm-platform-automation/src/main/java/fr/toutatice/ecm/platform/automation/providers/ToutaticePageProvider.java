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
package fr.toutatice.ecm.platform.automation.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.core.CoreQueryPageProviderDescriptor;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.automation.helper.TimeDebugger;


/**
 * Allows to execute pages provider queries with elasticSearch or not.
 * 
 * @author david chevrier
 *
 */
@Operation(id = ToutaticePageProvider.ID, category = Constants.CAT_FETCH, label = "Toutatice PageProvider",
        description = "Allows to execute pages provider queries with elasticSearch or not.")
public class ToutaticePageProvider {

    public static final String ID = "Document.TTCPageProvider";

    private static final Log log = LogFactory.getLog(ToutaticePageProvider.class);

    public static final String TTC_ES_PAGE_PROVIDER_NAME = "ToutaticeESPageProvider";

    public static final String ASC = "ASC";
    public static final String DESC = "DESC";

    @Context
    protected CoreSession session;

    @Param(name = "query", required = true)
    protected String query;

    /** Indicator of execution with elasticSearch or not. */
    @Param(name = "isES", required = false)
    protected Boolean isES = Boolean.FALSE;

    /** Indicates if we fetch Documents from ES or not. */
    @Param(name = "fetchFromES", required = false)
    protected Boolean targetedFetchFromES = Boolean.FALSE;

    @Param(name = "currentPageIndex", required = false)
    protected Integer currentPageIndex;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize;

    @Param(name = "maxResults", required = false)
    protected String maxResults = "-1";

    @Param(name = "sortBy", required = false, description = "Sort by properties (separated by comma)")
    protected String sortBy;

    @Param(name = "sortOrder", required = false, description = "Sort order, ASC or DESC", widget = Constants.W_OPTION, values = {ASC, DESC})
    protected String sortOrder;

    /* What is this??? */
    @Param(name = "documentLinkBuilder", required = false)
    protected String documentLinkBuilder;


    @OperationMethod
    public PaginableDocumentModelListImpl run() throws Exception {

        if (log.isDebugEnabled()) {
            TimeDebugger timeDebugger = TimeDebugger.getInstance("runPageProvider", session.getSessionId());
            timeDebugger.setStartTime();
        }

        PageProviderService pps = Framework.getLocalService(PageProviderService.class);

        List<SortInfo> sortInfos = getSortInfos();

        Map<String, Serializable> props = new HashMap<String, Serializable>(1);
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY, (Serializable) session);

        Long targetPage = null;
        if (currentPageIndex != null) {
            targetPage = currentPageIndex.longValue();
        }

        Long targetPageSize = null;
        if (pageSize != null) {
            targetPageSize = pageSize.longValue();
        }

        PaginableDocumentModelListImpl documents = null;

        if (isES) {

            PageProviderDefinition ttcEsDesc = pps.getPageProviderDefinition(TTC_ES_PAGE_PROVIDER_NAME);
            ttcEsDesc.setPattern(query);

            documents = new PaginableDocumentModelListImpl((PageProvider<DocumentModel>) pps.getPageProvider(TTC_ES_PAGE_PROVIDER_NAME, ttcEsDesc, null,
                    sortInfos, targetPageSize, targetPage, props, new Object[0]), documentLinkBuilder);

        } else {

            CoreQueryPageProviderDescriptor desc = new CoreQueryPageProviderDescriptor();
            desc.setPattern(query);

            if (StringUtils.isNotBlank(maxResults)) {
                // set the maxResults to avoid slowing down queries
                desc.getProperties().put("maxResults", maxResults);
            }

            documents = new PaginableDocumentModelListImpl((PageProvider<DocumentModel>) pps.getPageProvider(StringUtils.EMPTY, desc, null, sortInfos,
                    targetPageSize, targetPage, props, new Object[0]), documentLinkBuilder);

        }

        if (log.isDebugEnabled()) {
            TimeDebugger timeDebugger = TimeDebugger.getInstance("runPageProvider", session.getSessionId());
            String message = timeDebugger.getMessage("runPageProvider", session.getSessionId(), StringUtils.EMPTY, timeDebugger.getTotalTime());
            log.debug(message);
        }

        return documents;

    }


    /**
     * @return the sort informations.
     */
    private List<SortInfo> getSortInfos() {
        List<SortInfo> sortInfos = null;
        // Sort Info Management
        if (!StringUtils.isBlank(sortBy)) {
            sortInfos = new ArrayList<>();
            String[] sorts = sortBy.split(",");
            String[] orders = null;
            if (!StringUtils.isBlank(sortOrder)) {
                orders = sortOrder.split(",");
            }
            for (int i = 0; i < sorts.length; i++) {
                String sort = sorts[i];
                boolean sortAscending = (orders != null && orders.length > i && "asc".equals(orders[i].toLowerCase()));
                sortInfos.add(new SortInfo(sort, sortAscending));
            }
        }
        return sortInfos;
    }


}
