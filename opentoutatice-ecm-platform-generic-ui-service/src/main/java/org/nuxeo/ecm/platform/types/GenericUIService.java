/**
 * 
 */
package org.nuxeo.ecm.platform.types;

import java.io.Serializable;

/**
 * @author david
 *
 */
public interface GenericUIService extends Serializable {
	
    /**
     * Méthode permettant d'jouter dans des vues des documents des layouts génériques
     * ou transverses à tous les documents.
     */
	void addGenericUIElements(GenericLayoutsDescriptor genericLayoutsDescriptor) throws Exception;
	
}
