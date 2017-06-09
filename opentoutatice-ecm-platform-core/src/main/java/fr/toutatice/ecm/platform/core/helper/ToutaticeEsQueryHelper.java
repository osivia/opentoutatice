/**
 * 
 */
package fr.toutatice.ecm.platform.core.helper;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class ToutaticeEsQueryHelper {

    /** Results limit. */
    public static final int DEFAULT_MAX_RESULT_SIZE = 10000;

    /** ElasticSeach service for querying. */
    private static ElasticSearchService ess;

    /**
     * Getter for ElasticSearchService.
     */
    protected static ElasticSearchService getElasticSearchService() {
        if (ess == null) {
            ess = Framework.getService(ElasticSearchService.class);
        }
        return ess;
    }

    /**
     * Utility class.
     */
    private ToutaticeEsQueryHelper() {
        super();
    }

    /**
     * Executes queryAndAggregate in unrestricted way.
     * 
     * @param session
     * @param query
     * @return IterableQueryResult
     */
    public static IterableQueryResult unrestrictedQueryAndAggregate(CoreSession session, String query) {
        UnrestrictedQueryAndAggregate uQnA = new UnrestrictedQueryAndAggregate(session);
        uQnA.runUnrestricted();
        return uQnA.getRowsResults();
    }

    /**
     * Execute query on Es (with really no limit: {@link org.nuxeo.elasticsearch.query.NxQueryBuilder#limit(int) NxQueryBuilder.limit(int)}).
     * 
     * @param session
     * @param nxql
     * @return DocumentModelList fetched from DB
     */
    public static DocumentModelList query(CoreSession session, String nxql) {
        // Fetch docs from DB to get facets not defined in DocumentTypeImpl (they ares tored in hierachy table like isRemoteProxy)
        return query(session, nxql, 0, -1, false);
    }

    /**
     * Execute query on Es.
     * 
     * @param session
     * @param nxql
     * @param limit
     * @return DocumentModelList fetched from DB
     */
    public static DocumentModelList query(CoreSession session, String nxql, int limit) {
        // Fetch docs from DB to get facets not defined in DocumentTypeImpl (they ares tored in hierachy table like isRemoteProxy)
        return query(session, nxql, 0, limit, false);
    }

    /**
     * Execute paginated query on ES.
     * 
     * @param session
     * @param nxql
     * @param currentPageIndex
     * @param pageSize
     * @return DocumentModelList fetched from DB
     */
    public static DocumentModelList query(CoreSession session, String nxql, int currentPageIndex, int pageSize) {
        // Fetch docs from DB to get facets not defined in DocumentTypeImpl (they ares tored in hierachy table like isRemoteProxy)
        return query(session, nxql, currentPageIndex, pageSize, false);
    }

    /**
     * Execute paginated query on ES.
     * 
     * @param session
     * @param nxql
     * @param currentPageIndex
     * @param pageSize
     * @return DocumentModelList fetched from Es or from DB
     */
    public static DocumentModelList query(CoreSession session, String nxql, int currentPageIndex, int pageSize, boolean fetchDocFromEs) {
        // Builder
        NxQueryBuilder qB = new NxQueryBuilder(session).nxql(nxql);

        // Fetch documents from Es
        if (fetchDocFromEs) {
            qB.fetchFromElasticsearch();
        } else {
            qB.fetchFromDatabase();
        }
        // Pagination
        if(pageSize > 0 && currentPageIndex >= 0){
            qB.offset(currentPageIndex * pageSize);
            qB.limit(pageSize);
        } else {
            qB.limit(DEFAULT_MAX_RESULT_SIZE);
        }

        // Query
        return getElasticSearchService().query(qB);
    }


    public static class UnrestrictedQueryAndAggregate extends UnrestrictedSessionRunner {

        /** Session. */
        private CoreSession session;
        /** Query. */
        private String query;

        /** Limit of results. */
        private int limit = -1;
        /** Results rows. */
        private IterableQueryResult iqr;

        /** Constructor. */
        protected UnrestrictedQueryAndAggregate(CoreSession session) {
            super(session);
        }

        /** Constructor. */
        protected UnrestrictedQueryAndAggregate(CoreSession session, String query) {
            super(session);
            this.query = query;
        }

        /** Setter for nxql query. */
        public void setQuery(String query) {
            this.query = query;
        }

        /** Setter for limit of query's results. */
        public void setLimit(int limit) {
            this.limit = limit;
        }

        /** Getter fir iterable query results. */
        public IterableQueryResult getRowsResults() {
            return this.iqr;
        }

        @Override
        public void run() throws ClientException {
            if (StringUtils.isNotBlank(this.query)) {
                // ES query
                ElasticSearchService ess = ToutaticeEsQueryHelper.getElasticSearchService();

                NxQueryBuilder queryBuilder = new NxQueryBuilder(this.session).fetchFromElasticsearch().nxql(this.query).limit(this.limit);
                this.iqr = ess.queryAndAggregate(queryBuilder).getRows();
            } else {
                throw new ClientException("No query defined.");
            }
        }

    }

}
