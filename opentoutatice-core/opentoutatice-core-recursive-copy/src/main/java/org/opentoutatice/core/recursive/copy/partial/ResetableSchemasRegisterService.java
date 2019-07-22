/**
 * 
 */
package org.opentoutatice.core.recursive.copy.partial;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.Extension;

/**
 * @author david
 *
 */
public class ResetableSchemasRegisterService extends DefaultComponent implements ResetableSchemasRegister {
	
	private static final Log log = LogFactory.getLog(ResetableSchemasRegisterService.class);

	private static final String RESETABLE_SCHEMAS_EXT_POINT = "resetableSchemas";

	private final Set<String> resetableSchemasNames;
	
	public ResetableSchemasRegisterService() {
		this.resetableSchemasNames = new HashSet<String>();
	}
	
	@Override
	public void registerExtension(Extension extension) throws Exception {
		Object[] contribs = extension.getContributions();
		if (contribs == null) {
			return;
		}
		for (Object contrib : contribs) {
			registerContribution(contrib, extension.getExtensionPoint(), extension.getComponent());
		}
		setModifiedNow();
	}

	@Override
	public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor)
			throws Exception {
		
		if(log.isDebugEnabled()) {
			log.debug("Trying register resetable schemas...");
		}
		
		if(RESETABLE_SCHEMAS_EXT_POINT.equals(extensionPoint)) {
			
			if(log.isDebugEnabled()) {
				log.debug("Registering resetable schemas...");
			}
			
			ResetableSchemasDescriptor resetableSchemasDescriptor = (ResetableSchemasDescriptor) contribution;
			this.resetableSchemasNames.addAll(resetableSchemasDescriptor.getSchemasNames());
			
			if(log.isDebugEnabled()) {
				StringBuffer schemsBuff = new StringBuffer();
				
				List<String> schemasNames = resetableSchemasDescriptor.getSchemasNames();
				if(schemasNames != null) {
					Iterator<String> iterator = schemasNames.iterator();
					while(iterator.hasNext()) {
						String schem = iterator.next();
						schemsBuff.append(schem);
						
						if(iterator.hasNext()) {
							schemsBuff.append(","); 
						}
					}
				}
				String schemNamesStr = StringUtils.isEmpty(schemsBuff.toString()) ? "null" : schemsBuff.toString();
				log.debug(String.format("Schema(s) [%s] registered", schemNamesStr));
			}
		}
	}

	@Override
	public Set<String> getResetableSchemasNames() {
		return this.resetableSchemasNames;
	}

}
