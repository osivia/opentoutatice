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
package fr.toutatice.ecm.platform.web.webeditor;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.jboss.seam.annotations.web.RequestParameter;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.ui.web.tag.fn.LiveEditConstants;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.note.EditorImageActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeEsQueryHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeFileHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeImageCollectionHelper;

@Name("editorImageActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeEditorImageActionsBean extends EditorImageActionsBean {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(ToutaticeEditorImageActionsBean.class);

	@In(required = false, create = true)
	protected transient DocumentsListsManager documentsListsManager;

	private static final String SEARCH_QUERY = "SELECT * FROM Document WHERE %s";

	private static final String THUMBNAIL_SRC_PATH = "%snxthumb/default/%s/blobholder:0/%s";

	@In(create = true, required = false)
	private transient CoreSession documentManager;

	private List<DocumentModel> resultDocuments;
	private boolean hasSearchResults = false;

	private static final int SEARCH_IN_MEDIA = 0;
	private static final int SEARCH_IN_SPACE = 1;
	private int searchInSpace = 0;

	private String selectedSize = "Medium";
	private String imageUrlAttr;
	private boolean isImageUploadedAttr = false;

	private boolean isImage = true;

	@RequestParameter
	private String selectedTab;

	private String oldSelectedTab;

	/**
	 * To know if we are on library root (tinymce image plugin)
	 */
	@RequestParameter
	private String libraryRoot;

	/**
	 * Current node of libraryTree;
	 */
	@RequestParameter
	private String currentLibraryNodeId;
	
	/**
	 * Node before currentLibraryNode;
	 */
	@RequestParameter
	private String previousLibraryNodeId;

	private DocumentModel currentLibraryNode;

	/**
	 * Previous node compared to Current node of libraryTree;
	 */
	private DocumentModel previousLibraryNode;

	public boolean getLibraryRoot() {
		boolean root = true;
		if (StringUtils.isNotBlank(libraryRoot)) {
			root = Boolean.valueOf(libraryRoot).booleanValue();
		}
		return root;
	}

	public void setLibraryRoot(boolean root) {
		this.libraryRoot = String.valueOf(root);
	}
	
	public String getCurrentLibraryNodeId() {
		return currentLibraryNodeId;
	}

	public void setCurrentLibraryNodeId(String currentLibraryNodeId) {
		this.currentLibraryNodeId = currentLibraryNodeId;
	}
	
	public String getPreviousLibraryNodeId() {
		return previousLibraryNodeId;
	}

	public void setPreviousLibraryNodeId(String previousLibraryNodeId) {
		this.previousLibraryNodeId = previousLibraryNodeId;
	}

	public DocumentModel getCurrentLibraryNode() {
		if (getLibraryRoot()) {
			currentLibraryNode = ToutaticeDocumentHelper.getMediaSpace(
					navigationContext.getCurrentDocument(), documentManager);
		} else if (StringUtils.isNotBlank(currentLibraryNodeId)) {
			IdRef idRef = new IdRef(currentLibraryNodeId);
			currentLibraryNode = documentManager.getDocument(idRef);
		}
		return currentLibraryNode;
	}
	
	public DocumentModel getPreviousLibraryNode(){
		if(previousLibraryNodeId != null){
			IdRef idRef = new IdRef(previousLibraryNodeId);
			previousLibraryNode = documentManager.getDocument(idRef);
		}
		return previousLibraryNode;
	}
	
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

	public String getMediaSpaceName() throws ClientException {
		DocumentModel doc = navigationContext.getCurrentDocument();
		if (ToutaticeDocumentHelper.getMediaSpace(doc, documentManager) != null) {
			return ToutaticeDocumentHelper.getMediaSpace(doc, documentManager)
					.getTitle();
		} else
			return null;

	}

	public String getSpaceName() throws ClientException {
		DocumentModel space = navigationContext.getCurrentSuperSpace();
		if (null != space) {
			return space.getTitle();
		}
		searchInSpace = 2;
		return null;
	}

	public int getSearchInSpace() throws ClientException {

		// Si pas de médiathèque, l'option n'est pas disponible
		if (searchInSpace == 0) {
			if (ToutaticeDocumentHelper.getMediaSpace(
					navigationContext.getCurrentDocument(), documentManager) == null) {
				searchInSpace = 1;
			}
		}
		return searchInSpace;
	}

	public void setSearchInSpace(int searchInSpace) {
		this.searchInSpace = searchInSpace;
	}

	/*
	 * Méthode remplaçant, pour le template editor_image_upload.xhtml,
	 * getInCreationMode() (qui retourne vrai si le document n'a pas le schéma
	 * "files"); on estime désormais que pour insérer une image dans le tinyMCE,
	 * ce schéma n'est pas nécessaire puisque l'image est désormais conservée
	 * dans ttc:images (le schéma toutatice est porté par tous les documents).
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
			return !doc
					.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE);
		}
	}

	public String searchImages(String view) throws ClientException {
		searchImages();
		return view;
	}

	@Override
	public String searchImages() throws ClientException {
		String searchKeywords = getSearchKeywords();

		log.debug("Entering searchDocuments with keywords: " + searchKeywords);

		resultDocuments = null;
		final List<String> constraints = new ArrayList<String>();

		// add keywords
		if (searchKeywords != null) {
			searchKeywords = searchKeywords.trim();
			if (searchKeywords.length() > 0) {
				if (!searchKeywords.equals("*")) {
					// full text search
					constraints.add(String.format("ecm:fulltext LIKE '%s'",
							searchKeywords));
				}
			}
		}

		// restrict to space if required
		if (searchInSpace == SEARCH_IN_MEDIA
				&& ToutaticeDocumentHelper
						.getMediaSpace(navigationContext.getCurrentDocument(),
								documentManager) != null) {
			constraints.add("ecm:path STARTSWITH '"
					+ ToutaticeDocumentHelper
							.getMediaSpace(
									navigationContext.getCurrentDocument(),
									documentManager).getPathAsString()
							.replace("'", "\\'") + "'");
		} else if (searchInSpace == SEARCH_IN_SPACE) {
			constraints.add("ecm:path STARTSWITH '"
					+ navigationContext.getCurrentSuperSpace()
							.getPathAsString().replace("'", "\\'") + "'");
		}

		constraints.add("ecm:primaryType = 'Picture'");
		constraints.add("ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'");

		final String query = String.format(SEARCH_QUERY,
				StringUtils.join(constraints, " AND "));

        // Log Timer
        final long begin = System.currentTimeMillis();
        // Query
        resultDocuments = ToutaticeEsQueryHelper.query(documentManager, query, 100);
        // Log timer
        if (log.isDebugEnabled()) {
            final long end = System.currentTimeMillis();
            log.debug("#searchImages: " + String.valueOf(end - begin) + " ms");
        }


		hasSearchResults = !resultDocuments.isEmpty();
		log.debug("query result contains: " + resultDocuments.size() + " docs.");
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
	
	@Override
	public String getSelectedSize() {
		return this.selectedSize;
	}
	
	@Override
	public void setSelectedSize(final String selectedSize) {
		this.selectedSize = selectedSize;
	}
	
	@Override
	public String getImageProperty() {
		return this.selectedSize + ":content";
	}

	@Override
	public String getSelectedTab() {
		if (selectedTab != null) {
			oldSelectedTab = selectedTab;
		} else if (oldSelectedTab == null) {
			oldSelectedTab = "ATTACH_IMGS";
		}
		return oldSelectedTab;
	}

	public boolean getIsImage() {
		return this.isImage;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String uploadImage() throws ClientException {
		Part uploadedImg = getUploadedImage();
		String uploadedImgName = FileUtils.retrieveFilename(uploadedImg);
		if (null == uploadedImg || StringUtils.isBlank(uploadedImgName)) {
			return null;
		}

		try {
			final DocumentModel doc = navigationContext.getCurrentDocument();
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
				
				documentManager.saveDocument(doc);
				documentManager.save();
				
				imageUrlAttr = DocumentModelFunctions.complexFileUrl("downloadFile",
						doc,
						ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES,
						files.indexOf(item),
						LiveEditConstants.DEFAULT_SUB_BLOB_FIELD,
						uploadedImgName);
				isImageUploadedAttr = true;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String addImageFromNuxeo() throws ClientException {
		DocumentModel currentDoc = navigationContext.getCurrentDocument();

		List<DocumentModel> selectedDocumentList = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

		if (null != selectedDocumentList && 0 < selectedDocumentList.size()) {
			List<Map<String, Object>> files = (List<Map<String, Object>>) currentDoc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);

			for (DocumentModel selectedDocument : selectedDocumentList) {
				Map<String, Object> picture = selectedDocument.getProperties("picture");

				List<Map<String, Object>> views = (List) picture.get("views");
				for (Map<String, Object> view : views) {
					if ("original".equalsIgnoreCase((String) view.get("tag"))) {
						Blob blob = (Blob) view.get("content");

						Map<String, Object> fileMap = new HashMap<String, Object>(
								2);
						fileMap.put("file", blob);
						fileMap.put("filename", selectedDocument.getTitle());
						if (!files.contains(fileMap)) {
							ToutaticeImageCollectionHelper.instance().add(
									files, fileMap);
						}
						break;
					}
				}
			}

			documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_SELECTION);

			// sauvegarder les modifications
			currentDoc.setPropertyValue(
					ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES,
					(Serializable) files);
			documentManager.saveDocument(currentDoc);
			documentManager.save();
			// Rafraîchir la liste des images
			// Events.instance().raiseEvent(EventNames.DOCUMENT_CHANGED,
			// currentDoc);

		}
		// "Navigation"...
		return "";
	}

	public String searchVideos(String view) throws ClientException {
		super.searchVideos();
		return view;
	}

	public String getThumbnailSrc(DocumentModel pictureOrFolder) {
		String src = StringUtils.EMPTY;
		if (pictureOrFolder != null) {
			String baseUrl = BaseURL.getBaseURL();
			if (pictureOrFolder.isFolder()) {
				src = baseUrl + "icons/workspace.gif";
			} else {
				src = String.format(THUMBNAIL_SRC_PATH, baseUrl,
						pictureOrFolder.getId(), pictureOrFolder.getTitle());
			}
		}
		return src;
	}

	public List<DocumentModel> getLibraryPicturesNFolders(DocumentModel node) {
		List<DocumentModel> picturesNFolders = new ArrayList<DocumentModel>(0);
		Filter filter = new Filter() {

			private static final long serialVersionUID = 2878502523162688751L;

			@Override
			public boolean accept(DocumentModel docModel) {
				boolean isNotProxy = !docModel.isProxy();
				boolean isPicture = ToutaticeNuxeoStudioConst.CST_DOC_TYPE_PICTURE
						.equals(docModel.getType());
				boolean isFolder = docModel.isFolder();
				return isNotProxy && (isPicture || isFolder);
			}

		};
		if(node!=null){
			DocumentModelList children = documentManager.getChildren(node.getRef(),
					null, filter, null);
			if (children != null && !children.isEmpty()){
				picturesNFolders.addAll(children);
			}
		}
		return picturesNFolders;
	}

	public String goTo(String viewId) {
		return viewId;
	}
}
