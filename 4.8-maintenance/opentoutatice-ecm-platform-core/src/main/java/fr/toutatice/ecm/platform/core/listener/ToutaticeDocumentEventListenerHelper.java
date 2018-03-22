/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.schema.FacetNames;


/**
 * @author david
 *
 */
public class ToutaticeDocumentEventListenerHelper {

    /**
     * Utility class.
     */
    private ToutaticeDocumentEventListenerHelper() {
        super();
    }

    /**
     * Alterable document is not a System document, is not a version and is not Immutable.
     * 
     * @param srcDoc
     * @return bbolean
     */
    public static boolean isAlterableDocument(DocumentModel srcDoc){
        return !srcDoc.hasFacet(FacetNames.SYSTEM_DOCUMENT) && !srcDoc.isVersion() && !srcDoc.hasFacet(FacetNames.IMMUTABLE);
    }

}
