/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.core.Manager;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.opentoutatice.ecm.attached.files.url.codec.OttcAttachedFileCodec;
import org.opentoutatice.ecm.attached.images.bean.OttcDocumentActionsBean;


/**
 * @author david
 *
 */
@FacesConverter("org.opentoutatice.ecm.attached.files.url.converter.DocumentContentConverter")
public class DocumentContentConverter implements Converter {
    
    private static final String webappName = "/".concat(BaseURL.getWebAppName()).concat("/");
    
    private static final String ATTACHED_RESOURCE_INDICATOR = "attachedImages";
    
    /** Component internal link pattern. */
    static final Pattern PATTERN_COMPONENT_LINK_OR_RESOURCE = Pattern.compile("(src|href)(=\")([.[^\"]]+\")");

    /** Component internal picture pattern. */
    static final Pattern PATTERN_COMPONENT_ATTACHED_RESOURCE = Pattern.compile("(nxfile/default/)([a-zA-Z0-9[-]&&[^/]]*)(/ttc:images/[.[^\"]]*\")");
    static final Pattern PATTERN_COMPONENT_ATTACHED_RESOURCE_PARAM = Pattern.compile("([?]conversationId=[.[^\"]]+)");

    /** Model internal picture pattern. */
    static final Pattern PATTERN_MODEL_ATTACHED_RESOURCE = Pattern.compile("(nxfile/default/)(".concat(ATTACHED_RESOURCE_INDICATOR).concat(")").concat("(/ttc:images/[.[^\"]]*\")"));
    
    /**
     * Default constructor.
     */
    public DocumentContentConverter() {
        super();
    }
    
    /**
     * Returned value is value which will be saved - model's value
     * (input value is component's value).
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        StringBuffer replacement = new StringBuffer();
        if (value != null) {
            Matcher pictMatcher = PATTERN_COMPONENT_ATTACHED_RESOURCE.matcher((String) value);
            StringBuffer pictReplacementStack = new StringBuffer();
            while (pictMatcher.find()) {
                String g3 = pictMatcher.group(3);
                Matcher paramMatcher = PATTERN_COMPONENT_ATTACHED_RESOURCE_PARAM.matcher(g3);
                if (paramMatcher.find()) {
                    g3 = StringUtils.remove(g3, paramMatcher.group(1));
                }
                pictMatcher.appendReplacement(pictReplacementStack, "$1" + ATTACHED_RESOURCE_INDICATOR + g3);
            }
            replacement = pictMatcher.appendTail(pictReplacementStack);
            
            Matcher linkMatcher = PATTERN_COMPONENT_LINK_OR_RESOURCE.matcher(replacement.toString());
            StringBuffer linkReplacementStack = new StringBuffer();
            while(linkMatcher.find()){
                // We store URL links prefixed with webapp's name
                String g3 = linkMatcher.group(3);
                String rp3 = g3;
                if (!StringUtils.startsWith(g3, webappName)) {
                    rp3 = webappName.concat(g3);
                }
                
                linkMatcher.appendReplacement(linkReplacementStack, "$1" + "$2" + rp3);
            }
            replacement = linkMatcher.appendTail(linkReplacementStack);
            
            // Set (and not just 'return') to resolve variable pointer
            value = replacement.toString();
            return value;

        }
        return replacement.toString();
    }
    
    /**
     * Returned value is value which will be shown in UI - component's value
     * (input value is model's value).
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        StringBuffer replacement = new StringBuffer();
        if (value != null) {
            Matcher matcher = PATTERN_MODEL_ATTACHED_RESOURCE.matcher((String) value);

            OttcDocumentActionsBean actionsBean = (OttcDocumentActionsBean) SeamComponentCallHelper.getSeamComponentByName("documentActions");
            DocumentModel currentDoc = actionsBean.getCurrentDocument();

            if (currentDoc != null) {
                StringBuffer pictReplacementStack = new StringBuffer();
                while (matcher.find()) {
                    // For creation mode
                    String docId = OttcAttachedFileCodec.CREATING_DOC_INDICATOR;
                    if (currentDoc.getId() != null) {
                        docId = currentDoc.getId();
                    }

                    String g3 = matcher.group(3);
                    // Creation mode
                    if (currentDoc.getId() == null) {
                        Manager conversationManager = (Manager) SeamComponentCallHelper.getSeamComponentByName("org.jboss.seam.core.manager");
                        String conversationId = "?".concat(conversationManager.getConversationIdParameter()).concat("=")
                                .concat(conversationManager.getCurrentConversationId());
                        g3 = StringUtils.remove(g3, "\"").concat(conversationId).concat("\"");
                    }

                    matcher.appendReplacement(pictReplacementStack, "$1" + docId + g3);
                }
                replacement = matcher.appendTail(pictReplacementStack);
                
                Matcher linkMatcher = PATTERN_COMPONENT_LINK_OR_RESOURCE.matcher(replacement.toString());
                StringBuffer linkReplacementStack = new StringBuffer();
                while(linkMatcher.find()){
                 // We set URL without webapp name
                    String lg3 = linkMatcher.group(3);
                    String rlg3 = lg3;
                    if(StringUtils.startsWith(rlg3, webappName)){
                        rlg3 = StringUtils.remove(rlg3, webappName);
                    }
                    
                    linkMatcher.appendReplacement(linkReplacementStack, "$1" + "$2" + rlg3);
                }
                replacement = linkMatcher.appendTail(linkReplacementStack);
                
                // Set (and not just 'return') to resolve variable pointer
                value = replacement.toString();
                return (String) value;
            }
        }
        return replacement.toString();
    }


}
