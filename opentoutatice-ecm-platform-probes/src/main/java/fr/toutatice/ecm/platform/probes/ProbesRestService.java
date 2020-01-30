/**
 * 
 */
package fr.toutatice.ecm.platform.probes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;


/**
 * @author Loïc Billon
 *
 */
@Path("/probes")
@Produces("application/json")
@WebObject(type = "Probe")
public class ProbesRestService extends ModuleRoot {
	
    private static final Log log = LogFactory.getLog(ProbesRestService.class);
	
	
	@GET
	@Path("status")
	public Object getStatus() {
				
		try {
			CoreSession session = SessionFactory.getSession();
			session.query("SELECT * FROM Document", 1);
		}
		catch(Exception e) {
			
			log.error(e.getMessage());
			
			return "{\"error\":1}";
		}
		return "{\"error\":0}";
	}
	
	
}
