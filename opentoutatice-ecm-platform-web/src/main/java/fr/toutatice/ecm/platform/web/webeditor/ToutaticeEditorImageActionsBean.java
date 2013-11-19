package fr.toutatice.ecm.platform.web.webeditor;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.ui.web.tag.fn.LiveEditConstants;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.note.EditorImageActionsBean;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeFileHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeImageCollectionHelper;

@Name("editorImageActions")
@Scope(CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class ToutaticeEditorImageActionsBean extends EditorImageActionsBean {

	private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(EditorImageActionsBean.class);
    
    @In(required = false, create = true)
    protected transient DocumentsListsManager documentsListsManager;
    
    private static final String SEARCH_QUERY = "SELECT * FROM Document WHERE %s";
    private static final String FILES_SCHEMA = "files";
    private static final String TTC_SCHEMA = "toutatice";
    private static final String IMAGES_PROPERTY = "images";
    private static final String ATTACH_IMAGES = "AttachableImages";

    @In(create = true, required = false)
    private transient CoreSession documentManager;

    private List<DocumentModel> resultDocuments;
    private boolean hasSearchResults = false;
    private boolean searchInSpace = true;
	private String selectedSize = "Medium";
    private String imageUrlAttr;
    private boolean isImageUploadedAttr = false;
    /* DCH */
    private boolean isImage = true;
    
	public String getImageUrlAttr() {
		return imageUrlAttr;
	}

	public void setImageUrlAttr(String imageUrlAttr) {
		this.imageUrlAttr = imageUrlAttr;
	}

	public boolean isImageUploadedAttr() {
		return isImageUploadedAttr;
	}

	public void setImageUploadedAttr(boolean isImageUploadedAttr) {
		this.isImageUploadedAttr = isImageUploadedAttr;
	}
	
	@Override
	public String getUrlForImage() {
		super.getUrlForImage();
		isImageUploadedAttr = false;
        return imageUrlAttr;
	}

	@Override
	public boolean getIsImageUploaded() {
		return isImageUploadedAttr;
	}

	public String getSpaceName() throws ClientException{
		DocumentModel space;
		space = navigationContext.getCurrentSuperSpace();
		if(null!=space) {
			return space.getTitle();
		}
		searchInSpace = false;
		return null;
	}

	public boolean isSearchInSpace() {
		return searchInSpace;
	}

	public void setSearchInSpace(boolean searchInSpace) {
		this.searchInSpace = searchInSpace;
	}
	
	/*
	 * Méthode remplaçant, pour le template editor_image_upload.xhtml,
	 * getInCreationMode() (qui retourne vrai si le document n'a pas le
	 * schéma "files"); on estime désormais que pour insérer une image 
	 * dans le tinyMCE, ce schéma n'est pas nécessaire puisque l'image
	 * est désormais conservée dans ttc:images (le schéma toutatice est 
	 * porté par tous les documents).
	 */
    public boolean getInToutaticeCreationMode() {
	    DocumentModel doc = navigationContext.getChangeableDocument();
        if (doc == null) {
            doc = navigationContext.getCurrentDocument();
        }
        if (doc == null) {
            return false;
        }
        if (doc.getId() == null) {
            return true;
        } else {
            return !doc.hasSchema(TTC_SCHEMA);
        }
    }

	@Override
	public String searchImages() throws ClientException {
		String searchKeywords = getSearchKeywords();

		log.debug("Entering searchDocuments with keywords: " + searchKeywords);

        resultDocuments = null;
        final List<String> constraints = new ArrayList<String>();
        if (searchKeywords != null) {
            searchKeywords = searchKeywords.trim();
            if (searchKeywords.length() > 0) {
                if (!searchKeywords.equals("*")) {
                    // full text search
                    constraints.add(String.format("ecm:fulltext LIKE '%s'", searchKeywords));
                }
            }
        }
        if (searchInSpace) {
        	constraints.add("ecm:path STARTSWITH '"+navigationContext.getCurrentSuperSpace().getPathAsString().replace("'", "\\'")+"'");
        }	

        constraints.add("ecm:primaryType = 'Picture'");
        constraints.add("ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'");

        final String query = String.format(SEARCH_QUERY, StringUtils.join(
                constraints, " AND "));
        resultDocuments = documentManager.query(query, 100);
        hasSearchResults = !resultDocuments.isEmpty();
        log.debug("query result contains: " + resultDocuments.size()
                + " docs.");
        return "editor_image_upload";
    }

	@Override
	public boolean getHasSearchResults() {
		return hasSearchResults;
	}
	
	@Override
    public List<DocumentModel> getSearchImageResults() {
        return resultDocuments;
    }
	
    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(final String selectedSize) {
        this.selectedSize = selectedSize;
    }
    
    /*
     * Modif DCH
     */
    public boolean getIsImage(){
        return this.isImage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String uploadImage() throws ClientException {
        isImage = true;
        String XPATH_FILES = NuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES;

        InputStream uploadedImage = getUploadedImage();
        String uploadedImageName = getUploadedImageName();
        if (uploadedImage == null || uploadedImageName == null) {
            return "";
        }

        final DocumentModel doc = navigationContext.getCurrentDocument();

        uploadedImageName = FileUtils.getCleanFileName(uploadedImageName);
        Blob uploadedImageBlob = FileUtils.createSerializableBlob(uploadedImage, uploadedImageName, null);
        boolean isImage = ToutaticeFileHelper.instance().isImageTypeFile(uploadedImageName, uploadedImageBlob);

        if (!isImage) {
            // Associer le fichier uploadé à la méta-donnée "files:files" s'il ne s'agit pas d'une image
            // XPATH_FILES = NuxeoStudioConst.CST_DOC_XPATH_NUXEO_FILES;
            /* DCH */
            this.isImage = false;
            return "editor_image_upload";
        }

        List<Map<String, Object>> files = (List<Map<String, Object>>) doc.getPropertyValue(XPATH_FILES);

        final Map<String, Object> item = new HashMap<String, Object>();
        item.put("filename", uploadedImageName);
        item.put("file", uploadedImageBlob);

        if (!doc.hasFacet(ATTACH_IMAGES) && doc.hasSchema(FILES_SCHEMA)) {
            XPATH_FILES = NuxeoStudioConst.CST_DOC_XPATH_NUXEO_FILES;
        }
        ToutaticeImageCollectionHelper.instance().add(files, item);
        doc.setPropertyValue(XPATH_FILES, (Serializable) files);

        documentManager.saveDocument(doc);
        documentManager.save();

        imageUrlAttr = DocumentModelFunctions.complexFileUrl("downloadFile", doc, XPATH_FILES, files.indexOf(item), LiveEditConstants.DEFAULT_SUB_BLOB_FIELD,
                uploadedImageName);
        isImageUploadedAttr = true;

        return "editor_image_upload";
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
    public String addImageFromNuxeo() throws ClientException {
    	DocumentModel currentDoc = navigationContext.getCurrentDocument();
    	
        List<DocumentModel> selectedDocumentList = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        
        if (null != selectedDocumentList && 0 < selectedDocumentList.size()) {
        	List<Map<String, Object>> files = (List<Map<String, Object>>) currentDoc.getPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
        	
        	for (DocumentModel selectedDocument : selectedDocumentList) {
        		Map<String, Object> picture = selectedDocument.getProperties("picture");
        		
        		List<Map<String, Object>> views = (List) picture.get("views");
        		for (Map<String, Object> view : views) {
        			if ("original".equalsIgnoreCase((String) view.get("tag"))) {
        				Blob blob = (Blob) view.get("content");
        				
        				Map<String, Object> fileMap = new HashMap<String, Object>(2);
        				fileMap.put("file", blob);
        				fileMap.put("filename", selectedDocument.getTitle());
        				if (!files.contains(fileMap)) {
        					ToutaticeImageCollectionHelper.instance().add(files, fileMap);
        				}
        				break;
        			}
        		}
        	}
        	
        	documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_SELECTION);
        	
        	// sauvegarder les modifications
        	currentDoc.setPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES, (Serializable) files);
        	documentManager.saveDocument(currentDoc);
        	documentManager.save();
        	// Rafraîchir la liste des images
    		//Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED, currentDoc);
        	
        }
        //"Navigation"...
    	return "";
    }

}
