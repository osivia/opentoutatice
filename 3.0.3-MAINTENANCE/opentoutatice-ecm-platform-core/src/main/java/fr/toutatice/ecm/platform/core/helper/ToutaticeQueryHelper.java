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
 *   lbillon
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.core.helper;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;


/**
 * @author david chevrier
 *
 */
public class ToutaticeQueryHelper {
    
    /*
     * Helper class.
     */
    private ToutaticeQueryHelper(){};
    
    /**
     * To query in unrestricted mode.
     * 
     * @author david
     *
     */
    public static class UnrestrictedQueryRunner extends UnrestrictedSessionRunner {

        String query;

        DocumentModelList docs;

        protected UnrestrictedQueryRunner(CoreSession session, String query) {
            super(session);
            this.query = query;
        }

        @Override
        public void run() throws ClientException {
            docs = session.query(query);
            for (DocumentModel documentModel : docs) {
                documentModel.detach(true);
            }
        }

        public DocumentModelList runQuery() throws ClientException {
            runUnrestricted();
            return docs;
        }
    }

}
