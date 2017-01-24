/**
 * 
 */
package fr.toutatice.ecm.platform.core.file;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
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
    
    /** Conversion service. */
    private static ConversionService convService;
    
    /**
     * Getter for Conversion service. 
     */
    public static ConversionService getConversionService(){
        if(convService == null){
            convService = (ConversionService) Framework.getService(ConversionService.class);
        }
        return convService;
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
        if(currentDocument != null && "File".equals(currentDocument.getType())){
            // Get conversion service
            ConversionService conversionService = getConversionService();
            // Can be convert to pdf
            BlobHolder bh = currentDocument.getAdapter(BlobHolder.class);
            Blob blob = bh.getBlob();
            
            if(blob != null){
                String mimeType = blob.getMimeType();
                convertible = "application/pdf".equals(mimeType) || conversionService.isSourceMimeTypeSupported(ANY_2_PDF_CONVERTER, mimeType);
            }
        }
        
        // Result
        infos.put(IS_PDF_CONVERTIBLE, convertible);
        
        return infos;
    }

}
