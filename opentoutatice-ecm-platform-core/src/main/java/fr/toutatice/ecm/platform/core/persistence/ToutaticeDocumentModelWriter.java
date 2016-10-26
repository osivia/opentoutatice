/**
 * 
 */
package fr.toutatice.ecm.platform.core.persistence;

import java.io.IOException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.io.DocumentTranslationMap;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class ToutaticeDocumentModelWriter extends DocumentModelWriter {
    
    /** Logger. */
    private static final Log log = LogFactory.getLog(ToutaticeDocumentModelWriter.class);

    /**
     * @param session
     * @param parentPath
     */
    public ToutaticeDocumentModelWriter(CoreSession session, String parentPath) {
        super(session, parentPath);
    }

    /**
     * @param session
     * @param parentPath
     * @param saveInterval
     */
    public ToutaticeDocumentModelWriter(CoreSession session, String parentPath, int saveInterval) {
        super(session, parentPath, saveInterval);
    }
    
    /**
     * To throw creation or update document exception.
     */
    @Override
    public DocumentTranslationMap write(ExportedDocument xdoc) throws IOException {
        DocumentTranslationMap dtm = super.write(xdoc);
        
        Boolean compatibilityImport = Boolean.valueOf(Framework.getProperty("ottc.import.archive.compatibility", "true"));
        // Maybe error in creation or update
        if(dtm == null){
            // (Re-) Check schema definition in case of creation
            try {
                Path xDocPath = xdoc.getPath();
                Path parentPath = xDocPath.removeLastSegments(1);
                String name = xDocPath.lastSegment();
                DocumentModel docModel = new DocumentModelImpl(parentPath.toString(), name,
                        xdoc.getType());
                super.loadSchemas(xdoc, docModel, xdoc.getDocument());
            } catch (ClientException ce) {
                if(BooleanUtils.isNotTrue(compatibilityImport)){
                    if(log.isDebugEnabled()){
                        log.debug(ce.getMessage(), ce);
                    }
                    throw new IOException(ce.getMessage(), ce);
                }
            }
        }
        
        return dtm;
    }

}
