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
 *   mberhaut1
 *   
 * Add the capability to use the WebID permalink within the REST API requests (GET POST PUT DELETE).
 * Path syntax is:
 *    <host>/nuxeo/api/toutatice/v1{/repo/default}/web/<webId>
 *    
 * A noter: le bug identifié par le ticket JIRA https://jira.nuxeo.com/browse/NXP-17701 conduit à un flux JSON contenant une erreur
 *    en réponse à une requête de type DELETE. Sera corrigé avec le HF-25.
 */
package fr.toutatice.ecm.platform.web.restapi.server.jaxrs;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.server.jaxrs.RestOperationException;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;
import org.nuxeo.ecm.platform.web.common.exceptionhandling.ExceptionHelper;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.exceptions.WebResourceNotFoundException;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;

@Path("/api/toutatice/v1{repo : (/repo/[^/]+?)?}")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "ToutaticeAPIRoot")
public class ToutaticeAPIRoot extends ModuleRoot {

    @Path("/")
    public Object doGetRepository(@PathParam("repo") String repositoryParam) throws NoSuchDocumentException {
        if (StringUtils.isNotBlank(repositoryParam)) {
            String repoName = repositoryParam.substring("repo/".length() + 1);
            try {
                ctx.setRepositoryName(repoName);
            } catch (final NuxeoException e) {
                throw new WebResourceNotFoundException(e.getMessage());
            }

        }
        return newObject("toutatice");
    }

    @Path("/automation")
    public Object getAutomationEndPoint() throws Exception {
        return newObject("automation");
    }
    
    @Override
    public Object handleError(final WebApplicationException cause) {
        Throwable unWrapException = ExceptionHelper.unwrapException(cause);
        if (unWrapException instanceof RestOperationException) {
            int customHttpStatus = ((RestOperationException) unWrapException).getStatus();
            return WebException.newException(cause.getMessage(), cause, customHttpStatus);
        }
        return WebException.newException(cause.getMessage(), unWrapException);
    }

}
