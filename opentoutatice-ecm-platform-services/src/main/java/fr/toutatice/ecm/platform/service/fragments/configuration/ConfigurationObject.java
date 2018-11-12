/**
 * 
 */
package fr.toutatice.ecm.platform.service.fragments.configuration;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

import com.lowagie.text.xml.xmp.DublinCoreSchema;


/**
 * @author david
 *         FIXME: todo with <extension point="adapters" target="org.nuxeo.ecm.core.api.DocumentAdapterService">
 */
public class ConfigurationObject {
    
    private ConfigurationObject() {};
    
    public static String getTitle(DocumentModel configDoc) {
        return getStringProperty(configDoc, DublinCoreSchema.TITLE);
    }

    public static String getCode(DocumentModel configDoc){
        return getStringProperty(configDoc, ConfigurationConstants.WCONF_CODE_XPATH);
    }

    public static String getCode2(DocumentModel configDoc) {
        return getStringProperty(configDoc, ConfigurationConstants.WCONF_CODE2_XPATH);
    }

    /**
     * @param configDoc
     * @return
     * @throws PropertyException
     * @throws ClientException
     */
    private static String getStringProperty(DocumentModel configDoc, String xPathProp) throws PropertyException, ClientException {
        return (String) configDoc.getPropertyValue(xPathProp);
    }

}
