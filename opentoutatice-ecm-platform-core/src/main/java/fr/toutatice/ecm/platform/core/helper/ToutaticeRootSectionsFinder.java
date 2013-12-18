package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.publisher.impl.finder.DefaultRootSectionsFinder;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;

public class ToutaticeRootSectionsFinder extends DefaultRootSectionsFinder {
	
	private static final Log log = LogFactory.getLog(ToutaticeRootSectionsFinder.class);

	private static String CST_QUERY_LIST_PUBLISH_SPACES = "SELECT * FROM %s WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0";
	
	public static enum ROOT_SECTION_TYPE {
		LOCAL,
		HERITED,
		ALL;
	}
	
	protected List<String> unrestrictedSectionRootFromLocalConfig;
	protected DocumentModel unrestrictedSectionRootParentCfgOwner;

	public ToutaticeRootSectionsFinder(CoreSession userSession) {
		super(userSession);
	}

	@Override
	protected DocumentModelList getDefaultSectionRoots(CoreSession session) throws ClientException {
		DocumentModelList sectionRoots = new DocumentModelListImpl();

		for (String sectionRootType : getSectionRootTypes()) {
			DocumentModelList list = session.query(String.format(CST_QUERY_LIST_PUBLISH_SPACES, sectionRootType));

			/* filtrer les 'section roots' dont le parent est également un 'section root' afin que ceux-ci ne soient pas présentés dans
			 * l'IHM de configuration des sections de publication d'un espace de travail. 
			 * Seul le section root parent sera présenté. Le section root fils sera visible néanmoins via la présentation des sections 
			 * sous forme d'arbre par le widget.  
			 */
			UnrestrictedSessionRunner filter = new UnrestrictedFilterSectionRootsRunner(session, list, sectionRoots);
			filter.runUnrestricted();
		}

		return sectionRoots;
	}
	
	private static class UnrestrictedFilterSectionRootsRunner extends UnrestrictedSessionRunner {
		DocumentModelList list;
		DocumentModelList sectionRoots;

		protected UnrestrictedFilterSectionRootsRunner(CoreSession session, DocumentModelList list, DocumentModelList sectionRoots) {
			super(session);
			this.list = list;
			this.sectionRoots = sectionRoots;
		}
		
		@Override
		public void run() throws ClientException {
			for (DocumentModel sectionRoot : list) {
				try {
					DocumentModel sectionRootParent = session.getParentDocument(sectionRoot.getRef());
					if (!sectionRootParent.hasFacet(FacetNames.MASTER_PUBLISH_SPACE)) {
						this.sectionRoots.add(sectionRoot);
					}
				} catch (Exception e) {
					log.warn("Failed to filter the section roots, error: " + e.getMessage());
				}
			}
		}
	}
		
	/*
	 * Ajout de robustesse:
	 *    Gérer le cas où une section a été supprimée (définitivement) sans que les espaces de travail qui pointent dessus
	 * soient mis à jour en cache et/ou repository (retrait de la section de la métadonnée 'publish:sections').
	 * 
	 * @see org.nuxeo.ecm.platform.publisher.helper.RootSectionsFinder#getFiltredSectionRoots(java.util.List, boolean)
	 */
	@Override
	protected DocumentModelList getFiltredSectionRoots(List<String> rootPaths, boolean onlyHeads) throws ClientException {
		List<DocumentRef> filtredDocRef = new ArrayList<DocumentRef>();
		List<DocumentRef> trashedDocRef = new ArrayList<DocumentRef>();

		for (String rootPath : rootPaths) {
			try {
				DocumentRef rootRef = new PathRef(rootPath);
				if (userSession.hasPermission(rootRef, SecurityConstants.READ)) {
					filtredDocRef.add(rootRef);
				} else {
					// Nuxeo Jira #5236 : échapper les simples quotes dans les chemins 
					DocumentModelList accessibleSections = userSession.query(buildQuery(rootPath.replace("'", "\\'")));
					for (DocumentModel section : accessibleSections) {
						if (onlyHeads && ((filtredDocRef.contains(section.getParentRef())) || (trashedDocRef.contains(section.getParentRef())))) {
							trashedDocRef.add(section.getRef());
						} else {
							filtredDocRef.add(section.getRef());
						}
					}
				}
			} catch (Exception e) {
				log.warn("Failed to get the section root '" + rootPath + "', error: " + e.getMessage());
			}
		}
		
		DocumentModelList documents = userSession.getDocuments(filtredDocRef.toArray(new DocumentRef[filtredDocRef.size()]));
		return filterDocuments(documents);
	}
	
