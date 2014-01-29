package fr.toutatice.ecm.platform.core.freemarker;

import java.util.List;

import org.jsoup.Jsoup;
import org.nuxeo.ecm.automation.features.PlatformFunctions;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;
import org.nuxeo.ecm.platform.comment.api.CommentManager;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.service.permalink.PermaLinkService;

public class ToutaticeFunctions extends PlatformFunctions {
	// private static final Log log = LogFactory.getLog(ToutaticeFunctions.class);
	private static CommentManager commentManager;

	private static PermaLinkService publicationService;

	/**
	 * @param doc
	 *            document
	 * @param xpath
	 *            xpath du blob
	 * @return url de download du blob identifier par son xpath dans le document
	 *         nuxeo
	 * @throws PropertyException
	 * @throws ClientException
	 */
	public String getDownloadFileUrl(DocumentModel doc, String xpath) throws PropertyException, ClientException {

		BlobProperty blob = (BlobProperty) doc.getProperty(xpath);
		String filename = ((SQLBlob) blob.getValue()).getFilename();
		String url = DocumentModelFunctions.fileUrl("downloadFile", doc, xpath, filename);

		return url;
	}

	/**
	 * @param le document
	 * @return permalink pour visualiser le document depuis le portail, si le document n'est pas visualisable la méthode retourne ""
	 * @throws PropertyException
	 * @throws ClientException
	 */
	public String getPermalink(DocumentModel doc) throws PropertyException, ClientException {
		String url = "";
		// verification : le document doit pouvoir être visible dans toutatice
		if (ToutaticeDocumentHelper.isVisibleInPortal(doc, doc.getCoreSession())) {

			// si oui recherche du permalink
			if (null == publicationService) {
				try {
					publicationService = Framework.getService(PermaLinkService.class);
				} catch (Exception e) {
					throw new WebException("Unable to get publicationService");
				}
			}

			url = publicationService.getPermalink(doc);
		}
		// sinon return null
		return url;
	}

	/**
	 * Récupére un commentaire sur le document.
	 * 
	 * @param le
	 *            document
	 * @param le
	 *            numéro du commentaire voulu. Si null, c'est le dernier
	 *            commentaire qui est retourné.
	 * @return le commentaire demandé
	 * @throws Exception
	 */
	public String getDocumentComments(DocumentModel doc, Integer noComment) throws Exception {
		String res = "";
		if (commentManager == null) {
			commentManager = getCommentManager();
		}
		List<DocumentModel> lstComments = commentManager.getComments(doc);
		int idxComment = lstComments.size() - 1;
		if (noComment != null && noComment < lstComments.size()) {
			idxComment = noComment;
		}

		DocumentModel derComment = lstComments.get(idxComment);
		res = (String) derComment.getPropertyValue("comment:text");
		return res;
	}

	private CommentManager getCommentManager() throws Exception {
		CommentManager commentManager = Framework.getService(CommentManager.class);
		if (commentManager == null) {
			throw new WebException("Unable to get commentManager");
		}
		return commentManager;
	}

	/**
	 * Transformer du texte html en texte simple
	 * 
	 * @param html
	 *            extrait à transformer
	 * @return texte
	 */
	public String extractTextFromHTML(String html) {
		return Jsoup.parse(html).text();
	}
}
