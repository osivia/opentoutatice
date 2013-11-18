package fr.toutatice.ecm.platform.web.context;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

public interface ToutaticeNavigationContext extends NavigationContext {
    public DocumentModel getCurrentPublicationArea();
    
    public DocumentModel getPublicationArea(DocumentModel document);

    public DocumentModel getCurrentWorkspaceArea();
    
	public DocumentModel getDocumentDomain(DocumentModel document);
	
	/**
	 * Retourne l'espace parent qui correspond à un espace de publication (publication 'externe'. SectionRoot, PortalSite, ...).
	 *
	 * @param section la section pour laquelle chercher le parent de type espace de publication
	 * @return L'espace de publication parent ou null
	 */
	public DocumentModel getSectionPublicationArea(DocumentModel section);

	public String getCurrentLifeCycleState() throws ClientException;
	
	public void resetNavigation() throws ClientException;
	
	public DocumentModel getSpaceDoc(DocumentModel document);
}