	@Override
    protected void computeUnrestrictedRoots(CoreSession session) throws ClientException {

		if (currentDocument != null) {
			/*
			 * Mantis #2993: tout containeur peut recevoir une configuration des sections de publication autorisées.
			 * Si le parent possède une configuration vide, il faut poursuivre la recherche d'un parent (jusqu'à l'élément root).
			 */
			unrestrictedSectionRootParentCfgOwner = getPublishingParent(session, currentDocument);
			DocumentModelList sectionRootsFromWorkspaceConfig = getSectionRootsFromWorkspaceConfig(unrestrictedSectionRootParentCfgOwner, session);
			unrestrictedSectionRootFromWorkspaceConfig = new ArrayList<String>();
			for (DocumentModel root : sectionRootsFromWorkspaceConfig) {
				unrestrictedSectionRootFromWorkspaceConfig.add(root.getPathAsString());
			}
		}
		
		if (unrestrictedDefaultSectionRoot == null) {
			unrestrictedDefaultSectionRoot = Collections.emptyList();
		}
	}

    protected void computeUnrestrictedLocalRoots(CoreSession session) throws ClientException {

		if (currentDocument != null) {
			DocumentModelList sectionRootsFromWorkspaceConfig = getSectionRootsFromWorkspaceConfig(currentDocument, session);
			unrestrictedSectionRootFromLocalConfig = new ArrayList<String>();
			for (DocumentModel root : sectionRootsFromWorkspaceConfig) {
				unrestrictedSectionRootFromLocalConfig.add(root.getPathAsString());
			}
		}
		
	}

    @Override
    public DocumentModelList getSectionRootsForWorkspace(DocumentModel currentDoc, boolean addDefaultSectionRoots) throws ClientException {
    	return getSectionRootsForWorkspace(currentDoc, addDefaultSectionRoots, ROOT_SECTION_TYPE.ALL);
    }

    public DocumentModelList getSectionRootsForWorkspace(DocumentModel currentDoc, boolean addDefaultSectionRoots, ROOT_SECTION_TYPE sectionListType) throws ClientException {
        if ((currentDocument == null) || (!currentDocument.getRef().equals(currentDoc.getRef()))) {
            computeUserSectionRoots(currentDoc);
        }

        if (unrestrictedDefaultSectionRoot.isEmpty() && addDefaultSectionRoots) {
            if (unrestrictedDefaultSectionRoot == null
                    || unrestrictedDefaultSectionRoot.isEmpty()) {
                DocumentModelList defaultSectionRoots = getDefaultSectionRoots(session);
                unrestrictedDefaultSectionRoot = new ArrayList<String>();
                for (DocumentModel root : defaultSectionRoots) {
                    unrestrictedDefaultSectionRoot.add(root.getPathAsString());
                }
            }
        }
        
        List<String> agregatedList = new ArrayList<String>();
        if (ROOT_SECTION_TYPE.ALL.equals(sectionListType) || ROOT_SECTION_TYPE.HERITED.equals(sectionListType)) {
        	agregatedList.addAll(unrestrictedSectionRootFromWorkspaceConfig);
        }
        if (ROOT_SECTION_TYPE.ALL.equals(sectionListType) || ROOT_SECTION_TYPE.LOCAL.equals(sectionListType)) {
        	agregatedList.addAll(unrestrictedSectionRootFromLocalConfig);
        }
        return getFiltredSectionRoots(agregatedList, true);
    }

    public DocumentModelList getSectionRootsForLocal(DocumentModel currentDoc, boolean addDefaultSectionRoots) throws ClientException {
        if ((currentDocument == null) || (!currentDocument.getRef().equals(currentDoc.getRef()))) {
            computeUserSectionRoots(currentDoc);
        }
        
        return getFiltredSectionRoots(unrestrictedSectionRootFromLocalConfig, true);
    }
    
    public DocumentModel getParentCfgOwner() {
    	return this.unrestrictedSectionRootParentCfgOwner;
    }

	private DocumentModel getPublishingParent(CoreSession session, DocumentModel document) throws ClientException {
		if (!NuxeoStudioConst.CST_DOC_TYPE_ROOT.equals(document.getType())) {
			document = session.getDocument(document.getParentRef());
		}
		
		if (document.hasSchema(SCHEMA_PUBLISHING)) {
			String[] sectionIdsArray = (String[]) document.getPropertyValue(SECTIONS_PROPERTY_NAME);
			if (null != sectionIdsArray && 0 < sectionIdsArray.length) {
				return document;
			}
		}
		
		if (!NuxeoStudioConst.CST_DOC_TYPE_ROOT.equals(document.getType())) {
			document = getPublishingParent(session, document);
		}
				
		return document;
	}
	
    @Override
    public void run() throws ClientException {
        computeUnrestrictedRoots(session);
        computeUnrestrictedLocalRoots(session);
    }
}
