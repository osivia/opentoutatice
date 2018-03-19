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
package fr.toutatice.ecm.platform.web.document;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.webapp.contentbrowser.OrderableDocumentActions;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

@Name("orderableDocumentActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeOrderableDocumentActions extends OrderableDocumentActions {

	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(ToutaticeOrderableDocumentActions.class);

	public static final String QUERY_DOC_ORDERED_CHILDREN_QM = "SELECT * FROM Document WHERE ecm:parentId = '%s' AND ecm:isCheckedInVersion = 0 AND ecm:mixinType != 'HiddenInNavigation' AND ecm:currentLifeCycleState != 'deleted' AND (ecm:name NOT LIKE '%%.proxy') ORDER BY ecm:pos";
	public static final int CST_MAX_RESULT_SET_SIZE = 10000;

    @In(create = true, required = false)
    protected transient ToutaticeDocumentActions documentActions;
    
    /* FIXME: test strength */
    @Override
    protected boolean isSectionType(DocumentModel doc) {
        boolean is = super.isSectionType(doc);
       return is && !documentActions.belongToPublishSpace();
    }


	@Override
	protected boolean getCanMoveDown(DocumentModel container, String documentsListName) throws NuxeoException {
		boolean status = false;

		List<DocumentModel> docs = documentsListsManager.getWorkingList(documentsListName);
		if (null != docs && docs.size() == 1) {
			status = true;
			
			DocumentModel selectedDocument = docs.get(0);
			List<DocumentModel> children = getChildrenFor(container.getId());
			int selectedDocumentIndex = children.indexOf(selectedDocument);
			int nextIndex = selectedDocumentIndex + 1;
			if (nextIndex == children.size()) {
				// can't move down the last document
				status = false;
			}

			if (status) {
				status = checkPermissions(container, selectedDocument, children.get(nextIndex));
			}
		}
		
		return status;
	}
	
	@Override
	protected boolean getCanMoveUp(DocumentModel container,	String documentsListName) throws NuxeoException {
		boolean status = false;
		
		List<DocumentModel> docs = documentsListsManager.getWorkingList(documentsListName);
		if (null != docs && docs.size() == 1) {
			status = true;

			DocumentModel selectedDocument = docs.get(0);
			List<DocumentModel> children = getChildrenFor(container.getId());
			int selectedDocumentIndex = children.indexOf(selectedDocument);
			int previousIndex = selectedDocumentIndex - 1;
			if (previousIndex < 0) {
				// can't move up the first document
				status = false;
			}
			
			if (status) {
				status = checkPermissions(container, selectedDocument, children.get(previousIndex));
			}
		}

		return status;
	}

	@Override
	protected boolean getCanMoveToTop(DocumentModel container, String documentsListName) throws NuxeoException {
		boolean status = false;

		List<DocumentModel> docs = documentsListsManager.getWorkingList(documentsListName);
		if (null != docs && docs.size() == 1) {
			status = true;

			DocumentModel selectedDocument = docs.get(0);
			List<DocumentModel> children = getChildrenFor(container.getId());
			int selectedDocumentIndex = children.indexOf(selectedDocument);
			if (selectedDocumentIndex <= 0) {
				// can't move to top the first document
				status = false;
			}
			
			if (status) {
				status = checkPermissions(container, selectedDocument, null);
			}
		}
		
        return status;
	}
	
	@Override
	protected boolean getCanMoveToBottom(DocumentModel container, String documentsListName) throws NuxeoException {
		boolean status = false;
		
		List<DocumentModel> docs = documentsListsManager.getWorkingList(documentsListName);
		if (null != docs && docs.size() == 1) {
			status = true;
			
			DocumentModel selectedDocument = docs.get(0);
			List<DocumentModel> children = getChildrenFor(container.getId());
			int selectedDocumentIndex = children.indexOf(selectedDocument);
			if (selectedDocumentIndex >= children.size() - 1) {
				// can't move to bottom the last document
				status = false;
			}
			
			if (status) {
				status = checkPermissions(container, selectedDocument, null);
			}
		}
		
		return status;
	}

	@Override
	protected String moveDown(DocumentModel container, String documentsListName) throws NuxeoException {
		DocumentModel selectedDocument = documentsListsManager.getWorkingList(documentsListName).get(0);

		List<DocumentModel> children = getChildrenFor(container.getId());
		int selectedDocumentIndex = children.indexOf(selectedDocument);
		int nextIndex = selectedDocumentIndex + 1;
		DocumentModel nextDocument = children.get(nextIndex);

		documentManager.orderBefore(container.getRef(), nextDocument.getName(), selectedDocument.getName());
		
        // placer le proxy à côté du document sélectionné
        moveDocumentProxy(container, selectedDocument);
        moveDocumentProxy(container, nextDocument);
		
		documentManager.save();

		notifyChildrenChanged(container);
		addFacesMessage("feedback.order.movedDown");
		return null;
	}

	@Override
	protected String moveUp(DocumentModel container, String documentsListName) throws NuxeoException {
        DocumentModel selectedDocument = documentsListsManager.getWorkingList(documentsListName).get(0);

        List<DocumentModel> children = getChildrenFor(container.getId());
        int selectedDocumentIndex = children.indexOf(selectedDocument);
        int previousIndex = selectedDocumentIndex - 1;
        DocumentModel previousDocument = children.get(previousIndex);

        documentManager.orderBefore(container.getRef(), selectedDocument.getName(), previousDocument.getName());

        // placer le proxy à côté du document sélectionné
        moveDocumentProxy(container, selectedDocument);
        moveDocumentProxy(container, previousDocument);
		
        documentManager.save();

        notifyChildrenChanged(container);
        addFacesMessage("feedback.order.movedUp");
        return null;
	}

	@Override
	protected String moveToTop(DocumentModel container, String documentsListName) throws NuxeoException {
        DocumentModel selectedDocument = documentsListsManager.getWorkingList(documentsListName).get(0);
        List<DocumentModel> children = getChildrenFor(container.getId());
        DocumentModel firstDocument = children.get(0);

        documentManager.orderBefore(container.getRef(), selectedDocument.getName(), firstDocument.getName());

        // placer le proxy à côté du document sélectionné
        moveDocumentProxy(container, selectedDocument);
        moveDocumentProxy(container, firstDocument);

        documentManager.save();

        notifyChildrenChanged(container);
        addFacesMessage("feedback.order.movedToTop");
        return null;
	}
	
	@Override
	protected String moveToBottom(DocumentModel container, String documentsListName) throws NuxeoException {
        DocumentRef containerRef = container.getRef();
        DocumentModel selectedDocument = documentsListsManager.getWorkingList(documentsListName).get(0);
        documentManager.orderBefore(containerRef, selectedDocument.getName(), null);
        
        // placer le proxy à côté du document sélectionné
        moveDocumentProxy(container, selectedDocument);

        documentManager.save();

        notifyChildrenChanged(container);
        addFacesMessage("feedback.order.movedToBottom");
        return null;
	}
	
	protected DocumentModelList getChildrenFor(String containerId) throws NuxeoException {
		try {
			// don't use the query model: is deprecated & has a result size limitation too small
			 return documentManager.query(String.format(QUERY_DOC_ORDERED_CHILDREN_QM, containerId), CST_MAX_RESULT_SET_SIZE);
		} catch (Exception e) {
			throw new NuxeoException(e);
		}
	}

	/**
	 * Si le document passé en paramètre possède un proxy, ce dernier est placé au dessus du document target.
	 */
	protected void moveDocumentProxy(DocumentModel container, DocumentModel document) throws NuxeoException {
		DocumentModel proxy = documentActions.getProxy(document);
		if (null != proxy) {
			documentManager.orderBefore(container.getRef(), proxy.getName(), document.getName());
		}
	}
	
	private boolean checkPermissions(DocumentModel container, DocumentModel source, DocumentModel destination) throws NuxeoException {
		boolean status = true;
		
		if ( !documentManager.hasPermission(container.getRef(), SecurityConstants.WRITE) 
		  || !documentManager.hasPermission(source.getRef(), SecurityConstants.WRITE)
		  || (null != destination && !documentManager.hasPermission(destination.getRef(), SecurityConstants.WRITE)) ) {
				status = false;
		}
		
		return status;
	}

}
