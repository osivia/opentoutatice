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
 *   lbillon
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.core.listener;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * Cet event listener permet de réaliser les opérations non effectuées par défaut par le framework Nuxeo sur la suppression d'un document 
 * 
 * @author mberhaut1
 */
public class ToutaticeDeleteEventListener implements EventListener {
	
	private static String CST_QUERY_DOCUMENT_HAVING_A_SECTION_REFERENCE = "SELECT * FROM Document WHERE ecm:mixinType = 'Folderish' AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0 AND publish:sections IN ('%s')";

	@Override
	public void handleEvent(Event event) throws NuxeoException {
		if (event.getContext() instanceof DocumentEventContext) {
			EventContext ctx = event.getContext();
			DocumentEventContext  docCtx = (DocumentEventContext) event.getContext();
			DocumentModel document = docCtx.getSourceDocument();
			CoreSession session = ctx.getCoreSession();
			
			/*
			 * Retirer des documents les références à la section en cours de suppression
			 */
			if (ToutaticeNuxeoStudioConst.CST_DOC_TYPE_SECTION.equals(document.getType())) {
				// Rechercher les documents qui font référence à cette section
				DocumentModelList list = session.query(String.format(CST_QUERY_DOCUMENT_HAVING_A_SECTION_REFERENCE, document.getId()));
				
				// Mettre à jour ces documents
				for (DocumentModel docToUpdate : list) {
					if (session.hasPermission(docToUpdate.getRef(), SecurityConstants.WRITE)) {
						String[] currentSectionIdsList = (String[]) docToUpdate.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_NUXEO_SECTIONS_PROPERTY_NAME);

						List<String> newSectionIdsList = new ArrayList<String>();
						if (currentSectionIdsList != null) {
							for (String sectionId : currentSectionIdsList) {
								if (!sectionId.equals(document.getId())) {
									newSectionIdsList.add(sectionId);
								}
							}
						}

						docToUpdate.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_NUXEO_SECTIONS_PROPERTY_NAME, newSectionIdsList.toArray(new String[newSectionIdsList.size()]));
						session.saveDocument(docToUpdate);
					}
				}
			} else {
				/*
				 * Retirer les proxies associés au document en cours de suppression 
				 */
				
				// filtre les documents de type proxy
				if (!document.isProxy() && !document.isVersion()) {
					// vérifie que le document en cours de suppression possède un proxy
					
					DocumentModelList proxies = session.getProxies(document.getRef(), document.getParentRef());    	
					if ((null != proxies) && (!proxies.isEmpty())) {
						// remove all document proxies
						for (DocumentModel proxy : proxies) {
							session.removeDocument(proxy.getRef());
						}
					}
				}
			}
		}
	}
	
}
