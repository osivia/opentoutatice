package fr.toutatice.ecm.platform.web.adapters;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBlobHolderFactory;

/**
 * Cette surcharge a été rendue nécessaire pour le bon fonctionnement de la vue "toutatice_view" sur un document de type "PictureBook".
 * Le ticket Jira suivant identifie le souci: https://jira.nuxeo.com/browse/SUPNXP-7421
 */
public class ToutaticePictureBlobHolderFactory extends PictureBlobHolderFactory {

	@Override
	public BlobHolder getBlobHolder(DocumentModel doc) {
		BlobHolder bh = null;
		
		if ("PictureBook".equals(doc.getType())) {
			bh = new ToutaticePictureBookBlobHolder(doc, "");
		} else {
			bh = super.getBlobHolder(doc);
		}
		
		return bh;
	}

}
