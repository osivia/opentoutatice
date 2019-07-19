/**
 * 
 */
package org.opentoutatice.core.recursive.copy.partial;

import java.util.Arrays;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @author david
 *
 */
@XObject("schemas")
public class ResetableSchemasDescriptor {
	
	@XNodeList(value = "schema", type = String[].class, componentType = String.class)
	protected String[] schemas;
	
	public List<String> getSchemasNames() {
		return this.schemas != null ? Arrays.asList(this.schemas) : null;
	}

}
