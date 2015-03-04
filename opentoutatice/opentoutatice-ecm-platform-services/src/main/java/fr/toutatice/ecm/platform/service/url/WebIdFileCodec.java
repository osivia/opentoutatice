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
 * dchevrier
 * lbillon
 * 
 */
package fr.toutatice.ecm.platform.service.url;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;

/**
 * @author David Chevrier
 * 
 */
public class WebIdFileCodec extends WebIdCodec {

	public static final String WEBID_FILE_PREFIX = "webpicsfile/";

	@Override
	public String getPrefix() {
		if (prefix != null) {
			return prefix;
		}
		return WEBID_FILE_PREFIX;
	}

	@Override
	public DocumentView getDocumentViewFromUrl(String url) {

		DocumentView webView = super.getDocumentViewFromUrl(url.replace(
				WEBID_FILE_PREFIX, WebIdCodec.PREFIX));
		if (webView != null) {
			ToutaticeDocumentLocation docLoc = (ToutaticeDocumentLocation) webView
					.getDocumentLocation();
			WedIdRef webIdRef = docLoc.getWebIdRef();
			String webid = (String) webIdRef.reference();
			Map<String, String> webviewParameters = webView.getParameters();
			Map<String, String> parameters = new HashMap<String, String>();
			String content = webviewParameters.get(CONTENT_PARAM);
			if (content != null) {
				/*
				 * content is always defined for Picture in case of tiny-mce
				 * source link
				 */
				parameters.put(FILE_PROPERTY_PATH_KEY,
						content.concat(":content"));
			} else {
				parameters.put(FILE_PROPERTY_PATH_KEY, "Original:content");
			}
			parameters.put(FILENAME_KEY, webid);
			parameters.putAll(webviewParameters);

			return new DocumentViewImpl(docLoc, null, parameters);
		}
		return webView;
	}

	@Override
	public String getUrlFromDocumentView(DocumentView docView) {

		String webUrl = super.getUrlFromDocumentView(docView);
		if (webUrl != null) {
			webUrl = webUrl.replace(WebIdCodec.PREFIX, WEBID_FILE_PREFIX);
		}
		return webUrl;

	}

}
