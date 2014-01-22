package fr.toutatice.ecm.platform.web.publication;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.publisher.web.PublishActionsBean;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.web.annotations.Install;

@Name("publishActions")
@Scope(CONVERSATION)
@Install(precedence = Install.TOUTATICE)
public class ToutaticePublishActionsBean extends PublishActionsBean {

	private static final Log log = LogFactory.getLog(ToutaticePublishActionsBean.class);

	private static final long serialVersionUID = 1L;

	public boolean getCanPublish() throws ClientException {
		DocumentModel doc = navigationContext.getCurrentDocument();
		return getCanPublish(doc);
	}

	public boolean getCanPublish(DocumentModel document) throws ClientException {
		boolean status = false;

		try {
			if (null != document) {
				PublicationTree tree = getCurrentPublicationTreeForPublishing();
				DocumentModel parent = documentManager.getParentDocument(document.getRef());
				if (null != parent) {
					status = true;

					// vérifier qu'il n'existe pas un processus en cours

					// vérifier que le document n'est pas déjà en ligne avec la
					// même version
					DocumentModelList proxies = documentManager.getProxies(document.getRef(), parent.getRef());
					if ((proxies != null) && (!proxies.isEmpty())) {
						DocumentModel proxy = proxies.get(0);
						if (proxy.getVersionLabel().equals(document.getVersionLabel())) {
							status = false;
						}
					}

					// vérifier que l'arbre de publication autorise la mise en
					// ligne
					if (true == status) {
						PublicationNode target = tree.getNodeByPath(parent.getPathAsString());
						status = super.canPublishTo(target);
					}
				}
			}
		} catch (Exception e) {
			log.debug("Failed to check the permission to publish the document '" + document.getTitle() + "', error: " + e.getMessage());
		}

		return status;
	}

	public boolean getCanUnpublish() throws ClientException {
		DocumentModel doc = navigationContext.getCurrentDocument();
		return getCanUnpublish(doc);
	}

	public boolean canUnpublishProxy(DocumentModel proxy) throws ClientException {
		boolean status = false;

		if (proxy.isProxy() && documentManager.hasPermission(proxy.getRef(), NuxeoStudioConst.CST_PERM_VALIDATE)) {
			PublishedDocument publishedDocument = new SimpleCorePublishedDocument(proxy);
			status = super.canUnpublish(publishedDocument);
		}

		return status;
	}

	public boolean getCanUnpublish(DocumentModel document) throws ClientException {
		boolean status = false;

		try {
			if (null != document) {
				DocumentRef parentRef = documentManager.getParentDocumentRef(document.getRef());
				if (null != parentRef) {
					DocumentModelList proxies = documentManager.getProxies(document.getRef(), parentRef);
					if ((proxies != null) && (!proxies.isEmpty())) {
						DocumentModel proxy = proxies.get(0);
						status = canUnpublishProxy(proxy);
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to check the permission to unpublish the document '" + document.getTitle() + "', error: " + e.getMessage());
		}

		return status;
	}

	public boolean getCanPublishSelection() {
		boolean status = false;

		try {
			List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
			for (DocumentModel selectedDocument : currentDocumentSelection) {
				status = getCanPublish(selectedDocument);
				if (false == status) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("Failed to check permission to publish the selection, error: " + e.getMessage());
		}

		return status;
	}

	public boolean getCanUnPublishSelection() {
		boolean status = false;

		try {
			List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
			for (DocumentModel selectedDocument : currentDocumentSelection) {
				status = getCanUnpublish(selectedDocument);
				if (false == status) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("Faild to check permission to unpublish the selection, error: " + e.getMessage());
		}

		return status;
	}

}