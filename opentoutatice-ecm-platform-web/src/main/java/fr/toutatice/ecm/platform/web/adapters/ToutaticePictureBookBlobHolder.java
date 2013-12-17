package fr.toutatice.ecm.platform.web.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.picture.api.adapters.PictureBookBlobHolder;

/**
 * Cette surcharge a été rendue nécessaire pour le bon fonctionnement de la vue "toutatice_view" sur un document de type "PictureBook".
 * Le ticket Jira suivant identifie le souci: https://jira.nuxeo.com/browse/SUPNXP-7421
 */
public class ToutaticePictureBookBlobHolder extends PictureBookBlobHolder {

	private static final Log log = LogFactory.getLog(ToutaticePictureBookBlobHolder.class);
	
	public ToutaticePictureBookBlobHolder(DocumentModel doc, String xPath) {
		super(doc, xPath);
	}

	@Override
	public Blob getBlob() throws ClientException {
		Blob b = null;
		
		try {
			b = super.getBlob();
		} catch (Exception e) {
			log.warn("Failed to get the blob item from the document '" + this.doc.getTitle() + "', error: " + e.getMessage());
		}
		
		return b;
	}

}
