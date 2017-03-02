/**
 * 
 */
package fr.toutatice.ecm.platform.core.file;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
    /** Error on pdf conversion. */
    private static final String ERROR_ON_PDF_CONVERSION = "errorOnPdfConversion";
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
                String mimeType = blob.getMimeType();
                if ("application/pdf".equals(mimeType)) {
                    convertible = true;
                } else {
                    // Use of possible OfficeDocumentConverter inputMimeTyes
                    OfficeDocumentConverter converter = getOfficeDocumentConverter();
                    if (converter != null) {
                        convertible = converter.getFormatRegistry().getFormatByMediaType(mimeType) != null;

                        // If not found, sniff mimeType from Blob (NOT use of srcMimeType of any2pdf converter)
                        String sniffedMimeType = null;
                        if (!convertible) {
                            try {
                                sniffedMimeType = getMimetypeRegistry().getMimetypeFromBlob(blob);
                            } catch (MimetypeNotFoundException | MimetypeDetectionException e) {
                                // MagicMimetype lib (called from getMimetypeFromBlob) can't found mimetype ...
                                // but we know it is a text mimetype (case seen for ldif)
                                // Lt's force it to text/plain (!)
                                if (StringUtils.startsWith(mimeType, "text/")) {
                                    blob.setMimeType("text/plain");
                                    bh.setBlob(blob);
                                    // Save adapted document (as BlobHolder)
                                    coreSession.saveDocument(currentDocument);
                                }
                            }

                            // Ckeck with OfficeDocumentConverter
                            convertible = converter.getFormatRegistry().getFormatByMediaType(sniffedMimeType) != null;
                        }
                    } else {
                        infos.put(ERROR_ON_PDF_CONVERSION, true);
                    }
                }

            }
        }

        // Result
        infos.put(IS_PDF_CONVERTIBLE, convertible);

        return infos;
    }

}
