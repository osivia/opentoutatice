/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

import org.nuxeo.ecm.platform.ui.web.component.editor.HtmlEditorRenderer;
import org.nuxeo.ecm.platform.ui.web.component.editor.UIHtmlEditor;
import org.nuxeo.ecm.platform.ui.web.htmleditor.api.HtmlEditorPluginService;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class OttcHTMLEditorRenderer extends HtmlEditorRenderer {
    
    private static Map<String, String> pluginsOptions;

    private static Map<String, String> toolbarPluginsOptions;
    
    //private Converter converter;

    /**
     * 
     */
    public OttcHTMLEditorRenderer() {
        super();
    }
    
    @Override
    public void encodeBegin(FacesContext context, UIComponent component)
          throws IOException {

        rendererParamsNotNull(context, component);

    }

    @Override
    protected void getEndTextToRender(FacesContext context,
                                      UIComponent component,
                                      String currentValue) throws IOException {
        
        if (!component.isRendered()) {
            return;
        }

        UIHtmlEditor editorComp = (UIHtmlEditor) component;
        ResponseWriter writer = context.getResponseWriter();
        Locale locale = context.getViewRoot().getLocale();

        // tiny MCE generic scripts now included in every page header

        // script to actually init tinyMCE with configured options
        String editorSelector = editorComp.getEditorSelector();
        // plugins registration
        if (pluginsOptions == null) {
            final HtmlEditorPluginService pluginService = Framework.getLocalService(HtmlEditorPluginService.class);
            pluginsOptions = new HashMap<String, String>();
            pluginsOptions.put("plugins", pluginService.getFormattedPluginsNames());
            toolbarPluginsOptions = new HashMap<String, String>();
            toolbarPluginsOptions.put("toolbar", pluginService.getFormattedToolbarsButtonsNames());
        }

        String clientId = editorComp.getClientId(context);
        boolean disableHtmlInit = Boolean.TRUE.equals(editorComp.getDisableHtmlInit());

        // input text area
        writer.startElement("textarea", editorComp);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("name", clientId, null);
        if (Boolean.TRUE.equals(editorComp.getDisableHtmlInit())) {
            writer.writeAttribute("class", editorSelector + ",disableMCEInit", null);
        } else {
            writer.writeAttribute("class", editorSelector, null);
        }
        writer.writeAttribute("rows", editorComp.getRows(), null);
        writer.writeAttribute("cols", editorComp.getCols(), null);
        //Object currentValue = getCurrentValue(editorComp);
        if (currentValue != null) {
            //writer.writeText(currentValue, null);
            writer.writeText(currentValue, editorComp, "value");
        } else {
            writer.writeText("", null);
        }
        writer.endElement("textarea");

        if (!disableHtmlInit) {
            writer.startElement("script", editorComp);
            writer.writeAttribute("type", "text/javascript", null);
            // Since 5.7.3, use unique clientId instead of editorSelector value
            // so that tiny mce editors are initialized individually: no need
            // anymore to specify a class to know which one should or should
            // not be initialized
            String scriptContent = String.format("initTinyMCE(%s, %s, '%s', '%s', '%s', '%s');", editorComp.getWidth(),
                    editorComp.getHeight(), clientId, pluginsOptions.get("plugins"), locale.getLanguage(),
                    toolbarPluginsOptions.get("toolbar"));
            writer.writeText(scriptContent, null);
            String ajaxScriptContent = String.format(
                    "jsf.ajax.addOnEvent(function(data) {if (data.status == \"success\") {%s}});", scriptContent);
            writer.writeText(ajaxScriptContent, null);
            String scriptContent2 = String.format(
                    "jQuery(document.getElementById('%s')).closest('form').bind('ajaxsubmit', function() {tinyMCE.editors['%s'].save();});",
                    clientId, clientId);
            writer.writeText(scriptContent2, null);
            writer.endElement("script");
        }
        
    }

}
