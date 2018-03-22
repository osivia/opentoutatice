/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.ui.web.component.editor.HtmlEditorRenderer;
import org.nuxeo.ecm.platform.ui.web.component.editor.UIHtmlEditor;
import org.nuxeo.ecm.platform.ui.web.htmleditor.api.HtmlEditorPluginService;
import org.nuxeo.runtime.api.Framework;

import com.sun.faces.renderkit.html_basic.HtmlBasicRenderer;


/**
 * @author david
 *
 */
public class OttcHTMLEditorRenderer extends HtmlEditorRenderer {

    private static Map<String, String> pluginsOptions;
    private static Map<String, String> toolbarPluginsOptions;

    /**
     * Constructor.
     */
    public OttcHTMLEditorRenderer() {
        super();
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        rendererParamsNotNull(context, component);
    }

    /**
     * We must implement this method to be able to use DocumentContentConverter during validation phase.
     * Furthermore, the static <code>HtmlEditorRenderer.getCurrentValue()</code> method disable default validate behavior,
     * so we have to rewrite textarea with currentValue, i.e. converted value (cf {@link HtmlBasicRenderer#getCurrentValue(FacesContext,
     * UIComponent)} method).
     * The static behavior of <code>HtmlEditorRenderer.getCurrentValue()</code> implies to rewrite all code ...
     */
    @Override
    protected void getEndTextToRender(FacesContext context, UIComponent component, String currentValue) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        UIHtmlEditor editorComp = (UIHtmlEditor) component;
        ResponseWriter writer = context.getResponseWriter();
        Locale locale = context.getViewRoot().getLocale();

        if (pluginsOptions == null) {
            final HtmlEditorPluginService pluginService = Framework.getLocalService(HtmlEditorPluginService.class);
            pluginsOptions = new HashMap<String, String>();
            pluginsOptions.put("plugins", pluginService.getFormattedPluginsNames());
        }
        if (toolbarPluginsOptions == null) {
            final HtmlEditorPluginService pluginService = Framework.getLocalService(HtmlEditorPluginService.class);
            toolbarPluginsOptions = new HashMap<String, String>();
            toolbarPluginsOptions.put("toolbar", pluginService.getFormattedToolbarsButtonsNames());
        }

        String clientId = editorComp.getClientId(context);
        boolean disableHtmlInit = Boolean.TRUE.equals(editorComp.getDisableHtmlInit());

        // input text area
        writer.startElement("textarea", editorComp);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("name", clientId, null);
        String editorSelector = editorComp.getEditorSelector();
        if (Boolean.TRUE.equals(editorComp.getDisableHtmlInit())) {
            writer.writeAttribute("class", editorSelector + ",disableMCEInit", null);
        } else {
            writer.writeAttribute("class", editorSelector, null);
        }
        writer.writeAttribute("rows", editorComp.getRows(), null);
        writer.writeAttribute("cols", editorComp.getCols(), null);
        // We do not use of HtmlEditorRenderer.getCurrentValue() here
        if (currentValue != null) {
            writer.writeText(currentValue, null);
        } else {
            writer.writeText("", null);
        }
        writer.endElement("textarea");

        if (!disableHtmlInit) {
            writer.startElement("script", editorComp);
            writer.writeAttribute("type", "text/javascript", null);
            String compConfiguration = editorComp.getConfiguration();
            if (StringUtils.isBlank(compConfiguration)) {
                compConfiguration = "{}";
            }
            // Since 5.7.3, use unique clientId instead of editorSelector value
            // so that tiny mce editors are initialized individually: no need
            // anymore to specify a class to know which one should or should
            // not be initialized
            String scriptContent = new StringBuilder().append("initTinyMCE(").append(editorComp.getWidth()).append(", ").append(editorComp.getHeight())
                    .append(", '").append(clientId).append("', '").append(pluginsOptions.get("plugins")).append("', '").append(locale.getLanguage())
                    .append("', '").append(toolbarPluginsOptions.get("toolbar")).append("', '").append(compConfiguration).append("');").toString();
            writer.writeText(scriptContent, null);
            String ajaxScriptContent = "jsf.ajax.addOnEvent(function(data) {if (data.status == \"success\") {" + scriptContent + "}});";
            writer.writeText(ajaxScriptContent, null);
            String scriptContent2 = "jQuery(document.getElementById('" + clientId
                    + "')).closest('form').bind('ajaxsubmit', function() { var editor = tinyMCE.editors['" + clientId
                    + "']; if (editor != undefined) {editor.save()};});";
            writer.writeText(scriptContent2, null);
            writer.endElement("script");
        }

        writer.flush();
    }

}
