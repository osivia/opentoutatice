/**
 * 
 */
package fr.toutatice.ecm.platform.core.file;

import java.util.HashMap;
import java.util.Map;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.convert.ooomanager.OOoManagerService;
import org.nuxeo.ecm.platform.mimetype.MimetypeDetectionException;
import org.nuxeo.ecm.platform.mimetype.MimetypeNotFoundException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;


/**
 * @author david
 *
 */
public class FileInfosProvider implements DocumentInformationsProvider {

    /** Is pdf convertible property. */
    public static final String IS_PDF_CONVERTIBLE = "isPdfConvertible";
    /** Converter name. */
    public static final String ANY_2_PDF_CONVERTER = "any2pdf";

    /** MimetypeRegistry. */
    private static MimetypeRegistry mimetypeRegistry;

    /** Office converter. */
    private static OfficeDocumentConverter officeConverter;

    /**
     * Getter for Office converter.
     */
    public static OfficeDocumentConverter getOfficeDocumentConverter() {
        if (officeConverter == null) {
            OOoManagerService oooManagerService = Framework.getService(OOoManagerService.class);
            officeConverter = oooManagerService.getDocumentConverter();
        }
        return officeConverter;
    }

    /**
     * Getter for MimetypeRegistry.
     */
    public static MimetypeRegistry getMimetypeRegistry() {
        if (mimetypeRegistry == null) {
            mimetypeRegistry = (MimetypeRegistry) Framework.getService(MimetypeRegistry.class);
        }
        return mimetypeRegistry;
    }

    /**
     * Checks if File is convertible as pdf.
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
        // Infos
        Map<String, Object> infos = new HashMap<String, Object>(1);

        // Convertible to pdf
        boolean convertible = false;

        // Must be File
        if (currentDocument != null && "File".equals(currentDocument.getType())) {
            // Can be convert to pdf
            BlobHolder bh = currentDocument.getAdapter(BlobHolder.class);
            Blob blob = bh.getBlob();

            if (blob != null) {
                String mimeType = blob.getFilename();
                if ("application/pdf".equals(mimeType)) {
                    convertible = true;
                } else {

                    try {
                        // Sniff mimeType from Blob (NOT use of srcMimeType of any2pdf converter)
                        // and use of possible OfficeDocumentConverter inputMimeTyes
                        String mimetypeStr = getMimetypeRegistry().getMimetypeFromBlob(blob);
                        convertible = getOfficeDocumentConverter().getFormatRegistry().getFormatByMediaType(mimetypeStr) != null;
                    } catch (MimetypeNotFoundException | MimetypeDetectionException e) {
                        convertible = false;
                    }

                }

            }
        }

        // Result
        infos.put(IS_PDF_CONVERTIBLE, convertible);

        return infos;
    }

}
