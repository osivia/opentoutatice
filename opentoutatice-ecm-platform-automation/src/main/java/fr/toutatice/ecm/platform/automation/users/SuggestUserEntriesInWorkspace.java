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
 * lbillon
 */
package fr.toutatice.ecm.platform.automation.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.features.PrincipalHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.PermissionProvider;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.directory.ldap.LDAPDirectory;
import org.nuxeo.ecm.directory.ldap.LDAPSession;
import org.nuxeo.ecm.platform.ui.select2.common.Select2Common;
import org.nuxeo.ecm.platform.usermanager.UserConfig;
import org.nuxeo.ecm.platform.usermanager.UserManager;

import com.ctc.wstx.util.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * @author Loïc Billon.
 *
 */
@Operation(id = SuggestUserEntriesInWorkspace.ID, category = Constants.CAT_SERVICES,
        label = "Get user/group suggestion having membership in workspace",
        description = "Get user/group suggestion having membership in workspace on given document. This is returning a blob containing a serialized JSON array..",
        addToStudio = false)
public class SuggestUserEntriesInWorkspace {

    public final static String ID = "UserGroup.SuggestUserEntriesInWorkspace";

    @Context
    protected OperationContext ctx;

    @Context
    protected PermissionProvider permissionProvider;

    @Context
    protected SchemaManager schemaManager;

    @Param(name = "searchTerm", alias = "prefix", required = false)
    protected String prefix;

    @Param(name = "documentId")
    protected DocumentModel document;

    @Param(name = "permission")
    protected String permission;

    @Param(name = "searchType", required = false)
    protected String searchType;

    @Param(name = "groupRestriction", required = false)
    protected String groupRestriction;

    @Param(name = "userSuggestionMaxSearchResults", required = false)
    protected Integer userSuggestionMaxSearchResults;

    @Param(name = "firstLabelField", required = false)
    protected String firstLabelField;

    @Param(name = "secondLabelField", required = false)
    protected String secondLabelField;

    @Param(name = "thirdLabelField", required = false)
    protected String thirdLabelField;

    @Param(name = "hideFirstLabel", required = false)
    protected boolean hideFirstLabel = false;

    @Param(name = "hideSecondLabel", required = false)
    protected boolean hideSecondLabel = false;

    @Param(name = "hideThirdLabel", required = false)
    protected boolean hideThirdLabel;

    @Param(name = "displayEmailInSuggestion", required = false)
    protected boolean displayEmailInSuggestion;

    @Param(name = "hideIcon", required = false)
    protected boolean hideIcon;

    @Context
    protected UserManager userManager;

    @Context
    protected DirectoryService directoryService;

    @Param(name = "lang", required = false)
    protected String lang;

