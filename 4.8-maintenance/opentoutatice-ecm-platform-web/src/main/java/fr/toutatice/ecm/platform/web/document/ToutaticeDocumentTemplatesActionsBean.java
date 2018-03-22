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

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.webapp.documenttemplates.DocumentTemplatesActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

@Name("documentTemplatesActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeDocumentTemplatesActionsBean extends DocumentTemplatesActionsBean {

	private static final long serialVersionUID = 5605205971806068358L;
	
	public String createDocumentFromTemplate(String viewId) throws NuxeoException {
		if (null == this.changeableDocument) {
			this.changeableDocument = this.navigationContext.getChangeableDocument();
		}
		
		super.createDocumentFromTemplate();
		return viewId;
	}
	
	/**
	 * To avoid local proxies from templates list.
	 */
	@Override
    public DocumentModelList getTemplates(String targetTypeName)
            throws NuxeoException {
        DocumentModelList templates = super.getTemplates(targetTypeName);
        DocumentModel[] templatesArray = templates.toArray(new DocumentModel[0]);
        for(DocumentModel template : templatesArray){
            if(template.isProxy()){
                templates.remove(template);
            }
        }
        return templates;
    }

}
