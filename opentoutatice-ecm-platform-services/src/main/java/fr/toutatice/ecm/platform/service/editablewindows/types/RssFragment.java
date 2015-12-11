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
 * 	 lbillon
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.service.editablewindows.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.editablewindows.EwConstants;
import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;

/**
 * @author david
 *
 */
public class RssFragment implements EditableWindow {

	public static final String RSS_SCHEMA = "rss_fragments";

	/**
	 * {@inheritDoc}}
	 */
	@Override
	public String prepareCreation(DocumentModel doc, String uri, String region,
			String belowUri, String code2) throws EwServiceException {

		try {

			Map<String, Object> properties = doc.getProperties(RSS_SCHEMA);

			Collection<Object> values = properties.values();

			// Une seule liste dans ce schéma
			Object liste = values.iterator().next();

			if (liste instanceof List) {
				List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

				Map<String, String> newEntry = new HashMap<String, String>();

				newEntry.put(EwConstants.REF_URI, uri);

				listeData.add(newEntry);

				doc.setProperties(RSS_SCHEMA, properties);
			}

		} catch (ClientException e) {
			throw new EwServiceException(e);
		}
		return uri;
	}

}