    @OperationMethod
    public Blob run() throws ClientException {
    	
    	boolean userOnly = false;
    	
    	// if no search is typed, search only groups
    	boolean groupOnly = StringUtils.isEmpty(prefix);
    	
		if (searchType != null && !searchType.isEmpty()) {
			if (searchType.equals(Select2Common.USER_TYPE)) {
				userOnly = true;
			} else if (searchType.equals(Select2Common.GROUP_TYPE)) {
				groupOnly = true;
			}
		}
    	
        JSONArray result = new JSONArray();
    
        LDAPDirectory userDir = (LDAPDirectory) directoryService.getDirectory(userManager.getUserDirectoryName());
        LDAPDirectory groupDir = (LDAPDirectory) directoryService.getDirectory(userManager.getGroupDirectoryName());
        LDAPSession session = (LDAPSession) userDir.getSession();
        DirContext context = session.getContext();
        
        String workspaceId = getLdapWorkspaceId(document);
        String workspaceMemberDn = null;
        
        if(StringUtils.isNotBlank(workspaceId)) {
        	try {
				String searchBaseDn = groupDir.getConfig().getSearchBaseDn();
				String fieldId = groupDir.getFieldMapper().getBackendField("groupname");
				String fieldLabel = groupDir.getFieldMapper().getBackendField("grouplabel");
				String fieldDisplayname = groupDir.getFieldMapper().getBackendField("displayname");
				String fieldWorkspaceId = groupDir.getFieldMapper().getBackendField("workspaceId");
				String fieldRole = groupDir.getFieldMapper().getBackendField("role");
				String fieldType = groupDir.getFieldMapper().getBackendField("type");
				
				// search space_group name
				StringBuilder sbSpaceGroup = new StringBuilder();
				sbSpaceGroup.append("(&");
				sbSpaceGroup.append("(").append(fieldWorkspaceId).append("=").append(workspaceId).append(")");
				sbSpaceGroup.append("(").append(fieldType).append("=").append("space_group").append(")");
				sbSpaceGroup.append(")");
				
				NamingEnumeration<SearchResult> search = context.search(searchBaseDn, sbSpaceGroup.toString(), groupDir.getSearchControls());
				
				while(search.hasMoreElements()) {
					SearchResult next = search.next();
					workspaceMemberDn = next.getNameInNamespace();
				}
				
				StringBuilder sbAllGroup = new StringBuilder();
				sbAllGroup.append("(&");
				sbAllGroup.append("(").append(fieldWorkspaceId).append("=").append(workspaceId).append(")");
				if(StringUtils.isNotBlank(prefix)) {
					sbAllGroup.append("(").append(fieldLabel).append("=").append(prefix).append("*)");
				}
				sbAllGroup.append(")");
				
				
				search = context.search(searchBaseDn, sbAllGroup.toString(), groupDir.getSearchControls());
				
				while(search.hasMoreElements()) {
					SearchResult next = search.next();
					
					Attribute attrType = next.getAttributes().get(fieldType);
					Attribute attrRole = next.getAttributes().get(fieldRole);
					String label = "";
					
					if(attrType.get(0).equals("space_group")) {
						workspaceMemberDn = next.getNameInNamespace();
						label = I18NUtils.getMessageString("messages", "label.search.allmembers", null, getLocale());
					}
					else if (attrType.get(0).equals("security_group")) {
						if(attrRole.get(0).equals("READER")) { 
							label = I18NUtils.getMessageString("messages", "label.search.readers", null, getLocale());
						}
						else if(attrRole.get(0).equals("CONTRIBUTOR")) {
							label = I18NUtils.getMessageString("messages", "label.search.contributors", null, getLocale());
						}
						else if(attrRole.get(0).equals("WRITER")) {
							label = I18NUtils.getMessageString("messages", "label.search.writers", null, getLocale());
						}
						else if(attrRole.get(0).equals("ADMIN")) {
							label = I18NUtils.getMessageString("messages", "label.search.admins", null, getLocale());
						}
						else if(attrRole.get(0).equals("OWNER")) {
							label = I18NUtils.getMessageString("messages", "label.search.owners", null, getLocale());
						}
					}
					else { // local groups
						
						Attribute displayname = next.getAttributes().get(fieldDisplayname);
						
						if(displayname != null) {
							label = displayname.get(0).toString();	
						}
						else {
							label = next.getAttributes().get(fieldLabel).get(0).toString();;
						}
						
					}
					
					if(!userOnly) {
						JSONObject obj = new JSONObject();
	
						String groupId = next.getAttributes().get(fieldId).get(0).toString();
						
						obj.put(Select2Common.ID, groupId);
						obj.put(Select2Common.LABEL, label);
						// If the group hasn't an label, let's put the groupid
						//Select2Common.computeGroupLabel(obj, groupId, userManager.getGroupLabelField(), hideFirstLabel);
						obj.put(Select2Common.TYPE_KEY_NAME, Select2Common.GROUP_TYPE);
						obj.put(Select2Common.PREFIXED_ID_KEY_NAME, NuxeoGroup.PREFIX + groupId);
						Select2Common.computeUserGroupIcon(obj, hideIcon);
						result.add(obj);
					}

				}
			} catch (NamingException e) {
				throw new ClientException(e);
			}
        }
        
        if(!groupOnly) {
	        try {
	        	String searchBaseDn = userDir.getConfig().getSearchBaseDn();
				String fieldMemberOf = userDir.getFieldMapper().getBackendField("groups");
				
				String fieldId = userDir.getFieldMapper().getBackendField("username");
				String fieldFirstName = userDir.getFieldMapper().getBackendField("firstName");
				String fieldLastName = userDir.getFieldMapper().getBackendField("lastName");
				String fieldMail = userDir.getFieldMapper().getBackendField("email");
				
				StringBuilder subSb = new StringBuilder();
				subSb.append("(|");
				subSb.append("(").append(fieldFirstName).append("=").append(prefix).append("*)");
				subSb.append("(").append(fieldLastName).append("=").append(prefix).append("*)");
				subSb.append("(").append(fieldMail).append("=").append(prefix).append("*)");
				subSb.append(")");
	
				StringBuilder sb = new StringBuilder();
				sb.append("(&");
				sb.append(subSb.toString());
				if(workspaceMemberDn != null) {
					sb.append("(").append(fieldMemberOf).append("=").append(workspaceMemberDn).append("*)");
				}
				sb.append(")");
				
				NamingEnumeration<SearchResult> search = context.search(searchBaseDn, sb.toString(), userDir.getSearchControls());
				
				while (search.hasMoreElements()) {
					SearchResult next = search.next();
	
					JSONObject obj = new JSONObject();
	
					String userId = next.getAttributes().get(fieldId).get(0).toString();
					obj.put(Select2Common.ID, userId);
					obj.put(Select2Common.TYPE_KEY_NAME, Select2Common.USER_TYPE);
					obj.put(Select2Common.PREFIXED_ID_KEY_NAME, NuxeoPrincipal.PREFIX + userId);
					Attribute firstName = next.getAttributes().get(fieldFirstName);
					if(firstName != null) {
						obj.put(UserConfig.FIRSTNAME_COLUMN, firstName.get(0));
					}
					
					Attribute lastName = next.getAttributes().get(fieldLastName);
					if(lastName != null) {
						obj.put(UserConfig.LASTNAME_COLUMN, lastName.get(0));
					}
					
					Attribute mail = next.getAttributes().get(fieldMail);
					if(mail != null) {
						obj.put(UserConfig.EMAIL_COLUMN, mail.get(0));
					}
					else {
						obj.put(UserConfig.EMAIL_COLUMN, "inconnu");
					}
					
					
					Select2Common.computeUserLabel(obj, firstLabelField, secondLabelField, thirdLabelField, hideFirstLabel,
							hideSecondLabel, hideThirdLabel, displayEmailInSuggestion, userId);
					Select2Common.computeUserGroupIcon(obj, hideIcon);
					result.add(obj);
				}
				
				
			} catch (NamingException e) {
				throw new ClientException(e);
			}
	        
        }
        
        return new StringBlob(result.toString(), "application/json");
    }

    private String getLdapWorkspaceId(DocumentModel document) {
    	
        // current doc may not exist and can not be found via session
        Path documentPath = document.getPath(); 
        
        while(documentPath != null) {
        	
            DocumentModel parent = ctx.getCoreSession().getDocument(new PathRef(documentPath.toString()));

        	if(parent.hasSchema("webcontainer") && parent.getType().equals("Workspace")) {
        		return parent.getPropertyValue("webc:url").toString();
        	}       	
        	documentPath = parent.getPath().removeLastSegments(1);
        }

		return null;
	}


    protected String getLang() {
        if (lang == null) {
            lang = (String) ctx.get("lang");
            if (lang == null) {
                lang = Select2Common.DEFAULT_LANG;
            }
        }
        return lang;
    }

    protected Locale getLocale() {
        return new Locale(getLang());
    }
}
