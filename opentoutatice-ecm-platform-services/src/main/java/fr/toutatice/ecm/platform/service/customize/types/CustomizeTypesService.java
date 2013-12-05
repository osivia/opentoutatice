/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.types;

import java.io.Serializable;

import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.platform.types.Type;

/**
 * @author david
 *
 */
public interface CustomizeTypesService extends Serializable {
	
    /**
     * Ajout des schémas toutatice, uid et de la facet Versionable
     * à tous les types de document.
     */
    void addToutaticeDocType(DocumentTypeDescriptor baseDocTypeDescriptor);

    /**
     * Ajout dynamique de schémas, facets à un document openToutatice.
     * 
     */
    void addDocTypeContrib(DocumentTypeDescriptor docTypeDescriptor) throws Exception;
	
}
