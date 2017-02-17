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
 * Converter used for html property fields of document, i.e set in tinyMCE.
 * It allows loss of document reference in its attached resources links.
 * So those links are preserved in case of copies and so on.
 * 
 * @author david
 *
 */
@FacesConverter("org.opentoutatice.ecm.attached.files.url.converter.DocumentContentConverter")
public class DocumentContentConverter implements Converter {

    /** Nuxeo webapp context. */
    private static final String webappName = "/".concat(BaseURL.getWebAppName()).concat("/");

    /** Absolute URL indicator. */
    private static final String protocolIndicator = ":";

    private static final String ATTACHED_RESOURCE_INDICATOR = "attachedImages";

    /** Component internal link pattern. */
    static final Pattern PATTERN_COMPONENT_LINK_OR_RESOURCE = Pattern.compile("(src|href)(=\")([.[^\"]]+\")");

    /** Component internal picture pattern. */
    static final Pattern PATTERN_COMPONENT_ATTACHED_RESOURCE = Pattern.compile("(nxfile/default/)([a-zA-Z0-9[-]&&[^/]]*)(/ttc:images/[.[^\"]]*\")");
    static final Pattern PATTERN_COMPONENT_ATTACHED_RESOURCE_PARAM = Pattern.compile("([?]conversationId=[.[^\"]]+)");

    /** Model internal picture pattern. */
    static final Pattern PATTERN_MODEL_ATTACHED_RESOURCE = Pattern.compile("(nxfile/default/)(".concat(ATTACHED_RESOURCE_INDICATOR).concat(")")
            .concat("(/ttc:images/[.[^\"]]*\")"));

    /**
     * Default constructor.
     */
    public DocumentContentConverter() {
        super();
    }

    /**
     * Called before document saving.
     * For relative resource link, replace document's uuid
     * by joker cause Portal knows current document.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        // Result
        StringBuffer replacement = new StringBuffer();

        if (value != null) {
            // Attached resource treatment
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

            // Add webapp name before all relative link (on portal side, the XSLFunction class
            // is coded with it)
            Matcher linkMatcher = PATTERN_COMPONENT_LINK_OR_RESOURCE.matcher(replacement.toString());
            StringBuffer linkReplacementStack = new StringBuffer();

            while (linkMatcher.find()) {
                String g3 = linkMatcher.group(3);
                String rp3 = g3;
                // Add webapp name if needed
                if (!isAbsoluteLink(g3) && !StringUtils.startsWith(g3, webappName)) {
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
     * Called before rendering by tinyMCE.
     * Replace attached resource indicator by current document uuid
     * to be render by Nuxeo.
     * 
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        // Result
        StringBuffer replacement = new StringBuffer();

        if (value != null) {
            // Attached resource treatment
            Matcher matcher = PATTERN_MODEL_ATTACHED_RESOURCE.matcher((String) value);

            // Get current document
            OttcDocumentActionsBean actionsBean = (OttcDocumentActionsBean) SeamComponentCallHelper.getSeamComponentByName("documentActions");
            DocumentModel currentDoc = actionsBean.getCurrentDocument();

            if (currentDoc != null) {
                StringBuffer pictReplacementStack = new StringBuffer();
                while (matcher.find()) {
                    // Document uuid
                    String docId = OttcAttachedFileCodec.CREATING_DOC_INDICATOR;
                    if (currentDoc.getId() != null) {
                        docId = currentDoc.getId();
                    }

                    String g3 = matcher.group(3);
                    // Creation mode
                    if (currentDoc.getId() == null) {
                        // Add Seam conversation
                        Manager conversationManager = (Manager) SeamComponentCallHelper.getSeamComponentByName("org.jboss.seam.core.manager");
                        String conversationId = "?".concat(conversationManager.getConversationIdParameter()).concat("=")
                                .concat(conversationManager.getCurrentConversationId());
                        g3 = StringUtils.remove(g3, "\"").concat(conversationId).concat("\"");
                    }

                    matcher.appendReplacement(pictReplacementStack, "$1" + docId + g3);
                }
                replacement = matcher.appendTail(pictReplacementStack);

                // All relative links are stored prefixed with /<Nx webapp name>/ (e.g '/nuxeo/')
                // but tinyMCE doesn't allow it.
                // So we must remove it before rendering.
                Matcher linkMatcher = PATTERN_COMPONENT_LINK_OR_RESOURCE.matcher(replacement.toString());
                StringBuffer linkReplacementStack = new StringBuffer();
                while (linkMatcher.find()) {
                    String rlg3 = linkMatcher.group(3);

                    // Remove webapp name if necessary
                    if (StringUtils.startsWith(rlg3, webappName)) {
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
    
    /**
     * Checks if link is absolute.
     * 
     * @param link
     * @return true if link is absolute
     */
    public static boolean isAbsoluteLink(String link){
        boolean absolute = false;
        
        if(StringUtils.contains(link, protocolIndicator)){
            String possibleProtocol = StringUtils.substringBefore(link, protocolIndicator);
            // It's protocol if it doesn't contain any special character, especially '/'
            return !StringUtils.contains(possibleProtocol, "/");
        }
        
        return absolute;
    }


}
