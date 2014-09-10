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
