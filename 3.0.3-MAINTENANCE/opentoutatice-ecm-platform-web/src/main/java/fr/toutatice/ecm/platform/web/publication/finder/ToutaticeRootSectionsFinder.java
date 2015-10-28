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
package fr.toutatice.ecm.platform.web.publication.finder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.publisher.impl.finder.DefaultRootSectionsFinder;

public class ToutaticeRootSectionsFinder extends DefaultRootSectionsFinder {

    private static final Log log = LogFactory.getLog(ToutaticeRootSectionsFinder.class);

    private static String CST_QUERY_LIST_PUBLISH_SPACES = "SELECT * FROM %s WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0";

    public ToutaticeRootSectionsFinder(CoreSession userSession) {
        super(userSession);
    }

    /* To refresh sections roots list */
    public void refreshRootSections() {
        if (unrestrictedDefaultSectionRoot != null) {
            unrestrictedDefaultSectionRoot.clear();
        }
    }

    @Override
    protected DocumentModelList getDefaultSectionRoots(CoreSession session) throws ClientException {
        DocumentModelList sectionRoots = new DocumentModelListImpl();

        for (String sectionRootType : getSectionRootTypes()) {
            DocumentModelList list = session.query(String.format(CST_QUERY_LIST_PUBLISH_SPACES, sectionRootType));

            /*
             * filtrer les 'section roots' dont le parent est également un 'section root' afin que ceux-ci ne soient pas présentés dans
             * l'IHM de configuration des sections de publication d'un espace de travail.
             * Seul le section root parent sera présenté. Le section root fils sera visible néanmoins via la présentation des sections
             * sous forme d'arbre par le widget.
             */
            UnrestrictedSessionRunner filter = new UnrestrictedFilterSectionRootsRunner(session, list, sectionRoots);
            filter.runUnrestricted();
        }

        return sectionRoots;
    }

    private static class UnrestrictedFilterSectionRootsRunner extends UnrestrictedSessionRunner {

        DocumentModelList list;
        DocumentModelList sectionRoots;

        protected UnrestrictedFilterSectionRootsRunner(CoreSession session, DocumentModelList list, DocumentModelList sectionRoots) {
            super(session);
            this.list = list;
            this.sectionRoots = sectionRoots;
        }

        @Override
        public void run() throws ClientException {
            for (DocumentModel sectionRoot : list) {
                try {
                    DocumentModel sectionRootParent = this.session.getParentDocument(sectionRoot.getRef());
                    if (!sectionRootParent.hasFacet(FacetNames.MASTER_PUBLISH_SPACE)) {
                        this.sectionRoots.add(sectionRoot);
                    }
                } catch (Exception e) {
                    log.warn("Failed to filter the section roots, error: " + e.getMessage());
                }
            }
        }
    }

}
