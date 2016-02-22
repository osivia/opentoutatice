package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;

public class ToutaticeOwnerSecurityPolicy extends AbstractSecurityPolicy {

	private static final Log log = LogFactory.getLog(ToutaticeOwnerSecurityPolicy.class);

	@Override
	public Access checkPermission(Document doc, 
			ACP mergedAcp,
			Principal principal, 
			String permission,
			String[] resolvedPermissions, 
			String[] additionalPrincipals) {
		Access access = Access.UNKNOWN;

		try {
			if (doPolicyApply(mergedAcp, principal, permission, additionalPrincipals)) {
				String creator = (String) doc.getPropertyValue("dc:creator");
				if (!principal.getName().equals(creator)) {
					access = Access.DENY;
				}
			}
		} catch (DocumentException e) {
			log.error("Failed to evaluate the policy, error: " + e.getMessage());
		}

		return access;
	}

	private boolean doPolicyApply(ACP mergedAcp, Principal principal, String permission, String[] additionalPrincipals) {
		boolean status = false;

		if (SecurityConstants.WRITE.equals(permission) || SecurityConstants.REMOVE.equals(permission)) {
			List<String> principalsList = new ArrayList<String>();
			if (null != additionalPrincipals) {
				principalsList.addAll(Arrays.asList(additionalPrincipals));
			} else {
				principalsList.add(principal.getName());
			}
			
			Access acessWMOO = mergedAcp.getAccess(principalsList.toArray(new String[principalsList.size()]), new String[] {"WriteModifyOwnOnly"});
			Access acessWrite = mergedAcp.getAccess(principalsList.toArray(new String[principalsList.size()]), new String[] {SecurityConstants.READ_WRITE});
			if (Access.GRANT == acessWMOO && Access.GRANT != acessWrite) {
				status = true;
			}
		}

		return status;
	}

	@Override
	public boolean isRestrictingPermission(String permission) {
		assert permission.equals("Browse"); // others not coded
		return false;
	}

	@Override
	public boolean isExpressibleInQuery() {
		return true;
	}

	@Override
	public SQLQuery.Transformer getQueryTransformer() {
		return SQLQuery.Transformer.IDENTITY;
	}

}
