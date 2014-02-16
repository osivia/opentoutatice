package fr.toutatice.ecm.platform.automation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.operations.services.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.core.GenericPageProviderDescriptor;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.cmis.query.ToutaticeCMISQueryDocumentPageProvider;

@Operation(id = QueryDocumentBaseOperation.ID, category = Constants.CAT_FETCH, label = "QueryDocumentBase", description = "Perform a query on the repository.")
public class QueryDocumentBaseOperation {
	public static final String ID = "QueryDocumentBase.PageProvider";

	public static final String CURRENT_USERID_PATTERN = "$currentUser";

	public static final String CURRENT_REPO_PATTERN = "$currentRepository";

	@Context
	protected CoreSession session;

	@Param(name = "providerName", required = false)
	protected String providerName;

	@Param(name = "query", required = true)
	protected String query=null;

	@Param(name = "docIdColumnName", required = true)
	protected String docIdColumnName=null;

	@Param(name = "page", required = false)
	protected Integer page= 0;

	@Param(name = "pageSize", required = false)
	protected Integer pageSize=null;

	@Param(name = "sortInfo", required = false)
	protected StringList sortInfoAsStringList;

	@Param(name = "queryParams", required = false)
	protected StringList strParameters;

	@SuppressWarnings("unchecked")
	@OperationMethod
	public DocumentModelList run() throws Exception {

		PageProviderService pps = Framework.getLocalService(PageProviderService.class);

		List<SortInfo> sortInfos = new ArrayList<SortInfo>();
		if (sortInfoAsStringList!=null) {
			for (String sortInfoDesc : sortInfoAsStringList) {
				SortInfo sortInfo;
				if (sortInfoDesc.contains(":")) {
					String[] parts = sortInfoDesc.split(":");
					sortInfo = new SortInfo(parts[0], Boolean.parseBoolean(parts[1]));
				} else {
					sortInfo = new SortInfo(sortInfoDesc, true);
				}
				sortInfos.add(sortInfo);
			}
		}

		Object[] parameters= null;

		if (strParameters!=null && strParameters.size()>0) {
			parameters = strParameters.toArray(new String[strParameters.size()]);
			// expand specific parameters
			for (int idx=0; idx< parameters.length; idx++) {
				String value = (String) parameters[idx];
				if (value.equals(CURRENT_USERID_PATTERN)) {
					parameters[idx]=session.getPrincipal().getName();
				}else if (value.equals(CURRENT_REPO_PATTERN)) {
					parameters[idx]=session.getRepositoryName();
				}
			}
		}

		Map<String , Serializable> props = new HashMap<String, Serializable>();
		props.put(ToutaticeCMISQueryDocumentPageProvider.CORE_SESSION_PROPERTY, (Serializable) session);
		props.put(ToutaticeCMISQueryDocumentPageProvider.DOC_ID_COLUMN_NAME_PROPERTY, (Serializable) docIdColumnName);

		Long targetPageSize = null;
		if (pageSize!=null) {
			targetPageSize = new Long(pageSize);
		}

		GenericPageProviderDescriptor desc = (GenericPageProviderDescriptor) pps.getPageProviderDefinition("CMIS_QUERY_DOCUMENT_PAGE_PROVIDER");
		desc.setPattern(query);
		return new PaginableDocumentModelListImpl((PageProvider<DocumentModel>) pps.getPageProvider(providerName, desc, sortInfos, targetPageSize, new Long(page), props, parameters));
	}
}
