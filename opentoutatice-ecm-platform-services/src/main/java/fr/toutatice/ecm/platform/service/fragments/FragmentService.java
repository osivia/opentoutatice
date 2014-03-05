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
package fr.toutatice.ecm.platform.service.fragments;

import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.fragments.types.Fragment;


public interface FragmentService {

    public Map.Entry<FragmentDescriptor, Fragment> findByCode(String code) throws FragmentServiceException;

    public Entry<FragmentDescriptor, Fragment> getFragmentCategory(DocumentModel doc, String uri) throws FragmentServiceException;

    public String prepareCreation(DocumentModel doc, Fragment specific, String fragmentCategory, String region, String belowUri, String code2)
            throws FragmentServiceException;

}
