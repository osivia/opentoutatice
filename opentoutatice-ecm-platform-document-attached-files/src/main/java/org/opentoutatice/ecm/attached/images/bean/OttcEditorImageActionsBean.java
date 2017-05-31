/**
 * 
 */
package org.opentoutatice.ecm.attached.images.bean;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeFileHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeImageCollectionHelper;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;
import fr.toutatice.ecm.platform.web.webeditor.ToutaticeEditorImageActionsBean;


/**
 * @author david
 *
 */
@Name("editorImageActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE + 100)
public class OttcEditorImageActionsBean extends ToutaticeEditorImageActionsBean {

    private static final long serialVersionUID = -5167514978372946152L;
    private static final Log log = LogFactory.getLog(OttcEditorImageActionsBean.class);
    
    @In(create = true, required = false)
    private transient CoreSession documentManager;
    
    private boolean isImage = true;

    /**
     * 
     */
    public OttcEditorImageActionsBean() {
        super();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String uploadImage() throws ClientException {
        Part uploadedImg = getUploadedImage();
        if(uploadedImg == null){
            return null;
        }
        
        String uploadedImgName = FileUtils.retrieveFilename(uploadedImg);
        if (null == uploadedImg || StringUtils.isBlank(uploadedImgName)) {
            return null;
        }

        try {
            ToutaticeDocumentActionsBean actionsbean = (ToutaticeDocumentActionsBean) SeamComponentCallHelper.getSeamComponentByName("documentActions");
            DocumentModel doc = actionsbean.getCurrentDocument();
            
            Blob uploadedImageBlob = FileUtils.createSerializableBlob(uploadedImg.getInputStream(), uploadedImgName, uploadedImg.getContentType());
            boolean isImage = ToutaticeFileHelper.instance().isImageTypeFile(uploadedImgName, uploadedImageBlob);
            
            if (isImage) {
                List<Map<String, Object>> files = (List<Map<String, Object>>) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
                
                final Map<String, Object> item = new HashMap<String, Object>();
                item.put("filename", uploadedImgName);
                item.put("file", uploadedImageBlob);
                
                ToutaticeImageCollectionHelper.instance().add(files, item);

                doc.setPropertyValue(
                        ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES,
                        (Serializable) files);
                
                // Not creation mode
                if(StringUtils.isNotBlank(doc.getId())){
                    documentManager.saveDocument(doc);
                    //documentManager.save();
                }
            } else {
                // Le document que l'on cherche à uploader n'a pas le bon type mime. Abandon + message d'erreur (dans page JSF)
                this.isImage = false;
                log.debug("The binary file to upload hasn't the correct mimetype.");
            }
            
            return "editor_image_upload";
        } catch (IOException e) {
            log.error("Failed to upload the image, error: " + e.getMessage());
            throw new ClientException(e);
        }
    }

}
