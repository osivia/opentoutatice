/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.ui;

import java.io.Serializable;


/**
 * @author david
 *
 */
public interface CustomizeUIService extends Serializable {

    /**
     * Méthode permettant d'adapter les tableaux des vues "Contenu"
     * au mécanisme de publication.
     */
    void adaptContentViews() throws Exception;
    
    /**
     * Permet de surcharger le template d'un layout donné.
     */
    void overrideLayoutsTemplate(LayoutsDescriptor layoutsDescriptor);

}
