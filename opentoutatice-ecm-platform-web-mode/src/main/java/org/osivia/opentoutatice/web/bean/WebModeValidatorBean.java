/**
 * 
 */
package org.osivia.opentoutatice.web.bean;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;


/**
 * @author david
 *
 */
@Name("webModeValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class WebModeValidatorBean implements Serializable {
    
    private static final long serialVersionUID = 5623376301895535136L;
    
    final Pattern patternSegment = Pattern.compile("([a-zA-Z_0-9\\-]+)");
    
    public void validateSegment(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if(StringUtils.isNotBlank((String) value)){
            Matcher m = patternSegment.matcher((String) value);
            if (!m.matches()) {
                String msg = ComponentUtils.translate(context, "webmode.validator.malformed.segment");
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
        }
    }

}
