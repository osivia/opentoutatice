/**
 * 
 */
package fr.toutatice.ecm.platform.automation.versioning;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.versioning.VersioningService;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david
 *
 */
@Operation(id = CreateExplicitVersion.ID, category = Constants.CAT_DOCUMENT, label = "Create a new major version tagging it as 'explicit'", 
           description = "Create a new major version tagging it as 'explicit' for the input document. "
                   + "Any modification made on the document by the chain will be automatically saved. Returns the version).")
public class CreateExplicitVersion {
    
    /** Operation id. */
    public static final String ID = "Document.CreateExplicitVersion";
    
    @Context
    protected CoreSession session;

    @Param(name = "comment", required = false)
    protected String comment = StringUtils.EMPTY;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        if (!doc.hasFacet(FacetNames.VERSIONABLE)) {
            throw new ClientException(String.format(
                    "The document (id:'%s') with title '%s' doesn't have 'versionable' facet", doc.getId(),
                    doc.getTitle()));
        }
        
        // Create major version tagging it as explicit
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        doc.addFacet(ToutaticeNuxeoStudioConst.CST_FACET_EXPLICIT_VERSION);
        DocumentHelper.saveDocument(session, doc);
        doc.removeFacet(ToutaticeNuxeoStudioConst.CST_FACET_EXPLICIT_VERSION);
        
        return session.getLastDocumentVersion(doc.getRef());
        
    }

}
