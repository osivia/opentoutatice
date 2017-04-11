/**
 * 
 */
package fr.toutatice.ecm.platform.automation.document;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;


/**
 * @author david
 *
 */
@Operation(id = AnyBlobToPDF.ID)
public class AnyBlobToPDF {

    /** Logger. */
    private static final Log log = LogFactory.getLog(AnyBlobToPDF.class);

    /** Operation's ID. */
    public static final String ID = "Blob.AnyToPDF";

    @Context
    protected ConversionService service;

    @Param(name = "converterName", required = false)
    protected String converterName = "toutaticeAny2pdf";

    /**
     * Convert blob of document to pdf with given converter.
     * Converter any2pdf is used by default.
     * 
     * @param doc
     * @return document
     * @throws Exception
     */
    @OperationMethod
    public Blob run(DocumentModel doc) throws Exception {
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        if (bh == null) {
            return null;
        }
        if ("application/pdf".equals(bh.getBlob().getMimeType())) {
            return bh.getBlob();
        }

        // Result
        Blob result = null;

        // Get modified date to compute cache key
        Calendar modDate = (GregorianCalendar) doc.getPropertyValue("dc:modified");
        Map<String, Serializable> cacheKeyParams = new HashMap<String, Serializable>();
        cacheKeyParams.put("modifiedOn", modDate.getTimeInMillis());

        // try {
            BlobHolder pdfBh = this.service.convert(this.converterName, bh, cacheKeyParams);
            result = pdfBh.getBlob();

            String fname = result.getFilename();
            String filename = bh.getBlob().getFilename();
            if (filename != null && !filename.isEmpty()) {
                // add pdf extension
                int pos = filename.lastIndexOf('.');
                if (pos > 0) {
                    filename = filename.substring(0, pos);
                }
                filename += ".pdf";
                result.setFilename(filename);
            } else if (fname != null && !fname.isEmpty()) {
                result.setFilename(fname);
            } else {
                result.setFilename("file");
            }

            result.setMimeType("application/pdf");
        // } catch (ConversionException te) {
        // return manageConversionException(te, doc);
        // }

        return result;
    }

    /**
     * Return portal messages on conversion service exception.
     * 
     * @param te
     * @return
     */
    // private Blob manageConversionException(ConversionException ce, DocumentModel document) {
    // // Log
    // log.error("Error converting: " + document.getPathAsString(), ce);
    // // FIXME: suffisant??
    // return null;
    // }

}
