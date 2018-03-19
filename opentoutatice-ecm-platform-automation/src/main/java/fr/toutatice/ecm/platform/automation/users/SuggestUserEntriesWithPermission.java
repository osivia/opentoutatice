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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.features.PrincipalHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.PermissionProvider;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.QName;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.SizeLimitExceededException;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserAdapter;
import org.nuxeo.ecm.platform.usermanager.UserManager;


/**
 * @author David Chevrier.
 *
 */
@Operation(id = SuggestUserEntriesWithPermission.ID, category = Constants.CAT_SERVICES,
        label = "Get user/group suggestion having given permission on given document",
        description = "Get user/group suggestion having given permission on given document. This is returning a blob containing a serialized JSON array..",
        addToStudio = false)
public class SuggestUserEntriesWithPermission {

    public final static String ID = "UserGroup.SuggestUserEntriesWithPermission";

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
    public Blob run() throws NuxeoException {
        JSONArray result = new JSONArray();
        boolean isGroupRestriction = !StringUtils.isBlank(groupRestriction);
        boolean groupOnly = false;
        boolean userOnly = isGroupRestriction;
        CoreSession session = ctx.getCoreSession();

        if (!isGroupRestriction && searchType != null && !searchType.isEmpty()) {
            if (searchType.equals(Select2Common.USER_TYPE)) {
                userOnly = true;
            } else if (searchType.equals(Select2Common.GROUP_TYPE)) {
                groupOnly = true;
            }
        }
        try {
            DocumentModelList userList = null;
            DocumentModelList groupList = null;
            if (!groupOnly) {
                Schema schema = schemaManager.getSchema(userManager.getUserSchemaName());
                userList = userManager.searchUsers(prefix);
                Directory userDir = directoryService.getDirectory(userManager.getUserDirectoryName());
                for (DocumentModel user : userList) {
                    NuxeoPrincipal principal = userManager.getPrincipal(user.getTitle());
                    if (session.hasPermission(principal, document.getRef(), permission)) {
                        JSONObject obj = new JSONObject();
                        for (Field field : schema.getFields()) {
                            QName fieldName = field.getName();
                            String key = fieldName.getLocalName();
                            Serializable value = user.getPropertyValue(fieldName.getPrefixedName());
                            if (key.equals(userDir.getPasswordField())) {
                                continue;
                            }
                            obj.element(key, value);
                        }
                        String userId = user.getId();
                        obj.put(Select2Common.ID, userId);
                        obj.put(Select2Common.TYPE_KEY_NAME, Select2Common.USER_TYPE);
                        obj.put(Select2Common.PREFIXED_ID_KEY_NAME, NuxeoPrincipal.PREFIX + userId);
                        Select2Common.computeUserLabel(obj, firstLabelField, secondLabelField, thirdLabelField, hideFirstLabel, hideSecondLabel,
                                hideThirdLabel, displayEmailInSuggestion, userId);
                        Select2Common.computeUserGroupIcon(obj, hideIcon);
                        if (isGroupRestriction) {
                            // We need to load all data about the user particularly
                            // its
                            // groups.
                            user = userManager.getUserModel(userId);
                            UserAdapter userAdapter = user.getAdapter(UserAdapter.class);
                            List<String> groups = userAdapter.getGroups();
                            if (groups != null && groups.contains(groupRestriction)) {
                                result.add(obj);
                            }
                        } else {
                            result.add(obj);
                        }
                    }
                }
            }
            if (!userOnly) {

                Schema schema = schemaManager.getSchema(userManager.getGroupSchemaName());
                groupList = userManager.searchGroups(prefix);

                List<String> groupsForPermission = getGroupsForPermission(document, permission);

                for (DocumentModel group : groupList) {
                    
                    String groupName = (String) group.getPropertyValue("group:groupname");
                    
                    if (groupsForPermission.contains(groupName)) {

                        JSONObject obj = new JSONObject();
                        for (Field field : schema.getFields()) {
                            QName fieldName = field.getName();
                            String key = fieldName.getLocalName();
                            Serializable value = group.getPropertyValue(fieldName.getPrefixedName());
                            obj.element(key, value);
                        }
                        String groupId = group.getId();
                        obj.put(Select2Common.ID, groupId);
                        // If the group hasn't an label, let's put the groupid
                        Select2Common.computeGroupLabel(obj, groupId, userManager.getGroupLabelField(), hideFirstLabel);
                        obj.put(Select2Common.TYPE_KEY_NAME, Select2Common.GROUP_TYPE);
                        obj.put(Select2Common.PREFIXED_ID_KEY_NAME, NuxeoGroup.PREFIX + groupId);
                        Select2Common.computeUserGroupIcon(obj, hideIcon);
                        result.add(obj);

                    }
                }
            }

            // Limit size results.
            int userSize = userList != null ? userList.size() : 0;
            int groupSize = groupList != null ? groupList.size() : 0;
            int totalSize = userSize + groupSize;
            if (userSuggestionMaxSearchResults != null && userSuggestionMaxSearchResults > 0) {
                if (userSize > userSuggestionMaxSearchResults || groupSize > userSuggestionMaxSearchResults || totalSize > userSuggestionMaxSearchResults) {
                    throw new SizeLimitExceededException();
                }
            }

        } catch (SizeLimitExceededException e) {
            return searchOverflowMessage();
        }

        return new StringBlob(result.toString(), "application/json");
    }

    /**
     * @param document
     * @param permission
     * @return names of groups with given permission.
     */
    protected List<String> getGroupsForPermission(DocumentModel document, String permission) {
        List<String> groups = new ArrayList<String>();

        PrincipalHelper principalHelper = new PrincipalHelper(userManager, permissionProvider);
        String[] perms = principalHelper.getPermissionsToCheck(permission);

        ACP acp = document.getACP();
        for (ACL acl : acp.getACLs()) {
            for (ACE ace : acl.getACEs()) {
                if (ace.isGranted() && permissionMatch(perms, ace.getPermission())) {
                    NuxeoGroup group = userManager.getGroup(ace.getUsername());
                    if(group != null){
                        groups.add(group.getName());
                    }
                }
            }
        }

        return groups;
    }

    /**
     * @param perms
     * @param perm
     * @return true if perms contains perm.
     */
    public boolean permissionMatch(String[] perms, String perm) {
        for (String p : perms) {
            if (p.equals(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return searchOverflowMessage
     * @since 5.7.3
     */
    private Blob searchOverflowMessage() {
        JSONArray result = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put(Select2Common.LABEL, I18NUtils.getMessageString("messages", "label.security.searchOverFlow", new Object[0], getLocale()));
        result.add(obj);
        return new StringBlob(result.toString(), "application/json");
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
