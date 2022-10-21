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
package fr.toutatice.ecm.platform.services.permalink;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.Serializable;
import java.util.Map;

public class PermalinkByWebid implements Permalink
{
	private static final String SEPARATOR = "/";

	@Override
	public String getPermalink(DocumentModel doc, String host, Map<String,String> params)
	{
		StringBuilder permalinkResult = new StringBuilder();
		permalinkResult.append(host);
		permalinkResult.append(SEPARATOR);
		for (String param : params.values()) {
			permalinkResult.append(param);
			permalinkResult.append(SEPARATOR);
		}

		Serializable webid = doc.getPropertyValue("ttc:webid");

		// recherche du path du doc
		//String docPath = doc.getPath().toString();
		if(webid == null){
			//throw new NullPointerException(String.format("The document webid is null. Document's id='%s'", doc.getId()));
			return StringUtils.EMPTY;
		} else {
			return permalinkResult.append(webid.toString()).toString();
		}

	}

}
