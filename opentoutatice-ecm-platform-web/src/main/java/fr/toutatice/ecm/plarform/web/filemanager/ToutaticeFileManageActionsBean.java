/**
 * 
 */
package fr.toutatice.ecm.plarform.web.filemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;
import org.nuxeo.ecm.webapp.filemanager.FileManageActionsBean;
import org.nuxeo.ecm.webapp.filemanager.NxUploadedFile;
import org.nuxeo.runtime.api.Framework;
import org.richfaces.event.FileUploadEvent;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;


/**
 * @author david
 *
 */
@Name("toutaticeFileManageActions")
@Scope(ScopeType.EVENT)
@Install(precedence = Install.FRAMEWORK)
public class ToutaticeFileManageActionsBean extends FileManageActionsBean {
    
    private static final Log log = LogFactory.getLog(FileManageActionsBean.class);
    
    /**
     * To do not save (case of creation in peculiar).
     */
    @Override
    @SuppressWarnings({ "rawtypes" })
    public void performAction(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext eContext = context.getExternalContext();
        String index = eContext.getRequestParameterMap().get("index");

        try {
            DocumentModel current = ((ToutaticeDocumentActionsBean) documentActions).getCurrentDocument();
            if (!current.hasSchema(FILES_SCHEMA)) {
                return;
            }
            ArrayList files = (ArrayList) current.getPropertyValue(FILES_PROPERTY);
            Object file = CollectionUtils.get(files, Integer.valueOf(index).intValue());
            files.remove(file);
            current.setPropertyValue(FILES_PROPERTY, files);
            if (!ToutaticeDocumentHelper.isEmptyDocumentModel(current)) {
                documentActions.updateDocument(current, Boolean.TRUE);
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }
    
    /**
     * To do not save (case of creation in peculiar).
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void validateMultipleUploadForDocument(DocumentModel current) throws NuxeoException, FileNotFoundException, IOException {
        if (!current.hasSchema(FILES_SCHEMA)) {
            return;
        }
        Collection<NxUploadedFile> nxuploadFiles = getUploadedFiles();
        try {
            ArrayList files = (ArrayList) current.getPropertyValue(FILES_PROPERTY);
            if (nxuploadFiles != null) {
                for (NxUploadedFile uploadItem : nxuploadFiles) {
                    String filename = FileUtils.getCleanFileName(uploadItem.getName());
                    
                    Blob blob = FileUtils.createSerializableBlob(uploadItem.getBlob().getStream(), filename, uploadItem.getContentType());
                    
                    HashMap<String, Object> fileMap = new HashMap<String, Object>(2);
                    fileMap.put("file", blob);
                    fileMap.put("filename", filename);
                    if (!files.contains(fileMap)) {
                        files.add(fileMap);
                    }
                }
            }
            current.setPropertyValue(FILES_PROPERTY, files);
            if(!ToutaticeDocumentHelper.isEmptyDocumentModel(current)){
                documentActions.updateDocument(current, Boolean.TRUE);
            }
        } finally {
            if (nxuploadFiles != null) {
                for (NxUploadedFile uploadItem : nxuploadFiles) {
                    File tempFile = uploadItem.getFile();
                    if (tempFile != null && tempFile.exists()) {
                        Framework.trackFile(tempFile, tempFile);
                    }
                }
            }
        }
    }

}
