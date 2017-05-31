/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.converter;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.nuxeo.ecm.platform.ui.web.component.editor.UIHtmlEditor;


/**
 * @author david
 *
 */
public class OttcUIHtmlEditor extends UIHtmlEditor {
    
    private Converter converter;

    /**
     * 
     */
    public OttcUIHtmlEditor() {
        super();
        setRendererType(COMPONENT_TYPE);
        
        Application application = FacesContext.getCurrentInstance().getApplication();
        this.converter = application.createConverter(DocumentContentConverter.class);
    }

}
