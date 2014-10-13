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
 *   dchevrier
 *   lbillon
 *    
 */
package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author David Chevrier
 *
 */
@Operation(id = FetchDocumentInList.ID, category = Constants.CAT_FETCH, label = "FetchDocumentInList", description = "Fetch the document at given index in given list.")
public class FetchDocumentInList {

    public final static String ID = "Fetch.DocumentInList";
    
    @Param(name = "index", required = true)
    protected Long index;
    
    @OperationMethod
    public DocumentModel run(DocumentModelList list) throws Exception {
        return list.get(index.intValue());
    }
    
}
