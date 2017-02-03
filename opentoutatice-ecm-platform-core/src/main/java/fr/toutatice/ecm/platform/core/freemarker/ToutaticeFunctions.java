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
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.core.freemarker;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nuxeo.ecm.automation.features.PlatformFunctions;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLBlob;
import org.nuxeo.ecm.platform.comment.api.CommentManager;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.services.permalink.PermaLinkService;

public class ToutaticeFunctions extends PlatformFunctions {
    
    /** Base Url protocol. */
    private static final String PROTOCOL_BASE_URL = "http://";
    /** Truncated HTML suffix. */
    private static final String TRUNCATED_HTML_SUFFIX = "<p> ... </p>";
    
	private static CommentManager commentManager;
	private static PermaLinkService permaLinkService;

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
		String url = StringUtils.EMPTY;
		// verification : le document doit pouvoir être visible dans toutatice
		if (ToutaticeDocumentHelper.isVisibleInPortal(doc, doc.getCoreSession())) {

			// si oui recherche du permalink
			if (null == permaLinkService) {
				try {
					permaLinkService = Framework.getService(PermaLinkService.class);
				} catch (Exception e) {
					throw new WebException("Unable to get publicationService");
				}
			}
			url = permaLinkService.getPermalink(doc);
		}
		
		return url;
	}
	
	public String getPortalHost(DocumentModel doc){
	    String host = StringUtils.EMPTY;
	    
	    if (ToutaticeDocumentHelper.isVisibleInPortal(doc, doc.getCoreSession())) {
	        
	        if (null == permaLinkService) {
                try {
                    permaLinkService = Framework.getService(PermaLinkService.class);
                } catch (Exception e) {
                    throw new WebException("Unable to get publicationService");
                }
            }
	        
	        host = permaLinkService.getPortalHost();
	        
	    }
	    
	    return host;
	}
	
	public String getShortPortalHost(String portalHost){
	    String shortHost = StringUtils.EMPTY;
	    
	    if(StringUtils.isNotBlank(portalHost)){
	        shortHost = StringUtils.substring(portalHost, PROTOCOL_BASE_URL.length());
	    }
	    
	    return shortHost;
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
	
	/**
	 * Truncate HTML according to its number of characters inside its
	 * text elements.
	 * 
	 * @param html
	 * @param nbChars
	 * @return truncatedHtml
	 */
	public String truncateHTML(String html, int nbChars) {
        String truncatedHtml = html;

        if (html != null) {
            // Document
            Document document = Jsoup.parse(html);

            // Get as text to count characters
            String text = document.text();

            if (text != null) {
                // Elements
                Elements elements = document.getAllElements();

                if (elements != null) {
                    // Truncate if necessary
                    for (int index = elements.size(); text.length() > nbChars && index > 0; index--) {
                        // Remove last element (containing text)
                        Element lastTextElement = elements.get(index -1);
                        
                        if (lastTextElement.text() != null) {
                            // Inner text
                            String innerText = lastTextElement.text();
                            if(innerText.length() > 0){
                                // Remove element content from text variable
                                text = StringUtils.substring(text, 0, text.length() - innerText.length());
                            }
                        }

                        // Remove element from DOM
                        lastTextElement.remove();
                    }

                    // Rebuild String
                    Elements body = document.getElementsByTag("body");
                    truncatedHtml = body.first().html() + TRUNCATED_HTML_SUFFIX;
                }
            }

        }

        return truncatedHtml;
    }
	
	/**
	 * @param username
	 * @return firstName + lastName of user.
	 * @throws Exception 
	 * @throws ClientException 
	 */
	public String getUserFullName(String username) {
	    String fullName = StringUtils.EMPTY;
	    NuxeoPrincipal principal;
        try {
            principal = (NuxeoPrincipal) super.getUserManager().getPrincipal(username);
            if(principal != null){
                fullName = principal.getFirstName().concat(" ").concat(principal.getLastName());
            }
        } catch (Exception e) {
            return fullName;
        }
        return fullName;
	}
	
}
