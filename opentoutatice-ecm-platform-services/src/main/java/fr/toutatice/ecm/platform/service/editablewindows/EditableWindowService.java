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
package fr.toutatice.ecm.platform.service.editablewindows;

import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.editablewindows.types.EditableWindow;

/**
 * Service for editable window registration
 * @author Loïc Billon
 *
 */
public interface EditableWindowService {

	/**
	 * Get an EW by code
	 * @param code
	 * @return the EW instance
	 * @throws EwServiceException
	 */
    public Map.Entry<EwDescriptor, EditableWindow> findByCode(String code) throws EwServiceException;

    /**
     * Get descriptor and instance of an EW
     * @param doc
     * @param uri
     * @return Entry.set
     * @throws EwServiceException
     */
    public Entry<EwDescriptor, EditableWindow> getEwEntry(DocumentModel doc, String uri) throws EwServiceException;

    /**
     * Used to instanciate a new editable window in a page.
     * @param doc
     * @param specific
     * @param fragmentCategory
     * @param region
     * @param belowUri
     * @param code2
     * @return
     * @throws EwServiceException
     */
    public String prepareCreation(DocumentModel doc, EditableWindow specific, String fragmentCategory, String region, String belowUri, String code2)
            throws EwServiceException;

}
