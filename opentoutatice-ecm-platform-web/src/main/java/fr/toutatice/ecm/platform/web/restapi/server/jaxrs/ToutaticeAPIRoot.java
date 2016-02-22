package fr.toutatice.ecm.platform.web.restapi.server.jaxrs;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.server.jaxrs.RestOperationException;
import org.nuxeo.ecm.core.api.ClientException;
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
            } catch (final ClientException e) {
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
