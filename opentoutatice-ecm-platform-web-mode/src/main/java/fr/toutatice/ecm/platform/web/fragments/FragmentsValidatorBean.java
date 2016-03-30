/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * 
 * Contributors:
 * mberhaut1
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.web.fragments;

import java.io.Serializable;
import java.net.MalformedURLException;
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
 * @author David Chevrier.
 *
 */
@Name("fgtsValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class FragmentsValidatorBean implements Serializable {

    private static final long serialVersionUID = -3623228467660612275L;
    
    /** Simple Web URL. */
    private static final Pattern SIMPLE_WEB_URL = Pattern.compile("(http|https)://(.*)");
    
    public void validateExternelURLField(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String url = (String) value;
        
        if(StringUtils.isNotBlank(url) && !StringUtils.contains(url, "/nuxeo/")){
                Matcher urlMatcher = SIMPLE_WEB_URL.matcher(url);
                if(!urlMatcher.matches()){
                    String msg = ComponentUtils.translate(context, "osivia.fgts.validator.malformed.url");
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                    throw new ValidatorException(message);
                }
        }
    }

}
