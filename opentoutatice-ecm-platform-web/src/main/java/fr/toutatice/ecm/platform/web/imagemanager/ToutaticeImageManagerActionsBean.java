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
package fr.toutatice.ecm.platform.web.imagemanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;
import org.nuxeo.ecm.webapp.filemanager.FileManageActionsBean;
import org.richfaces.model.UploadItem;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeFileHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeImageCollectionHelper;

@Name("ImageManagerActions")
@Scope(ScopeType.EVENT)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeImageManagerActionsBean extends FileManageActionsBean {
	
	private static final Log log = LogFactory.getLog(ToutaticeImageManagerActionsBean.class);

	@Override
    @SuppressWarnings("unchecked")
    public void validateMultipleUploadForDocument(DocumentModel current) throws ClientException, FileNotFoundException {
    	List<String>  msg_params = new ArrayList<String>();
    	
        if (!current.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
            return;
        }
        
        try {
        	List<Map<String, Object>> files = (List<Map<String, Object>>) current.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
            for (UploadItem uploadItem : getUploadedFiles()) {
                String filename = FileUtils.getCleanFileName(uploadItem.getFileName());
                Blob blob = FileUtils.createSerializableBlob(new FileInputStream(uploadItem.getFile()), filename, null);
                
                // vérifier que le fichier est de type image
                if (ToutaticeFileHelper.instance().isImageTypeFile(filename, blob)) {
                	HashMap<String, Object> fileMap = new HashMap<String, Object>(2);
                	fileMap.put("file", blob);
                	fileMap.put("filename", filename);
                	if (!files.contains(fileMap)) {
                		ToutaticeImageCollectionHelper.instance().add(files, fileMap);
                	}
                } else {
                	// ignorer le fichier
                	msg_params.add(filename);
                }
            }
            
            if (!msg_params.isEmpty()) {
				facesMessages.add(StatusMessage.Severity.WARN,
						messages.get("toutatice.fileImporter.error.mimetype.image"),
						formatParamsToString(msg_params));
            }
            
            current.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES, (Serializable) files);
            documentManager.saveDocument(current);
            documentManager.save();
        } finally {
            for (UploadItem uploadItem : getUploadedFiles()) {
                uploadItem.getFile().delete();
            }
        }
        Contexts.getConversationContext().remove("fileUploadHolder");
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void performAction(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext eContext = context.getExternalContext();
        String index = eContext.getRequestParameterMap().get("index");

        try {
            DocumentModel current = navigationContext.getCurrentDocument();
            if (!current.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
                return;
            }
            
            List<Map<String, Object>> files = (List<Map<String, Object>>) current.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
            Object file = CollectionUtils.get(files, new Integer(index));
            ToutaticeImageCollectionHelper.instance().remove(files, file);
            current.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES, (Serializable) files);
            documentManager.saveDocument(current);
            documentManager.save();
        } catch (Exception e) {
            log.error("Failed to remove the attached image, error: " +  e.getMessage());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void createImageStamp() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext eContext = context.getExternalContext();
        String index = eContext.getRequestParameterMap().get("index");
        
        try {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            if (!currentDocument.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
                return;
            }
            
            // la génération de la vignette se fera automatiquement via l'event listener "OnVignetteDocumentUpdate" (Nuxeo Studio)
			Collection files = (Collection) currentDocument.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
			Map<String, Object> file = (Map<String, Object>) CollectionUtils.get(files, new Integer(index));
            currentDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_STAMP, (Serializable) file.get("file"));
            
            // sauvegarder la modification (et déclencher le resizing de la vignette)
            documentManager.saveDocument(currentDocument);

            // notifier la fin de l'opération
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
            		messages.get("toutatice.fileImporter.create.success.stamp"));
			
            // some changes (versioning) happened server-side, fetch new one
			fetchCurrentDocument(currentDocument);
        } catch (Exception e) {
            log.error("Failed to generate the stamp image, error: " + e.getMessage());
        }
    }
    
    private String formatParamsToString(List<String>  params) {
    	String toString = null;
    	
    	for (String param : params) {
    		if (StringUtils.isBlank(toString)) {
    			toString = param;
    		} else {
    			toString = toString + ", " + param;
    		}
    	}

    	return toString;
    }

    public void fetchCurrentDocument(DocumentModel document) throws ClientException {
        navigationContext.invalidateCurrentDocument();
//		EventManager.raiseEventsOnDocumentChange(currentDocument);    	
    }

}
