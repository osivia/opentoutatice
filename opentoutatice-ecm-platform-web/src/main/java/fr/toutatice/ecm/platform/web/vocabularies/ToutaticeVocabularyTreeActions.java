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
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.web.vocabularies;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.webapp.directory.VocabularyTreeActions;
import org.nuxeo.ecm.webapp.directory.VocabularyTreeNode;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;


/**
 * Used to fix possibility to have circular dependencies
 * when modify an entry with parent
 * (widget select_l10n_vocabulary_tree).
 * 
 * @author David Chevrier.
 *
 */
@Scope(CONVERSATION)
@Name("vocabularyTreeActions")
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeVocabularyTreeActions extends VocabularyTreeActions {

    private static final long serialVersionUID = -3914999057494484582L;
    
    /**
     * @param nodeId tree node
     * @return true if tree node is not current directory entry 
     *         we want to modify.
     */
    public boolean nodeIsNotCurrentEntry(String vocabularyName, String nodeId){
        
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = context.getViewRoot();
        
        DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
        String vocabularySchema = directoryService.getDirectorySchema(vocabularyName);

        String currentEntryId = getCurrentEntryValue(viewRoot, vocabularyName, vocabularySchema);

        return !nodeId.equalsIgnoreCase(currentEntryId);
    }

//    /**
//     * To fix possibility to have circular dependencies
//     * when modify an entry with parent.
//     */
//    @Override
//    public List<VocabularyTreeNode> getRoots(String vocabularyName, boolean displayObsoleteEntries, char keySeparator, String orderingField) {
//
//        List<VocabularyTreeNode> treeNodes = super.getRoots(vocabularyName, displayObsoleteEntries, keySeparator, orderingField);
//
//        FacesContext context = FacesContext.getCurrentInstance();
//        UIViewRoot viewRoot = context.getViewRoot();
//
//        DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
//        String vocabularySchema = directoryService.getDirectorySchema(vocabularyName);
//
//        String currentEntryId = getCurrentEntryValue(viewRoot, vocabularyName, vocabularySchema);
//
//        if (StringUtils.isNotBlank(currentEntryId)) {
//
//            VocabularyTreeNode rootToDel = null;
//            Iterator<VocabularyTreeNode> iterator = treeNodes.iterator();
//
//            while (iterator.hasNext() && rootToDel == null) {
//                VocabularyTreeNode vocabularyTreeNode = iterator.next();
//                String rootId = vocabularyTreeNode.getId();
//                if (currentEntryId.equalsIgnoreCase(rootId)) {
//                    rootToDel = vocabularyTreeNode;
//                }
//            }
//
//            if (rootToDel != null) {
//                treeNodes.remove(rootToDel);
//                clearTreeNode(vocabularyName);
//            }
//
//        }
//
//        return treeNodes;
//    }
//
//    /**
//     * Clear the current tree nodes
//     * to re-calculate this one
//     * (but bad for performances...)
//     */
//    private void clearTreeNode(String vocabularyName) {
//
//        VocabularyTreeNode vocabularyTreeNode = super.treeModels.get(vocabularyName);
//        if (vocabularyTreeNode != null) {
//            super.treeModels.remove(vocabularyName);
//        }
//
//    }

    /**
     * Find current entry value we want to modify.
     * 
     * @param component
     */
    private String getCurrentEntryValue(UIComponent component, String vocabulary, String vocabularySchema) {
        String currentEntryValue = StringUtils.EMPTY;

        StringBuffer editEntry = new StringBuffer().append(":editDirectoryEntry:nxl_").append(vocabulary).append("_vocabulary:nxw_").append(vocabularySchema)
                .append("_id");
        HtmlOutputText currentEditEntry = (HtmlOutputText) component.findComponent(editEntry.toString());

        if (currentEditEntry != null) {
            currentEntryValue = (String) currentEditEntry.getValue();
        }

        return currentEntryValue;
    }


}
