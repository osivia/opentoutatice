/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.converter;

import org.nuxeo.ecm.platform.ui.web.component.editor.UIHtmlEditor;


/**
 * @author david
 *
 */
public class OttcUIHtmlEditor extends UIHtmlEditor {

    /**
     * To associate converter for validation phase.
     */
    public OttcUIHtmlEditor() {
        super();

        super.setConverter(new DocumentContentConverter());
    }


}
