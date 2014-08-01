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
 * lbillon
 */
package fr.toutatice.ecm.platform.web.urlservice;

import static org.jboss.seam.ScopeType.EVENT;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.picture.web.PictureManager;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.codec.DocumentFileCodec;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.service.url.WebIdCodec;

@Name("restHelper")
@Scope(EVENT)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeRestHelper extends RestHelper {
	
    private static final long serialVersionUID = 8463715204469011050L;

    private static final Log log = LogFactory.getLog(ToutaticeRestHelper.class);
    
    @In(create = true)
    PictureManager pictureManager;

    /**
     * Switch action in case of type of the document associated by the url
     * 
     * @param docView
     * @return a view
     * @throws ClientException
     */
    public String switchBehaviour(DocumentView docView) throws ClientException {

        String content = docView.getParameter(WebIdCodec.CONTENT_PARAM);

        // for picture, download the picture
        if (StringUtils.isNotBlank(content)) {
            pictureManager.download(docView);
        }
        // by default, call a nuxeo view
        else {
            initContextFromRestRequest(docView);
        }

        return "";
    }    
}
