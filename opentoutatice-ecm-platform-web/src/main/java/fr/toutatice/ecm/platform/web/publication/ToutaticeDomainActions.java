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
 *    
 */
package fr.toutatice.ecm.platform.web.publication;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * Manage some actions about the domain
 * 
 * @author loic
 * 
 */
@Name("domainActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class ToutaticeDomainActions implements Serializable {

	private static final String TTCD_DEFAULT_PORTAL_SITE_ID = "ttcd:defaultPortalSiteId";

	private static final String PORTAL_SITE = "PortalSite";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5069257071003926545L;

	@In(create = true, required = true)
	protected transient CoreSession documentManager;

	@In(create = true)
	protected transient NavigationContext navigationContext;

	/**
	 * Set the default portal site associated with a domain
	 * 
	 * @throws ClientException
	 */
	public void makeDefaultPortalSite() throws ClientException {

		DocumentModel currentDocument = navigationContext.getCurrentDocument();

		if (currentDocument.getType().equals(PORTAL_SITE) && !isDefaultPortal(currentDocument)) {
			DocumentModel domain = ToutaticeDocumentHelper.getDomain(documentManager, currentDocument, true);

			domain.setPropertyValue(TTCD_DEFAULT_PORTAL_SITE_ID, currentDocument.getId());

			documentManager.saveDocument(domain);
		}
	}

	/**
	 * Let a button to be displayed if this portal can be made as default
	 * 
	 * @return
	 */
	public boolean canMakeDefaultPSite() {

		DocumentModel currentDocument = navigationContext.getCurrentDocument();

		if (currentDocument.getType().equals(PORTAL_SITE) && !isDefaultPortal(currentDocument)) {
			return true;
		}
		return false;
	}

	/**
	 * Let a label displayed if this portal is the default portal
	 * 
	 * @return
	 */
	public boolean showIsDefaultPSite() {
		DocumentModel currentDocument = navigationContext.getCurrentDocument();

		if (currentDocument.getType().equals(PORTAL_SITE) && isDefaultPortal(currentDocument)) {
			return true;
		}
		return false;
	}

	private boolean isDefaultPortal(DocumentModel currentDocument) {
		
		try {
			DocumentModel domain = ToutaticeDocumentHelper.getDomain(documentManager, currentDocument, true);
			
			Serializable property = domain.getPropertyValue(TTCD_DEFAULT_PORTAL_SITE_ID);
			if (property != null && property.toString().equals(currentDocument.getId())) {
				return true;
			}
			
		} catch (ClientException e) {
			
		}
		
		return false;
	}
}
