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
package fr.toutatice.ecm.platform.core.constants;

/**
 * This class groups together all definitions done within the NuxeoStudio tool and referenced in this package
 * Core Nuxeo definitions are also stored here
 * 
 * @author mberhaut1
 */
public class ToutaticeNuxeoStudioConst {
	// document types
    public final static String CST_DOC_TYPE_PORTALSITE = "PortalSite";
    public final static String CST_DOC_TYPE_PORTALPAGE = "PortalPage";
	public final static String CST_DOC_TYPE_MAINTENANCE = "MaintenanceConfiguration";
	public final static String CST_DOC_TYPE_WORKSPACE = "Workspace";
	public final static String CST_DOC_TYPE_USER_WORKSPACE = "UserWorkspace";
	public final static String CST_DOC_TYPE_PICTURE_BOOK = "PictureBook";
	public final static String CST_DOC_TYPE_PICTURE = "Picture";
	public final static String CST_DOC_TYPE_SECTION = "Section";
	public final static String CST_DOC_TYPE_DOMAIN = "Domain";
	public final static String CST_DOC_TYPE_ROOT = "Root";
	public final static String CST_DOC_TYPE_WEB_CONFIGURATION = "WebConfiguration";

	// schemas
	public final static String CST_DOC_SCHEMA_NUXEO_WEBCONTAINER = "webcontainer";
	public final static String CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_PREFIX = "webc";
	public final static String CST_DOC_SCHEMA_NUXEO_FILES = "files";
	public final static String CST_DOC_SCHEMA_NUXEO_FILES_PREFIX = "files";
	public final static String CST_DOC_SCHEMA_USER_PREFIX = "user";
	public final static String CST_DOC_SCHEMA_DC_PREFIX = "dc";
	public final static String CST_DOC_SCHEMA_DUBLINCORE = "dublincore";
	public final static String CST_DOC_SCHEMA_TOUTATICE = "toutatice";
	public final static String CST_DOC_SCHEMA_TOUTATICE_PREFIX = "ttc";
    public final static String CST_DOC_SCHEMA_TOUTATICE_SPACE_PREFIX = "ttcs";
	public final static String CST_DOC_SCHEMA_TOUTATICE_SPACEID = "ttc:spaceID";
	public final static String CST_DOC_SCHEMA_PUBLISHING = "publishing";
	public final static String CST_DOC_SCHEMA_NUXEO_PUBLISH_PREFIX = "publish";
	public static final String CST_DOC_SCHEMA_NUXEO_MAIL_PREFIX = "mail";
	public static final String CST_DOC_SCHEMA_PICTURE_BOOK_PREFIX = "picturebook";
	public static final String CST_DOC_SCHEMA_MNT_PREFIX = "maintenance";
	public static final String CST_DOC_SCHEMA_WEB_CONF = "wconf";
	public static final String CST_DOC_REMOTE_SECTIONS = "rsi";
	public static final String CST_DOC_REMOTE_SECTIONS_SCHEMA = "remoteSectionsInfos";
	
	public static final String CST_DOC_SCHEMA_TTC_EVENT = "toutatice_event";
	public static final String CST_DOC_SCHEMA_TTC_EVENT_PREFIX = "ttcevt";
	
	// facets
	public final static String CST_DOC_FACET_WEB_VIEW = "WebView";
	public final static String CST_DOC_FACET_TTC_PUBLISH_SPACE = "TTCPublishSpace";
	public static final String CST_FACET_SPACE = "Space";
	public static final String CST_FACET_SPACE_NAVIGATION_ITEM = "SpaceNavigationItem";
	public static final String CST_FACET_SPACE_CONTENT = "SpaceContent";
	public static final String CST_FACET_HAS_REMOTE_SECTIONS = "HasRemoteSections";


	// meta-data
	public final static String CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_EMAIL = "email";

