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
 */
package fr.toutatice.ecm.platform.web.webeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.webapp.action.EditorLinkActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSorterHelper;
import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

@Name("editorLinkActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeEditorLinkActionsBean extends EditorLinkActionsBean {

    private static final String TOUT = "TOUT";

    private static final String DOMAIN = "DOMAIN";

    private static final String ESPACE = "ESPACE";

    private static final String MEDIALIBRARY = "MEDIALIBRARY";

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ToutaticeEditorLinkActionsBean.class);

    private static final String SEARCH_QUERY = "SELECT * FROM Document WHERE %s";

    private String typeDoc = "TOUS";
    private String scope = ESPACE;
    private Map<String, Object> types;
    private Map<String, String> scopes;
    
    /** Internationalization properties. */
    @In(create = true)
    protected Map<String, String> messages;

    private List<DocumentModel> resultDocuments;
    private String searchKeywords;

    private boolean hasSearchResults = false;

    private DocumentModel mediaSpace;

    @In(create = true, required = false)
    private CoreSession documentManager;

    @In(create = true, required = false)
    private SchemaManager schemaManager;


    public String getMediaSpaceName() throws ClientException {

        if (getMediaSpace() != null) {
            return getMediaSpace().getTitle();
        } else
            return null;

    }


    private DocumentModel getMediaSpace() throws ClientException {
        mediaSpace = null;
        DocumentModel currentDomain = navigationContext.getCurrentDomain();
        if (currentDomain != null) {
            String searchMediaLibraries = "ecm:primaryType = 'MediaLibrary' and ecm:path startswith '" + currentDomain.getPathAsString()
                    + "' and ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'";

            String queryMediaLibraries = String.format(SEARCH_QUERY, searchMediaLibraries);

            DocumentModelList query = documentManager.query(queryMediaLibraries);
            
            if(query.size() == 1){
                mediaSpace = query.get(0);
            }
        }
        return mediaSpace;
    }

    private String getSpaceName() throws ClientException {
        String res = null;
        DocumentModel space;
        space = navigationContext.getCurrentSuperSpace();
        if (space != null) {
            res = space.getTitle();
        }
        return res;
    }

    private String getDomaineName() throws ClientException {
        DocumentModel domain;
        String res = null;
        domain = navigationContext.getCurrentDomain();
        if (domain != null) {
            res = domain.getTitle();
        }
        return res;
    }

    public Map<String, String> getScopes() throws ClientException {
        scopes = new HashMap<String, String>();
        if (getMediaSpace() != null) {
            scopes.put(getMediaSpaceName(), MEDIALIBRARY);
        }
        scopes.put(getSpaceName(), ESPACE);
        scopes.put(getDomaineName(), DOMAIN);
        scopes.put("Tout nuxeo", TOUT);

        return scopes;
    }

    public void setScopes(Map<String, String> scopes) {
        this.scopes = scopes;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    public String getTypeDoc() {
        return typeDoc;
    }

    public void setTypeDoc(String typeDoc) {
        this.typeDoc = typeDoc;
    }

    public List<DocumentModel> getResultDocuments() {
        return resultDocuments;
    }

    public void setTypes(Map<String, Object> types) {
        this.types = types;

    }

    public Map<String, Object> getTypes() throws ClientException {
        types = new TreeMap<String, Object>();
        Collection<Type> collectTypes = typeManager.getTypes();
        
        if (MEDIALIBRARY.equals(scope)) {
            collectTypes = typeManager.findAllAllowedSubTypesFrom(getMediaSpace().getType(), getMediaSpace());
        } else if (ESPACE.equals(scope)) {
            collectTypes = typeManager.findAllAllowedSubTypesFrom(navigationContext.getCurrentSuperSpace().getType(), navigationContext.getCurrentSuperSpace());
        } else if (DOMAIN.equals(scope)) {
            collectTypes = typeManager.findAllAllowedSubTypesFrom(navigationContext.getCurrentDomain().getType(), navigationContext.getCurrentDomain());
        } else {
            collectTypes.clear();
        }
        
        List<Type> lstType = new ArrayList<Type>(collectTypes);
        types.put("Tous", "TOUS");
        
        for (Type type : lstType) {
            if ("SimpleDocument".equalsIgnoreCase(type.getCategory())) {
                String label = this.messages.get(type.getId());
                types.put(label, type.getId());
            }
        }

        return types;
    }

    @Override
    public List<DocumentModel> getSearchDocumentResults() {
        return resultDocuments;
    }

    public void setResultDocuments(List<DocumentModel> resultDocuments) {
        this.resultDocuments = resultDocuments;
    }

    public String getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    @Override
    public boolean getHasSearchResults() {
        return hasSearchResults;
    }
    
    public String searchDocuments(String view) throws ClientException{
        searchDocuments();
        return view;
    }

    @Override
    public String searchDocuments() throws ClientException {
        resultDocuments = null;
        final List<String> constraints = new ArrayList<String>();
        
        final int queryLimitResults = 101;

        // filter: path
        if (MEDIALIBRARY.equals(scope)) {
            constraints.add("ecm:path STARTSWITH '" + getMediaSpace().getPathAsString().replace("'", "\\'") + "'");
        }
        if (ESPACE.equals(scope)) {
            constraints.add("ecm:path STARTSWITH '" + navigationContext.getCurrentSuperSpace().getPathAsString().replace("'", "\\'") + "'");
        } else if (DOMAIN.equals(scope)) {
            constraints.add("ecm:path STARTSWITH '" + navigationContext.getCurrentDomain().getPathAsString().replace("'", "\\'") + "'");
        }

        // filter: document type
        if (typeDoc != null && !"TOUS".equals(typeDoc)) {
            constraints.add("ecm:primaryType = '" + getTypeDoc() + "'");
        }

        // filter: keywords
        if (searchKeywords != null) {
            searchKeywords = searchKeywords.trim();
            if (searchKeywords.length() > 0) {
                if (!searchKeywords.equals("*")) {
                    // full text search
                    constraints.add(String.format("ecm:fulltext LIKE '%s%%'", searchKeywords.replace("'", "\\'")));
                }
            }
        }

        // filter: no folderish doc nor hidden doc
        constraints.add("ecm:mixinType <> 'HiddenInNavigation' and ecm:mixinType <> 'OttcDraft'");

        // no archived, revisions, deleted
        constraints.add("(ecm:mixinType = 'isRemoteProxy' or ecm:isProxy = 0) AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState!='deleted'");

        // search keywords
        final String query = String.format("SELECT * FROM Document WHERE %s", StringUtils.join(constraints.toArray(), " AND "));
        log.debug("Query: " + query);
        
        // resultDocuments = documentManager.query(query, queryLimitResults);
        final long begin = System.currentTimeMillis();

        resultDocuments = ToutaticeEsQueryHelper.query(documentManager, query, 0, queryLimitResults);

        if (log.isDebugEnabled()) {
            final long end = System.currentTimeMillis();
            log.debug("query: " + String.valueOf(end - begin) + " ms");
        }

        hasSearchResults = !resultDocuments.isEmpty();

        if (log.isDebugEnabled()) {
            log.debug("query result contains: " + resultDocuments.size() + " docs.");
        }

        return "editor_link_search_document";
    }

    private class ListTypeComparator extends ToutaticeSorterHelper<Type> {

        @Override
        public String getComparisionString(Type type) {
            String stg = StringUtils.EMPTY;

            try {
                stg = type.getLabel();
            } catch (Exception e) {
                log.error("Failed to extract the comparision string to Type, error:" + e.getMessage());
            }

            return stg;
        }

    }

   /**
    * @param document
    * @return list of remote proxies of document (if any).
    */
    public DocumentModelList getRemotePublishedDocuments(DocumentModel document) {
        return ToutaticeDocumentHelper.getRemotePublishedDocuments(this.documentManager, document);
    }

}
