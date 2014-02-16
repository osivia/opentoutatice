package fr.toutatice.ecm.platform.cmis.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.query.api.PageProviderDefinition;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;

import fr.toutatice.ecm.platform.core.helper.ToutaticeSQLQueryHelper;

/**
 * Simple PageProvider implementation that uses the CMISQL api to be able to
 * perform paginated joins.
 * 
 * @author mberhaut1
 */
public class ToutaticeCMISQueryDocumentPageProvider extends CoreQueryDocumentPageProvider {

	/** generated serial version */
	private static final long serialVersionUID = -2770901112785949328L;

	public static final Log log = LogFactory.getLog(ToutaticeCMISQueryDocumentPageProvider.class);

	public static final String DOC_ID_COLUMN_NAME_PROPERTY = "docIdColumnName";

	@Override
	public List<DocumentModel> getCurrentPage() {
		DocumentModelList result = null;

		if (currentPageDocuments == null) {
			currentPageDocuments = new ArrayList<DocumentModel>();
			CoreSession coreSession = getCoreSession();
			String docIdColumnName = getDocIdColumnName();

			try {
				if (query == null) {
					buildQuery(coreSession);
				}

				result = ToutaticeSQLQueryHelper.instance().query(coreSession, query, this.pageSize, this.offset, docIdColumnName);

				resultsCount = result.totalSize();
				currentPageDocuments = result;

				if (log.isDebugEnabled()) {
					log.debug("Query='" + query + "', Result count=" + resultsCount);
				}
			} catch (Exception e) {
				throw new ClientRuntimeException(e);
			}
		}

		return currentPageDocuments;
	}

	@Override
	protected void buildQuery(CoreSession coreSession) {
		PageProviderDefinition def = getDefinition();
		this.query = def.getPattern();
	}

	protected String getDocIdColumnName() {
		Map<String, Serializable> props = getProperties();
		String docIdColumnName = (String) props.get(DOC_ID_COLUMN_NAME_PROPERTY);
		if (docIdColumnName == null) {
			throw new ClientRuntimeException("cannot find document identifier column");
		}
		return docIdColumnName;    	
	}
}
