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
package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;

@Operation(id = SetSpaceID.ID, category = Constants.CAT_DOCUMENT, label = "Set spaceId.", description = "Update spaceID's value of document and his children.")
public class SetSpaceID {

	public static final String ID = "Document.SetSpaceID";
	public final String WORKSPACE_TYPE = "Workspace";
	public final String USER_WORKSPACE_TYPE = "UserWorkspace";

	@Context
	protected CoreSession coreSession;

	@OperationMethod()
	public DocumentModel run(DocumentModel doc) throws Exception {
		if (!doc.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
			return doc;
		}
		
		String spaceId = getSpaceID(doc);
		
		// mise à jour du document
		InnerSilentModeUpdateSpaceID runner = new InnerSilentModeUpdateSpaceID(coreSession, doc, spaceId);
		runner.silentRun(true);
		doc = runner.getDoc();
		
		return doc;
	}

	private String getSpaceID(DocumentModel doc) throws ClientException,
			PropertyException {
		String spaceId;
		
		// si UserWorspace => spaceId = dc:title (conversion en minuscule afin de pouvoir utiliser l'indexation sur cette méta-donnée)
		if (ToutaticeNuxeoStudioConst.CST_DOC_TYPE_USER_WORKSPACE.equals(doc.getType())) {
			spaceId = doc.getTitle().toLowerCase();
		} else {

			// sinon récupérer la liste des spaceParents

			DocumentModelList spaceParentList = ToutaticeDocumentHelper.getParentSpaceList(coreSession, doc, true, true);

			if (spaceParentList == null || spaceParentList.isEmpty()) {
				// le document courant n'appartient pas à un space
				spaceId = "";
			} else {
				// prendre le 1er parent de type space rencontré
				DocumentModel space = spaceParentList.get(0);

				if (ToutaticeNuxeoStudioConst.CST_DOC_TYPE_USER_WORKSPACE.equals(space.getType())) {
					// si le type de ce space est UserWorkspace => spaceID = dc:title
					spaceId = space.getTitle().toLowerCase();
				} else {
					// sinon spaceID = space.getId
					spaceId = space.getId();
				}

			}
		}
		return spaceId;
	}
	
	private class InnerSilentModeUpdateSpaceID extends ToutaticeSilentProcessRunnerHelper {

		DocumentModel doc;
		String spaceID;

		public InnerSilentModeUpdateSpaceID(CoreSession session, DocumentModel doc, String spaceID) {
			super(session);
			this.spaceID = spaceID;
			this.doc = doc;
		}

		@Override
		public void run() throws ClientException {
			updateDoc(doc, spaceID);
		}

		public DocumentModel getDoc() {
			return this.doc;
		}
		
		private void updateDoc(DocumentModel doc, String spaceId) throws  ClientException, PropertyException {
			// si ce n'est pas un space, mise à jour du spaceID sur le document courant en mode silencieux
			doc.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_SPACEID, spaceId);
			this.session.saveDocument(doc);
				
			if (!doc.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_SPACE) && doc.isFolder()) {
				// récupération de ces enfants
				StringBuilder query = new StringBuilder();
				query.append("select * from Document where ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND " );
				query.append("ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0 AND ecm:parentId = '");
				query.append(doc.getId());
				query.append("'");
			
				DocumentModelList children = this.session.query(query.toString());	
				
				// s'il y a des enfants
				if (children != null && !children.isEmpty()) {
					for (DocumentModel child : children) {
						updateDoc(child, spaceId);
					}
				}
			}
		}
	}

}
