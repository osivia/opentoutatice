package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublicationTreeNotAvailable;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.publisher.helper.RootSectionFinder;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

public class ToutaticePublishHelper {

	private static final Log log = LogFactory.getLog(ToutaticePublishHelper.class);

	public static PublicationTree getCurrentPublicationTreeForPublishing(DocumentModel doc, PublisherService ps, CoreSession session)
			throws ClientException {
		PublicationTree currentPublicationTree = null;
		if (log.isDebugEnabled()) {
			log.debug(" ----> getCurrentPublicationTreeForPublishing ");
		}
		String currentPublicationTreeNameForPublishing = null;

		List<String> publicationTrees = new ArrayList<String>(ps.getAvailablePublicationTree());

		publicationTrees = filterEmptyTrees(publicationTrees, doc, ps, session);
		if (!publicationTrees.isEmpty()) {
			currentPublicationTreeNameForPublishing = publicationTrees.get(0);
		}

		if (currentPublicationTreeNameForPublishing != null) {
			try {
				currentPublicationTree = ps.getPublicationTree(currentPublicationTreeNameForPublishing, session, null, doc);
			} catch (PublicationTreeNotAvailable e) {
				currentPublicationTree = null;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(" <---- getCurrentPublicationTreeForPublishing : " + currentPublicationTree.getName());
		}
		return currentPublicationTree;
	}

	public static DocumentModel getFirstSection(DocumentModel doc, PublisherService ps, CoreSession session) throws ClientException {

		ToutaticeRootSectionsFinder rootFinder = (ToutaticeRootSectionsFinder) ps.getRootSectionFinder(session);
		DocumentModel target = null;
		// récupération du permier espace de publication définit pour le
		// document

		DocumentModelList targetList = rootFinder.getSectionRootsForWorkspace(doc, true,
				ToutaticeRootSectionsFinder.ROOT_SECTION_TYPE.valueOf("ALL"));
		if (targetList != null && !targetList.isEmpty()) {
			target = targetList.get(0);
			if (log.isDebugEnabled()) {
				log.debug(" -> premier espace de publication définit " + target.getName());
			}
		} else {
			throw new ClientException("Aucun espace de publication n'est défini pour ce document ");
		}
		return target;
	}

	public static List<String> filterEmptyTrees(Collection<String> trees, DocumentModel doc, PublisherService ps, CoreSession session)
			throws PublicationTreeNotAvailable, ClientException {
		List<String> filteredTrees = new ArrayList<String>();

		for (String tree : trees) {

			PublicationTree pTree = ps.getPublicationTree(tree, session, null, doc);
			if (pTree != null) {
				if (pTree.getTreeType().equals("RootSectionsPublicationTree")) {
					if (pTree.getChildrenNodes().size() > 0) {
						filteredTrees.add(tree);
					}
				} else {
					filteredTrees.add(tree);
				}
			}
		}
		return filteredTrees;
	}

	public static DocumentModelList getSelectedSections(String type, NavigationContext navigationContext, RootSectionFinder rsf) {
		DocumentModelList list = new DocumentModelListImpl();

		try {
			DocumentModel currentDocument = navigationContext.getCurrentDocument();
			return ((ToutaticeRootSectionsFinder) rsf).getSectionRootsForWorkspace(currentDocument, true,
					ToutaticeRootSectionsFinder.ROOT_SECTION_TYPE.valueOf(type));
		} catch (Exception e) {
			log.warn("Failed to list the sections, error: " + e.getMessage());
		}

		return list;
	}

}
