package fr.toutatice.ecm.platform.core.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.opencmis.impl.server.CMISQLQueryMaker;
import org.nuxeo.ecm.core.opencmis.impl.server.NuxeoCmisService;
import org.nuxeo.ecm.core.opencmis.impl.server.NuxeoRepository;

import fr.toutatice.ecm.platform.core.utils.exception.ToutaticeException;


public final class ToutaticeSQLQueryHelper {
	private static final Log log = LogFactory.getLog(ToutaticeSQLQueryHelper.class);

	private static ToutaticeSQLQueryHelper instance;
	private static NuxeoRepository repository;

	public static final long CST_PAGE_SIZE_UNLIMITED = 0;
	public static final String CST_DEFAULT_ID_COLUMN_NAME = "cmis:objectId";

	public static int STATIC_SQL_CRITERIA_LIVE_VERSION = 1;
	public static int STATIC_SQL_CRITERIA_NOT_DELETED = 1 << 1;
	public static int STATIC_SQL_CRITERIA_NOT_HIDDEN_TO_NAVIGATION = 1 << 2;
	public static int STATIC_SQL_CRITERIA_ALL = STATIC_SQL_CRITERIA_LIVE_VERSION | STATIC_SQL_CRITERIA_NOT_DELETED | STATIC_SQL_CRITERIA_NOT_DELETED;

	private ToutaticeSQLQueryHelper() {
	}

	public static ToutaticeSQLQueryHelper instance() throws ToutaticeException {
		if (null == instance) {
			instance = new ToutaticeSQLQueryHelper();
		}
		return instance;
	}

	/**
	 * Execute the query via the CMIS service. The query must comply with the CMISQL language.
	 * 
	 * @param session the core session
	 * @param query the query to execute (CMIS compliant)
	 * @param limit the max number of result to return
	 * @param offset the pagination offset 
	 * @param docIdColumnName the column name identifying the document reference
	 * @return the result set as a documents list (empty if the query has no result)
	 */
	public DocumentModelList query(CoreSession session, String query, long limit, long offset, String docIdColumnName) throws ToutaticeException {
		ArrayList<DocumentModel> resultDocList = new ArrayList<DocumentModel>();
		IterableQueryResult result = null;
		NuxeoCmisService cmisService = null;
		long resultsCount = 0;

		try {
			// get the cmis service
			cmisService = getCmisService(session);

			// execute the query
			result = query(session, cmisService, query);
			resultsCount = result.size();

			// make pagination
			if (offset < resultsCount) {
				result.skipTo(offset);
			}

			Iterator<Map<String, Serializable>> itr = result.iterator();
			int pos = 0;
			while (itr.hasNext() && (limit == CST_PAGE_SIZE_UNLIMITED || pos < limit)) {
				pos += 1;
				Map<String, Serializable> selectedAttributes = itr.next();
				Serializable docId = selectedAttributes.get(docIdColumnName);
				if (null == docId) {
					String message = "The attribute name '" + docIdColumnName + "' doesn't belong to the doc attributes (migth be missing the table alias name)";
					if (log.isDebugEnabled()) {
						log.debug(message);
					}
					throw new ClientRuntimeException(message);
				}
				DocumentRef docRef = new IdRef(docId.toString());
				DocumentModel doc = session.getDocument(docRef);
				resultDocList.add(doc);
			}
		} catch (Exception e) {
			throw new ClientRuntimeException(e);
		} finally {
			if (cmisService != null) {
				cmisService.close();
			}

			if (result != null) {
				result.close();
			}
		}

		return new DocumentModelListImpl(resultDocList, (resultsCount != resultDocList.size()) ? resultsCount : -1);
	}

	/**
	 * Execute the query via the CMIS service. The query must complies with the CMISQL language.
	 * 
	 * !IMPORTANT! the calling application is responsible for closing the returned result set  
	 * 
	 * @param session the core session
	 * @param query the CMIS query
	 * @return the query result set (iterable) or null if any error occurred
	 * @throws ToutaticeException if any error occurred while executing the query
	 */
	public IterableQueryResult query(CoreSession session, String query) throws ToutaticeException {
		NuxeoCmisService service = getCmisService(session);
		
		IterableQueryResult result = query(session, service, query);
		
		if (service != null)
			service.close();
		
		return result;
	}

	public IterableQueryResult query(CoreSession session, NuxeoCmisService service, String query) throws ToutaticeException {
		IterableQueryResult result = null;

		try {
			result = session.queryAndFetch(query, CMISQLQueryMaker.TYPE, service);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to execute the query '" + query + "', error: " + e.getMessage());
			}
			throw new ToutaticeException(e);
		}

		return result;
	}

	/**
	 * Add to the query the static criteria to filter the results according to :
	 * <ul>
	 * 	<li> hidden from navigation
	 * 	<li> get only the current version of the document, not its history
	 *  <li> document is not deleted
	 * </ul>
	 * 
	 * @param alias the document alias to use to prefix the where clause predicates
	 * @param criteria the static criteria to use. Selection must be among:
	 * 	<li> STATIC_SQL_CRITERIA_LIVE_VERSION: history is discarded. only the live (latest) version is selected</li> 
	 * 	<li> STATIC_SQL_CRITERIA_NOT_DELETED: documents with deleted status are discarded</li> 
	 * 	<li> STATIC_SQL_CRITERIA_NOT_HIDDEN_TO_NAVIGATION: documents hidden to the navigation are discarded</li> 
	 * @return the where clause predicates that deal with the static document criteria
	 */
	public String getDefaultStaticCriteria(String alias, int criteria) {
		String prefix = "";
		List<String> constraints = new ArrayList<String>();

		if (StringUtils.isNotBlank(alias)) {
			prefix = alias + ".";
		}

		if (0 < (criteria & STATIC_SQL_CRITERIA_LIVE_VERSION)) {
			constraints.add(prefix + "nuxeo:isVersion = false");
		}

		if (0 < (criteria & STATIC_SQL_CRITERIA_NOT_DELETED)) {
			constraints.add(prefix + "nuxeo:lifecycleState <> 'deleted'");
		}

		if (0 < (criteria & STATIC_SQL_CRITERIA_NOT_HIDDEN_TO_NAVIGATION)) {
			constraints.add(prefix + "ANY nuxeo:secondaryObjectTypeIds NOT IN ('HiddenInNavigation')");
		}

		return StringUtils.join(constraints.toArray(), " AND ");
	}

	private NuxeoCmisService getCmisService(CoreSession session) throws ToutaticeException {
		NuxeoCmisService service = null;

		try {
			getRepository(session);
			CallContext context = new CallContextImpl(CallContext.BINDING_LOCAL, session.getRepositoryName(), true);

			service = new NuxeoCmisService(repository, context, session);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to obtain the CMIS service, error: " + e.getMessage());
			}
			throw new ToutaticeException(e);
		}

		return service;
	}

	/**
	 * Return the Nuxeo repository
	 * 
	 * Note: the nuxeo repository is cached since:
	 * <li> the cost to build the instance is high </li>
	 * <li> it is common to all sessions </li>
	 * 
	 * @param session the current session
	 * @throws ToutaticeException if failed to obtain the repository
	 */
	private void getRepository(CoreSession session) throws ToutaticeException {
		try {
			if (repository == null) {
				String rootDocId = session.getRootDocument().getId();
				repository = new NuxeoRepository(session.getRepositoryName(), rootDocId);			
			}
		} catch (ClientException e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to obtain the repository, error: " + e.getMessage());
			}
			throw new ToutaticeException(e);
		}
	}

}
