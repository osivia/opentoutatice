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
package fr.toutatice.ecm.platform.web.publication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.Sorter;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.publisher.web.AdministrationPublishActions;
import org.nuxeo.ecm.webapp.tree.DocumentTreeNode;
import org.nuxeo.ecm.webapp.tree.DocumentTreeNodeImpl;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSorterHelper;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;
import fr.toutatice.ecm.platform.web.publication.finder.ToutaticeRootSectionsFinder;

@Name("adminPublishActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeAdministrationPublishActions extends AdministrationPublishActions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ToutaticeAdministrationPublishActions.class);

    /*
     * [Mantis #2805]
     * Mis en place afin que notre propre implémentation de classe RootFinder soit utilisée
     * à la place de celle de Nuxeo.
     * Objectif:
     * Pouvoir publier dans une section présente dans un document de type 'PortalSite' (équivalent
     * d'une SectionRoot).
     * 
     * @see org.nuxeo.ecm.platform.publisher.web.AdministrationPublishActions#getSectionRoots()
     */
    @Factory(value = "defaultPublishingRoots", scope = ScopeType.EVENT)
    public DocumentModelList getSectionRoots() throws ClientException {
        ToutaticeRootSectionsFinder rootFinder = (ToutaticeRootSectionsFinder) getRootFinder();
        rootFinder.refreshRootSections();
        DocumentModelList sectionRoots = rootFinder.getDefaultSectionRoots(false, true);
        Collections.sort(sectionRoots, new SectionRootsComparator());
        return sectionRoots;
    }
    
    /*
     * Fixes loops on parent.
     */
    @Override
    public String getDomainNameFor(final DocumentModel sectionRoot) throws ClientException {
        final List<String> domainName = new ArrayList<>();
        new UnrestrictedSessionRunner(documentManager) {
            @Override
            public void run() throws ClientException {
                DocumentModel parent = session.getParentDocument(sectionRoot.getRef());
                SchemaManager schemaManager = Framework.getLocalService(SchemaManager.class);
                while (parent != null && !"/".equals(parent.getPathAsString())) {
                    if (schemaManager.hasSuperType(parent.getType(), "Domain")) {
                        domainName.add(parent.getTitle());
                        return;
                    } else {
                        parent = session.getParentDocument(parent.getRef());  
                    }
                }
            }
        }.runUnrestricted();
        return domainName.isEmpty() ? null : domainName.get(0);
    }

    @Override
    protected void getPathFragments(DocumentModel document, List<String> pathFragments) throws ClientException {
        // ajouter le nom du document courant
        pathFragments.add(document.getTitle());

        // récupération du SectionRoot
        DocumentModel sectionRoot = ((ToutaticeNavigationContext) navigationContext).getSectionPublicationArea(document);
        if (!sectionRoot.equals(document)) {
            pathFragments.add(sectionRoot.getTitle());
        }

        // récupérer le nom du domaine associé au document
        DocumentModel domain = ((ToutaticeNavigationContext) navigationContext).getDocumentDomain(document);
        pathFragments.add(domain.getTitle());
    }

    private class SectionRootsComparator extends ToutaticeSorterHelper<DocumentModel> {

        private static final long serialVersionUID = 1L;

        @Override
        public String getComparisionString(DocumentModel document) {
            String stg = "";

            try {
                stg = getFormattedPath(document);
            } catch (Exception e) {
                log.error("Failed to extract the comparision string to sections, error:" + e.getMessage());
            }

            return stg;
        }

    }

    /*
     * [Mantis #2805]
     * Objectif:
     * Mettre en place un page provider pour filtrer les documents à présenter
     * dans l'arbre de publication qui apparaît pour la configuration des
     * sections de publication autorisées pour un espace de travail.
     */
    @Override
    protected DocumentTreeNode getDocumentTreeNode(DocumentModel document) {
        DocumentTreeNode dtn = null;

        if (document != null) {
            Filter filter = null;
            Sorter sorter = null;
            String pageProviderName = null;
            try {
                pageProviderName = getTreeManager().getPageProviderName(PUBLICATION_TREE_PLUGIN_NAME);
                sorter = getTreeManager().getSorter(PUBLICATION_TREE_PLUGIN_NAME);
            } catch (Exception e) {
                log.error("Could not fetch filter, sorter or node type for tree ", e);
            }

            dtn = new DocumentTreeNodeImpl(document.getSessionId(), document, filter, null, sorter, pageProviderName);
        }

        return dtn;
    }

    public String getRootFormattedPath(DocumentModel document) throws ClientException {
        List<String> pathFragments = new ArrayList<String>();
        pathFragments.add(document.getTitle());
        DocumentModel sectionRoot = ((ToutaticeNavigationContext) navigationContext).getSectionPublicationArea(document);
        if (!sectionRoot.equals(document)) {
            pathFragments.add(sectionRoot.getTitle());
        }
        DocumentModel domain = ((ToutaticeNavigationContext) navigationContext).getDocumentDomain(document);
        pathFragments.add(domain.getTitle());
        return super.formatPathFragments(pathFragments);
    }

}
