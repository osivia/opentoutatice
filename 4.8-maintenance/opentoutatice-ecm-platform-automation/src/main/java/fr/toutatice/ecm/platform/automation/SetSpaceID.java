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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.versioning.VersioningService;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;

@Operation(id = SetSpaceID.ID, category = Constants.CAT_DOCUMENT, label = "Set spaceId.", description = "Update spaceID's value of a document and his children.")
public class SetSpaceID {

	public static final String ID = "Document.SetSpaceID";
	public final String WORKSPACE_TYPE = "Workspace";
	public final String USER_WORKSPACE_TYPE = "UserWorkspace";
	
	private static final List<Class<?>> FILTERED_SERVICES_LIST = new ArrayList<Class<?>>() {
        private static final long serialVersionUID = 1L;
        {
            add(EventService.class);
            add(VersioningService.class);
        }
    };

	@Context
	protected CoreSession coreSession;

	@OperationMethod()
	public DocumentModel run(DocumentModel doc) throws Exception {
		// mise à jour du document et de sa sous-arborescence éventuelle
		InnerSilentModeUpdateSpaceID runner = new InnerSilentModeUpdateSpaceID(coreSession, doc);
		runner.silentRun(true, FILTERED_SERVICES_LIST);
		
		return runner.getDoc();
	}

	private class InnerSilentModeUpdateSpaceID extends ToutaticeSilentProcessRunnerHelper {
		DocumentModel doc;

		public InnerSilentModeUpdateSpaceID(CoreSession session, DocumentModel doc) {
			super(session);
			this.doc = doc;
		}

		@Override
		public void run() throws NuxeoException {
			String spaceID = ToutaticeDocumentHelper.getSpaceID(this.session, this.doc, true);
			updateDoc(this.doc, spaceID);
		}

		public DocumentModel getDoc() throws NuxeoException {
		    return this.doc;
		}

		private void updateDoc(DocumentModel doc, String spaceID) throws  NuxeoException, PropertyException {
			if (!doc.isImmutable() && doc.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
				// mise à jour du spaceID sur le document courant en mode silencieux
				doc.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_SPACEID, spaceID);
				this.session.saveDocument(doc);
			}
			
			if (doc.isFolder()) {
				// récupération de ses enfants
				StringBuilder query = new StringBuilder();
				query.append("select * from Document where ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND " );
				query.append("ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0 AND ecm:parentId = '");
				query.append(doc.getId());
				query.append("'");

				DocumentModelList children = this.session.query(query.toString());	

				// s'il y a des enfants
				if (children != null && !children.isEmpty()) {
					String childSpaceID = ToutaticeDocumentHelper.getSpaceID(this.session, children.get(0), true);
					for (DocumentModel child : children) {
						updateDoc(child, childSpaceID);
					}
				}
			}
		}
	}

}
