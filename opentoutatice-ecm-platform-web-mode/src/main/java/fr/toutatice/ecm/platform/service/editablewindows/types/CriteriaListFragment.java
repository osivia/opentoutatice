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
 *   lbillon
 *   dchevrier
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.service.editablewindows.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.service.editablewindows.EwConstants;
import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;
import fr.toutatice.ecm.platform.service.fragments.helpers.CriteriaListBeanHelper;

/**
 * @author david
 *
 */
public class CriteriaListFragment implements EditableWindow {
	
	public static final String CRITERIA_LIST_SCHEMA = "criteria_list_fragments";
	public static final String CRITERIA_LIST_XPATH = "crtlistfgt:criteriaListFragment";
	
	/**
	 * {@inheritDoc}}
	 */
	@Override
	public String prepareCreation(DocumentModel document, String uri, String region,
			String belowUri, String code2) throws EwServiceException {
		try {

			Map<String, Object> properties = document.getProperties(CRITERIA_LIST_SCHEMA);
			Collection<Object> values = properties.values();

			// One list in this schema
			Object liste = values.iterator().next();

			if (liste instanceof List) {
				List<Map<String, Object>> listeData = (List<Map<String, Object>>) liste;

				Map<String, Object> newEntry = new HashMap<String, Object>(1);
				newEntry.put(EwConstants.REF_URI, uri);
				
				// Criteria request
				Map<String, String> criteriaRequest = new HashMap<String, String>(3);
				//criteriaRequest.put("docTypes", CriteriaListBeanHelper.DocType.All.type());
				criteriaRequest.put("order", CriteriaListBeanHelper.Order.publicationDate.value());
				criteriaRequest.put("searchArea", CriteriaListBeanHelper.SearchArea.currentPage.value());
				criteriaRequest.put("currentDocId", document.getId());
				criteriaRequest.put("currentSpaceId", getSpaceId(document));
				newEntry.put("requestCriteria", criteriaRequest);
				
				// Criteria display
				Map<String, String> criteriaDisplay = new HashMap<String, String>(2);
				criteriaDisplay.put("style", CriteriaListBeanHelper.DisplayStyle.normal.value());
				/* FIXME: use schema default values? */
				criteriaDisplay.put("nbItems", String.valueOf(CriteriaListBeanHelper.NB_REQUEST_RESULTS));
				criteriaDisplay.put("nbItemsPerPage", String.valueOf(CriteriaListBeanHelper.NB_REQUEST_RESULTS_PER_PAGE));
				newEntry.put("displayCriteria", criteriaDisplay);
				
				listeData.add(newEntry);

				document.setProperties(CRITERIA_LIST_SCHEMA, properties);
			}

		} catch (ClientException e) {
			throw new EwServiceException(e);
		}
		return uri;
	}
	
	public static String getSpaceId(DocumentModel document){
	    String spaceId = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_SPACEID);
	    if(document.hasFacet(ToutaticeNuxeoStudioConst.CST_DOC_FACET_TTC_PUBLISH_SPACE)){
	        spaceId = document.getId();
	    }
	    return spaceId;
	}

}
