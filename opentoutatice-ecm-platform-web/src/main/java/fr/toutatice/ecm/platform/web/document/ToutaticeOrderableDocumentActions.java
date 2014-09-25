package fr.toutatice.ecm.platform.web.document;

import java.text.Collator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.Sorter;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.webapp.contentbrowser.OrderableDocumentActions;

import edu.emory.mathcs.backport.java.util.Collections;

@Name("orderableDocumentActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class ToutaticeOrderableDocumentActions extends OrderableDocumentActions {

	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(ToutaticeOrderableDocumentActions.class);

	//public static final String QUERY_DOC_ORDERED_CHILDREN_QM = "SELECT * FROM Document WHERE ecm:parentId = '%s' AND ecm:isCheckedInVersion = 0 AND ecm:mixinType != 'HiddenInNavigation' AND ecm:currentLifeCycleState != 'deleted' AND (ecm:isProxy = 0 OR (ecm:isProxy = 1 AND ecm:name LIKE '%%.remote.proxy%%')) ORDER BY ecm:pos";
	/*
	 * DCH: modif requête suite à non renommage des proxies distants
	 */
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
	protected boolean getCanMoveDown(DocumentModel container, String documentsListName) throws ClientException {
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
	protected boolean getCanMoveUp(DocumentModel container,	String documentsListName) throws ClientException {
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
	protected boolean getCanMoveToTop(DocumentModel container, String documentsListName) throws ClientException {
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
	protected boolean getCanMoveToBottom(DocumentModel container, String documentsListName) throws ClientException {
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
	protected String moveDown(DocumentModel container, String documentsListName) throws ClientException {
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
	protected String moveUp(DocumentModel container, String documentsListName) throws ClientException {
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
	protected String moveToTop(DocumentModel container, String documentsListName) throws ClientException {
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
	protected String moveToBottom(DocumentModel container, String documentsListName) throws ClientException {
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
	
	protected DocumentModelList getChildrenFor(String containerId) throws ClientException {
		try {
			// don't use the query model: is deprecated & has a result size limitation too small
			 return documentManager.query(String.format(QUERY_DOC_ORDERED_CHILDREN_QM, containerId), CST_MAX_RESULT_SET_SIZE);
		} catch (Exception e) {
			throw new ClientException(e);
		}
	}
	
	/**
	 * Si le document passé en paramètre possède un proxy, ce dernier est placé au dessus du document target.
	 */
	private void moveDocumentProxy(DocumentModel container, DocumentModel document) throws ClientException {
		DocumentModel proxy = documentActions.getProxy(document);
		if (null != proxy) {
			documentManager.orderBefore(container.getRef(), proxy.getName(), document.getName());
		}
	}

    public void orderAlphabetically() {
    	DocumentModel container = navigationContext.getCurrentDocument();
        if (container == null) {
        	return;
        }
        
    	try {
    		// get the current container's children (proxies filtered, sorted)
    		List<DocumentModel> childrenSorted = getChildrenFor(container.getId());
    		Collections.sort(childrenSorted, new AlphabeticallSorter());
    		
    		// re-order the documents from the sorted list
    		if (1 < childrenSorted.size()) {
    			// move at end the latest document
    			DocumentModel latestDoc = childrenSorted.get(childrenSorted.size() - 1);
    			documentManager.orderBefore(container.getRef(), latestDoc.getName(), null);

    			// iterate reverse on all other children
    			for (int index = childrenSorted.size() - 2; index >= 0 ; index--) {
    				DocumentModel child = childrenSorted.get(index);
    				DocumentModel follower = childrenSorted.get(index + 1);
    				documentManager.orderBefore(container.getRef(), child.getName(), follower.getName());
    			}
    			
    			// order the documents proxies
    			if (documentActions.belongToPublishSpace()) {
    				for (DocumentModel child : childrenSorted) {
    					moveDocumentProxy(container, child);
    				}
    			}
    			
    			// save the new order
    			documentManager.save();
    			
    			// refresh the GUI
    			notifyChildrenChanged(container);
    		}
    		
            addFacesMessage("label.acaren.action.ordering.done.successfully");
    	} catch (Exception e) {
    		log.debug("Failed to order as alphabetical order the folder '" + container.getName() + "', error: " + e.getMessage());
    	}
    }
    
    @SuppressWarnings("serial")
	private class AlphabeticallSorter implements Sorter {
    	
    	Pattern pattern;
    	Pattern patternExtractTail;
    	Collator collator;
    	
    	public AlphabeticallSorter() {
			collator = Collator.getInstance();
			collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
			collator.setStrength(Collator.TERTIARY);   
			
			this.pattern = Pattern.compile("^\\s*\\d+.*");
			this.patternExtractTail = Pattern.compile("^\\s*\\d+");
    	}

		@Override
		public int compare(DocumentModel d1, DocumentModel d2) {
			try {
				int oi1;
				int oi2;
				
				String o1 = d1.getTitle();
				String o2 = d2.getTitle();
				
				oi1 = extractIndexPrefix(o1);
				oi2 = extractIndexPrefix(o2);
				
				if (oi1 != Integer.MAX_VALUE && oi2 != Integer.MAX_VALUE) {
					return (oi1 < oi2) ? -1 : 1;
				}
				
				return collator.compare(o1, o2);
			} catch (ClientException e) {
				log.error("AlphabeticallSorter: failed to get the title of the document id '" + d1.getId() + ", '" + d2.getId() + "'");
				return 0;
			}
		}
		
		private int extractIndexPrefix(String stg) {
			int index = Integer.MAX_VALUE;
			
			if (this.pattern.matcher(stg).matches()) {
				String tail = stg.replaceFirst(this.patternExtractTail.pattern(), "");
				tail = Pattern.quote(tail);
				String indexStg = stg.replaceFirst(tail, "").trim();
				index = Integer.valueOf(indexStg);
			}
			
			return index;
		}
    	
    }
    
	private boolean checkPermissions(DocumentModel container, DocumentModel source, DocumentModel destination) throws ClientException {
		boolean status = true;
		
		if ( !documentManager.hasPermission(container.getRef(), SecurityConstants.WRITE) 
		  || !documentManager.hasPermission(source.getRef(), SecurityConstants.WRITE)
		  || (null != destination && !documentManager.hasPermission(destination.getRef(), SecurityConstants.WRITE)) ) {
				status = false;
		}
		
		return status;
	}

}
