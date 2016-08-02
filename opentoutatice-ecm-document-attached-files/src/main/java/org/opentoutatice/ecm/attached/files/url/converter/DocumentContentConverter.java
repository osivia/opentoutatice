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
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.opentoutatice.ecm.attached.files.url.codec.OttcAttachedFileCodec;
import org.opentoutatice.ecm.attached.images.bean.OttcDocumentActionsBean;


/**
 * @author david
 *
 */
@FacesConverter("org.opentoutatice.ecm.attached.files.url.converter.DocumentContentConverter")
public class DocumentContentConverter implements Converter {

    private static final String INTERNAL_PICTURE_INDICATOR = "attachedImages";

    /** NX internal picture pattern. */
    static final Pattern PATTERN_NX_INTERNAL_PICTURE = Pattern.compile("(src=\"nxfile/default/)([a-zA-Z0-9[-]&&[^/]]*)(/ttc:images/[.[^\"]]*\")");
    static final Pattern PATTERN_NX_INTERNAL_PICTURE_PARAM = Pattern.compile("([?]conversationId=[.[^\"]]+)");

    /** Ottc internal picture pattern. */
    static final Pattern PATTERN_INTERNAL_PICTURE = Pattern.compile("(src=\"nxfile/default/)(".concat(INTERNAL_PICTURE_INDICATOR).concat(")").concat("(/ttc:images/[.[^\"]]*\")"));

    /**
     * Default constructor.
     */
    public DocumentContentConverter() {
        super();
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            Matcher matcher = PATTERN_NX_INTERNAL_PICTURE.matcher((String) value);
                StringBuffer replacementStack = new StringBuffer();
                while (matcher.find()) {
                    String g3 = matcher.group(3);
                    Matcher paramMatcher = PATTERN_NX_INTERNAL_PICTURE_PARAM.matcher(g3);
                    if(paramMatcher.find()){
                        g3 = StringUtils.remove(g3, paramMatcher.group(1));
                    }
                    matcher.appendReplacement(replacementStack, "$1" + INTERNAL_PICTURE_INDICATOR + g3);
                }
                replacementStack = matcher.appendTail(replacementStack);
                // Set (and not just 'return') to resolve variable pointer
                value = replacementStack.toString();
                return value;
                
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            Matcher matcher = PATTERN_INTERNAL_PICTURE.matcher((String) value);
            
            OttcDocumentActionsBean actionsBean = (OttcDocumentActionsBean) SeamComponentCallHelper.getSeamComponentByName("documentActions");
            DocumentModel currentDoc = actionsBean.getCurrentDocument();
            
            if(currentDoc != null){
                StringBuffer replacementStack = new StringBuffer();
                while (matcher.find()) {
                    //For creation mode
                    String docId = OttcAttachedFileCodec.CREATING_DOC_INDICATOR;
                    if(currentDoc.getId() != null){
                        docId = currentDoc.getId();
                    } 
                    
                    String g3 = matcher.group(3);
                    // Creation mode
                    if(currentDoc.getId() == null){
                        Manager conversationManager = (Manager) SeamComponentCallHelper.getSeamComponentByName("org.jboss.seam.core.manager");
                        String conversationId = "?".concat(conversationManager.getConversationIdParameter()).concat("=")
                                .concat(conversationManager.getCurrentConversationId());
                        g3 = StringUtils.remove(g3, "\"").concat(conversationId).concat("\"");
                    }
                    
                    matcher.appendReplacement(replacementStack, "$1" + docId + g3);
                }
                replacementStack = matcher.appendTail(replacementStack);
                // Set (and not just 'return') to resolve variable pointer
                value = replacementStack.toString();
                return (String) value;
            }
        }
        return StringUtils.EMPTY;
    }


}
