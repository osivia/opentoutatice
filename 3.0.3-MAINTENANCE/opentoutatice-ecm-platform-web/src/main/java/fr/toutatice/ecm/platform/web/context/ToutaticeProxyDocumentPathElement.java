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

import javax.faces.context.FacesContext;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.pathelements.PathElement;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;

public class ToutaticeProxyDocumentPathElement implements PathElement {

	/**
	 * Olivier Adam, Rectorat de Rennes, le 26/10/11
	 */
	private static final long serialVersionUID = -3421858171637763580L;
	private final DocumentModel docModel;
    public static final String TYPE = "ProxyPathElement";
    
	public ToutaticeProxyDocumentPathElement(DocumentModel docModel) {
        this.docModel = docModel;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
        FacesContext context = FacesContext.getCurrentInstance();
        return ComponentUtils.translate(context, "label.toutatice.version.published");
	}

	@Override
	public boolean isLink() {
		return false;
	}
	
    public DocumentModel getDocumentModel() {
        return docModel;
    }

}
