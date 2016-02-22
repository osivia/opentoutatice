package fr.toutatice.ecm.platform.web.restapi.server.jaxrs;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.nuxeo.ecm.restapi.server.jaxrs.JSONDocumentObject;
import org.nuxeo.ecm.webengine.model.WebObject;

@WebObject(type = "Document")
@Produces({ "application/json+nxentity", "application/json+esentity", MediaType.APPLICATION_JSON })
public class ToutaticeJSONDocumentObject extends JSONDocumentObject {

}
