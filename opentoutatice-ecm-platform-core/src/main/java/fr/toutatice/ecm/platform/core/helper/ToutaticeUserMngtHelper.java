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
 */
package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.features.PrincipalHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.PermissionProvider;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

public class ToutaticeUserMngtHelper {
	private static final Log log = LogFactory.getLog(ToutaticeUserMngtHelper.class);

	private static ToutaticeUserMngtHelper instance;
	private static UserManager userManager;
	private static PermissionProvider permissionProvider;

	private ToutaticeUserMngtHelper() {
	}

	public static ToutaticeUserMngtHelper instance() {
		if (null == instance) {
			instance = new ToutaticeUserMngtHelper();
		}
		return instance;
	}

	public String getCurrentUserFullName(String principalName) {
		String fullName = principalName;

		String firstName = getUserProperty(principalName, "user:firstName");
		String lastName = getUserProperty(principalName, "user:lastName");

		if (null != firstName && null != lastName) {
			fullName = firstName + " " + lastName;
		}

		return StringUtils.isNotBlank(fullName) ? fullName : principalName;
	}

	public String getUserProperty(String principalName, String propertyName) {
		String propertyValue = "";

		try {
			propertyValue = (String) (getUserManager().getUserModel(principalName)).getPropertyValue(propertyName);
		} catch (Exception e) {
			log.error("Failed to get the user property '" + propertyName + "' from the user '" + principalName + "', error: " + e.getMessage());
		}

		return propertyValue;
	}	

	public List<String> getPublicUsers() throws ClientException {
			List<String> publicUsers = new ArrayList<String>();
			
			publicUsers = new ArrayList<String>();
			
			// add anonymous user (as configured in template)
			String anonymous = getUserManager().getAnonymousUserId();
			if (StringUtils.isNotBlank(anonymous)) {
				publicUsers.add(anonymous);
			}
			
			// add members (any connected user) (as configured in template)
			String members = getUserManager().getDefaultGroup();
			if (StringUtils.isNotBlank(members)) {
				publicUsers.add(members);
			}
			
			return publicUsers;
	}
	
	public List<String> getSuperAdministrators() throws ClientException {
		return getUserManager().getAdministratorsGroups();
	}
	
	/**
	 * 
	 * @param doc document	
	 * @param permission permission à contrôler
	 * @param ignoreGroups si true, alors les groupes sont exclus du resultat
	 * @param resolveGroups si true, alors les utilisateurs du groupe sont ajoutés au résultat
	 * @return
	 * @throws Exception
	 */
	 public Set<String> getDocumentUsersAndGroups(DocumentModel doc,String permission,boolean ignoreGroups, boolean resolveGroups) 
			 throws ClientException {
		 PrincipalHelper ph = new PrincipalHelper(getUserManager(), getPermissionProvider());
		 Set<String> result = ph.getUserAndGroupIdsForPermission(doc, permission, ignoreGroups, resolveGroups, false);
		 
		 return result;
	 }

	/**
	 * Initialize the service attribute
	 */
	 private static UserManager getUserManager() throws ClientException {
		 try {
			 if (null == userManager) {
				 userManager = (UserManager) Framework.getService(UserManager.class);
			 }
		 } catch (Exception e) {
			 log.error("Failed to get the user manager service, exception message: " + e.getMessage());
			 throw new ClientException("Failed to get the user manager service, exception message: " + e.getMessage());
		 }
		 
		 return userManager;
	 }
	 
	
		 private static PermissionProvider getPermissionProvider() throws ClientException {
			 try {
				 if (null == permissionProvider) {
					 permissionProvider = (PermissionProvider) Framework.getService(PermissionProvider.class);
				 }
			 } catch (Exception e) {
				 log.error("Failed to get the user manager service, exception message: " + e.getMessage());
				 throw new ClientException("Failed to get the user manager service, exception message: " + e.getMessage());
			 }
			 
			 return permissionProvider;
		 }
	 


}
