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
package fr.toutatice.ecm.platform.service.editablewindows.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;

/**
 * 
 * Service dédié aux listes
 * 
 */
public class LinksFragment implements EditableWindow {

	private static final String LINKS_FRAG_SCHEMA1 = "links_fragments";
	private static final String LINKS_SCHEMA = "links";

	@Override
	public String prepareCreation(DocumentModel doc, String uri, String region,
			String belowUri, String code2) throws EwServiceException {
		Map<String, Object> properties;
		Map<String, Object> links;
		try {

			links = doc.getProperties(LINKS_SCHEMA);

			Collection<Object> values = links.values();

			// Une seule liste dans ce schéma
			Object liste = values.iterator().next();

			if (liste instanceof List) {
				List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

				Map<String, String> newEntry = new HashMap<String, String>();
				newEntry.put("refURI", uri);

				listeData.add(newEntry);

				Map<String, String> newEntry1 = new HashMap<String, String>();
				newEntry1.put("refURI", uri);

				listeData.add(newEntry1);

				Map<String, String> newEntry2 = new HashMap<String, String>();
				newEntry2.put("refURI", uri);

				listeData.add(newEntry2);

				Map<String, String> newEntry3 = new HashMap<String, String>();
				newEntry3.put("refURI", uri);

				listeData.add(newEntry3);

				Map<String, String> newEntry4 = new HashMap<String, String>();
				newEntry4.put("refURI", uri);

				listeData.add(newEntry4);

				doc.setProperties(LINKS_SCHEMA, links);
			}

			properties = doc.getProperties(LINKS_FRAG_SCHEMA1);

			
			values = properties.values();

			// Une seule liste dans ce schéma
			liste = values.iterator().next();

			if (liste instanceof List) {
				List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

				Map<String, String> newEntry = new HashMap<String, String>();

				newEntry.put("refURI", uri);

				listeData.add(newEntry);

				doc.setProperties(LINKS_FRAG_SCHEMA1, properties);
			}

		} catch (ClientException e) {
			throw new EwServiceException(e);
		}
		return uri;
	}

}