	// meta-data xpath
	public final static String CST_DOC_XPATH_USER_EMAIL = CST_DOC_SCHEMA_USER_PREFIX + ":email";
	public final static String CST_DOC_XPATH_TOUTATICE_IMAGES = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":images";
	public final static String CST_DOC_XPATH_TOUTATICE_STAMP = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":vignette";
	public final static String CST_DOC_XPATH_TOUTATICE_SIM = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":showInMenu";
	public final static String CST_DOC_XPATH_TOUTATICE_DOMAIN_ID = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":domainID";
    public final static String CST_DOC_XPATH_TOUTATICE_EXPLICIT_URL = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":explicitUrl";
    public final static String CST_DOC_XPATH_TOUTATICE_EXTENSION_URL = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":extensionUrl";
	public static final String CST_DOC_XPATH_TOUTATICE_INTERNAL_CONTEXTUALIZATION = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":contextualizeInternalContents";
	public final static String CST_DOC_XPATH_NUXEO_DC_CREATOR = CST_DOC_SCHEMA_DC_PREFIX + ":creator";
	public final static String CST_DOC_XPATH_NUXEO_DC_TITLE = CST_DOC_SCHEMA_DC_PREFIX + ":title";
	public final static String CST_DOC_XPATH_NUXEO_WEBCONTAINER_EMAIL = CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_PREFIX + ":email";
	public final static String CST_DOC_XPATH_NUXEO_WEBCONTAINER_IS_WEB_CONTAINER = CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_PREFIX + ":isWebContainer";
	public static final String CST_DOC_XPATH_NUXEO_SECTIONS_PROPERTY_NAME = CST_DOC_SCHEMA_NUXEO_PUBLISH_PREFIX + ":sections";
	public static final String CST_DOC_XPATH_NUXEO_MAIL_RECIPIENTS = CST_DOC_SCHEMA_NUXEO_MAIL_PREFIX + ":recipients";
	public static final String CST_DOC_XPATH_NUXEO_PICTURE_BOOK_TEMPLATES = CST_DOC_SCHEMA_PICTURE_BOOK_PREFIX + ":picturetemplates";
	public static final String CST_DOC_XPATH_NUXEO_FILES = CST_DOC_SCHEMA_NUXEO_FILES_PREFIX + ":files";
	public static final String CST_DOC_XPATH_DC_KEYWORDS = CST_DOC_SCHEMA_DC_PREFIX + ":subjects";
	public final static String CST_DOC_XPATH_DC_PUBLISHER = CST_DOC_SCHEMA_DC_PREFIX + ":publisher";
	public final static String CST_DOC_XPATH_NUXEO_DC_ISSUED = CST_DOC_SCHEMA_DC_PREFIX + ":issued";
	public final static String CST_DOC_XPATH_MNT_AUTOMATION_LOGS_STATUS = CST_DOC_SCHEMA_MNT_PREFIX + ":automationLogsStatus";
	public final static String CST_DOC_XPATH_MNT_AUTOMATION_LOGS_THRESHOLD = CST_DOC_SCHEMA_MNT_PREFIX + ":automationLogsThreshold";
	public static final String CST_DOC_SCHEMA_TOUTATICE_WEBID = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":webid";
    public static final String CST_DOC_XPATH_TOUTATICESPACE_WEBID_ENABLED = CST_DOC_SCHEMA_TOUTATICE_SPACE_PREFIX + ":hasWebIdEnabled";
    public static final String CST_DOC_XPATH_WEB_CONF_CODE = CST_DOC_SCHEMA_WEB_CONF + ":code";
    public static final String CST_DOC_XPATH_COMMENTABLE_SPACE = CST_DOC_SCHEMA_TOUTATICE_SPACE_PREFIX + ":spaceCommentable";
    
    // Calendar
    public static final String CST_DOC_XPATH_TTC_EVT_ALL_DAY = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":allDay";
    public static final String CST_DOC_XPATH_TTC_EVT_DATE_BEGIN = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":dateBegin";
    public static final String CST_DOC_XPATH_TTC_EVT_TIME_BEGIN = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":timeBegin"; 
    public static final String CST_DOC_XPATH_TTC_EVT_DATE_END = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":dateEnd";
    public static final String CST_DOC_XPATH_TTC_EVT_TIME_END = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":timeEnd";
    public static final String CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":dateTimeBegin";
    public static final String CST_DOC_XPATH_TTC_EVT_DATE_TIME_END = CST_DOC_SCHEMA_TTC_EVENT_PREFIX + ":dateTimeEnd";
    
    //Remote sections of published live document
    public static final String CST_DOC_XPATH_REMOTE_SECTIONS = CST_DOC_REMOTE_SECTIONS + ":remoteSections";
    public static final String CST_DOC_REMOTE_SECTIONS_PROP_NAME = "remoteSections";
    public static final String CST_DOC_REMOTE_SECTIONS_TITLE_PROP = "sectionTitle";
    public static final String CST_DOC_REMOTE_SECTIONS_PATH_PROP = "sectionPath";
    public static final String CST_DOC_REMOTE_SECTIONS_URL_PROP = "sectionURL";
    public static final String CST_DOC_REMOTE_SECTIONS_PROXY_URL_PROP = "proxyURL";
    public static final String CST_DOC_REMOTE_SECTIONS_VERSION_PROP = "version";
    public static final String CST_DOC_REMOTE_SECTIONS_PENDING_PROP = "pending";
	 
	// life cycle states
	public final static String CST_DOC_STATE_PROJECT = "project";
	public final static String CST_DOC_STATE_APPROVED = "approved";
	public final static String CST_DOC_STATE_DELETED = "deleted";
	
	// custom permissions
	public final static String CST_PERM_VALIDATE = "validationWorkflow_validation";
	
	// operation chains
	public final static String CST_OPERATION_DOCUMENT_PUBLISH_ONLY = "setOnLine";
	public final static String CST_OPERATION_DOCUMENT_PUBLISH_REQUEST = "onlineWorkflow_start";
	public final static String CST_OPERATION_DOCUMENT_UNPUBLISH = "setOffLine";
	public final static String CST_OPERATION_DOCUMENT_UNPUBLISH_SELECTION = "setOffLineSelection";
	public final static String CST_OPERATION_RESIZE_IMAGE = "image-resizing";
	
	// operations constants
	public final static String CST_OPERATION_PARAM_NO_TRANSITION = "no_transition";
	
	// others
	public final static String CHILDREN_DOCUMENT_LIST = "CHILDREN_DOCUMENT_LIST";
	
}
