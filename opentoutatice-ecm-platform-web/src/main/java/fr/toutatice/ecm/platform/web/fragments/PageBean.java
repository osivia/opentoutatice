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
 *   dchevrier
 *   lbillon
 *    
 */
package fr.toutatice.ecm.platform.web.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;

@Name("pageBean")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class PageBean implements Serializable {

	private static final long serialVersionUID = -8405154430255486965L;
	private static final Log log = LogFactory.getLog(PageBean.class);

	private static String REQUEST_PARAM_SEPARATOR = "=";
	private static final String CONVERSATION_ID = "conversationId"
			+ REQUEST_PARAM_SEPARATOR;
	private static final String CONVERSATION_ID_REGEXP = "conversationId=([^&]+)";

	@In(create = true, required = true)
	protected transient CoreSession documentManager;

	/** To register a conversation */
	private boolean isConversationStarted = false;
	private String conversationId;

	private String fromUrl;
	protected String notificationKey;
	protected String liveEditLink;
	
	/**
	 * identifiant url de provenance
	 */
	@RequestParameter("fromUrl")
	private String requestedFromUrl;

	@RequestParameter("docId")
	private String inputDocId;

	public String getNotificationKey() {
		String notif = notificationKey;
		setNotificationKey(null);
		return notif;
	}

	public void setNotificationKey(String notificationKey) {
		this.notificationKey = notificationKey;
	}

	public String getInputDocId() {
		return this.inputDocId;
	}

	public void setInputDocId(String inputDocId) {
		this.inputDocId = inputDocId;
	}

	/**
	 * @return the fromUrl
	 */
	public String getFromUrl() {
		return fromUrl;
	}

	/**
	 * @param fromUrl
	 *            the fromUrl to set
	 */
	public void setFromUrl(String fromUrl) {
		this.fromUrl = fromUrl;
	}

	public String getLiveEditLink() {
		DocumentRef docRef = new IdRef(this.inputDocId);
		try {
			DocumentModel document = documentManager.getDocument(docRef);
			this.liveEditLink = DocumentModelFunctions.liveEditUrl(document);
			if (isConversationStarted) {
				this.liveEditLink.replaceAll(CONVERSATION_ID_REGEXP,
						CONVERSATION_ID + conversationId);
			} else {
				isConversationStarted = true;
				conversationId = getConversationId(this.liveEditLink);
			}
			return this.liveEditLink;
		} catch (ClientException e) {
			log.error(e);
		}
		return null;
	}

	public void setLiveEditLink(String liveEditLink) {
		this.liveEditLink = liveEditLink;
	}
	
	@Create
	public void startUp() {
		// En mode édition, retourne l'uri passée en parmaètre
		if (requestedFromUrl != null) {
			fromUrl = requestedFromUrl;
		}
	}

	private String getConversationId(String url) {
		Pattern pattern = Pattern.compile(CONVERSATION_ID_REGEXP);
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			String conversationKeyValue = matcher.group(0);
			return conversationKeyValue.split(REQUEST_PARAM_SEPARATOR)[1];
		}
		return null;
	}

}
