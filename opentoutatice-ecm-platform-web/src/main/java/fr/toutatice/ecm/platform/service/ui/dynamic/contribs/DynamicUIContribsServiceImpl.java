/**
 * 
 */
package fr.toutatice.ecm.platform.service.ui.dynamic.contribs;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.types.TypeService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.impl.ComponentInstanceImpl;
import org.nuxeo.runtime.model.impl.RegistrationInfoImpl;

/**
 * @author david
 * 
 */
public class DynamicUIContribsServiceImpl extends DefaultComponent implements DynamicUIContribsService {

	private static final long serialVersionUID = -3335398967270359400L;
	private static final Log log = LogFactory
			.getLog(DynamicUIContribsServiceImpl.class);

	/** Constantes de contribution */
	private static final String BASE_TYPE = "Document";
	private static final String TYPES_CONTRIBUTION_POINT = "types";

	/** Contribution de référence */
	private static String REF_UI_CONTRIB = "fr.toutatice.ecm.platform.web.append.ui.types";
	
	@Override
	public void activate(ComponentContext context) throws Exception {
		super.activate(context);
		addUIContribs();
	}
	
	/**
	 * Ajout dynamique de widgets.
	 */
	@Override
	public void addUIContribs() throws Exception {
		TypeManager typeManager = Framework.getService(TypeManager.class);
		Collection<Type> types = typeManager.getTypes();
		for (Type type : types) {
			String docType = type.getId();
			if (!BASE_TYPE.equals(docType)) {
				Type baseType = typeManager.getType(BASE_TYPE);
				if (baseType != null) {
					baseType.setId(docType);
					if (typeManager != null) {
						ComponentName cn = new ComponentName(REF_UI_CONTRIB);
						RegistrationInfoImpl ri = new RegistrationInfoImpl(cn);
						ComponentInstance component = new ComponentInstanceImpl(
								ri);
						((TypeService) typeManager).registerContribution(baseType,
								TYPES_CONTRIBUTION_POINT, component);
					}
				}
			}
		}
	}

}
