/**
 * 
 */
package fr.toutatice.ecm.platform.automation.document;

import java.io.IOException;

import org.nuxeo.ecm.automation.ConflictOperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
@Operation(id = UpdateDocument.ID)
public class UpdateDocument extends AbstractDublinCoreDocumentUpdate {

    public static final String ID = "Document.TTCUpdate";

    @Context
    protected CoreSession session;

    @Param(name = "properties")
    protected Properties properties;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @Param(name = "changeToken", required = false)
    protected String changeToken = null;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        DocumentModel updatedDoc = doc;

        if (changeToken != null) {
            // Check for dirty update
            String repoToken = doc.getChangeToken();
            if (!changeToken.equals(repoToken)) {
                throw new ConflictOperationException(doc);
            }
        }
        
        if (this.properties != null) {
            // Updates taking DublinCore properties into account
            updatedDoc = super.executeSplittingProperties(this.session, doc, this.properties, this.save);
        } else {
            // Updates normally
            updatedDoc = execute(this.session, doc, this.properties, this.save);
        }

        return updatedDoc;
    }
    
    /**
     * Updates document normally.
     * 
     * @param session
     * @param document
     * @param properties
     * @param save
     * @return updated document
     * @throws NuxeoException
     * @throws IOException
     */
    @Override
    protected DocumentModel execute(CoreSession session, DocumentModel document, Properties properties, boolean save) throws NuxeoException, IOException {
        DocumentHelper.setProperties(session, document, properties);
        if(save){
            document = session.saveDocument(document); 
        }
        return document;
    }
    
    /**
     * Update document setting Dublincore properties.
     * 
     * @param session
     * @param document
     * @param properties
     * @param dublinCoreProperties
     * @return document
     * @throws NuxeoException
     * @throws IOException
     */
    @Override
    protected DocumentModel execute(CoreSession session, DocumentModel document, Properties properties, Properties dublinCoreProperties, boolean save) throws NuxeoException, IOException {
        // Update document without given dublincore properties:
        // DublinCoreListener sets them
        DocumentHelper.setProperties(session, document, properties);
        DocumentModel updatedDocument = document;
        if(save){
            updatedDocument = session.saveDocument(document);
        }
        
        // Set dublincore properties and save silently to shortcut DublinCoreListener
        DocumentHelper.setProperties(session, updatedDocument, dublinCoreProperties);
        if(save){
            ToutaticeDocumentHelper.saveDocumentSilently(session, updatedDocument, false);
        }
        
        return updatedDocument;
    }
}
