package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

public class UserMngtHelper {
	private static final Log log = LogFactory.getLog(UserMngtHelper.class);

	private static UserMngtHelper instance;
	private static UserManager userManager;

	private UserMngtHelper() {
	}

	public static UserMngtHelper instance() {
		if (null == instance) {
			instance = new UserMngtHelper();
			getUserManager();
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
			propertyValue = (String) (userManager.getUserModel(principalName)).getPropertyValue(propertyName);
		} catch (Exception e) {
			log.error("Failed to get the user property '" + propertyName + "' from the user '" + principalName + "', error: " + e.getMessage());
		}

		return propertyValue;
	}	

	/**
	 * Initialize the service attribute
	 */
	 private static void getUserManager() {
		 try {
			 if (null == userManager) {
				 userManager = (UserManager) Framework.getService(UserManager.class);
			 }
		 } catch (Exception e) {
			 log.error("Failed to get the directory service, exception message: " + e.getMessage());
		 }
	 }
	 
	 
	public static List<String> getPublicUsers() throws ClientException {
			List<String> publicUsers = new ArrayList<String>();
			
			publicUsers = new ArrayList<String>();
			getUserManager();
			String anonymous = userManager.getAnonymousUserId();
			if (StringUtils.isNotBlank(anonymous)) {
				publicUsers.add(anonymous);
			}			
			publicUsers.add("members");
			
			return publicUsers;
	}

}
