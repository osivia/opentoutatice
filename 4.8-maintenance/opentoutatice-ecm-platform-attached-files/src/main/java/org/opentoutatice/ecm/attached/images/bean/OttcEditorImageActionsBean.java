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
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.core.io.download.OttcDownloadServiceImpl;
import org.opentoutatice.core.io.download.TransientDocumentCache;

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

    /** Cache key of changeable document. */
    private String virtualChangeableDocId = StringUtils.EMPTY;

    private boolean isImage = true;


    /**
     * Constructor.
     */
    public OttcEditorImageActionsBean() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String uploadImage() throws NuxeoException {
        Part uploadedImg = getUploadedImage();
        if (uploadedImg == null) {
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

                doc.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES, (Serializable) files);

                // Update document mode
                if (StringUtils.isNotBlank(doc.getId())) {
                    documentManager.saveDocument(doc);
                } else {
                    // Creating document mode: store empty document model to resolve attached images
                    // (cf OttcDownloadService)
                    this.virtualChangeableDocId = String.valueOf(System.currentTimeMillis()) + "-" + OttcDownloadServiceImpl.CREATING_DOC_INDICATOR;
                    TransientDocumentCache.put(this.virtualChangeableDocId, doc);
                }
            } else {
                // Le document que l'on cherche à uploader n'a pas le bon type mime. Abandon + message d'erreur (dans page JSF)
                this.isImage = false;
                log.debug("The binary file to upload hasn't the correct mimetype.");
            }

            return "editor_image_upload";
        } catch (IOException e) {
            log.error("Failed to upload the image, error: " + e.getMessage());
            throw new NuxeoException(e);
        }
    }

    public String getDownloadUrl(DocumentModel doc, String xpath, String filename) {
        String url = null;

        DownloadService downloadService = Framework.getService(DownloadService.class);

        // Document creation mode
        if (doc.getRef() == null) {
            url = downloadService.getDownloadUrl(this.documentManager.getRepositoryName(), this.virtualChangeableDocId, xpath, filename);
        } else {
            url = downloadService.getDownloadUrl(doc, xpath, filename);
        }

        return url;
    }


    public boolean isImage() {
        return isImage;
    }

    public String getVirtualChangeableDocId() {
        return virtualChangeableDocId;
    }


    public void setVirtualChangeableDocId(String virtualChangeableDocId) {
        this.virtualChangeableDocId = virtualChangeableDocId;
    }

}
