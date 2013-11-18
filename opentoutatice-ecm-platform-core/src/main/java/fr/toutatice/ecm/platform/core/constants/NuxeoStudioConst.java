package fr.toutatice.ecm.platform.core.constants;

/**
 * This class groups together all definitions done within the NuxeoStudio tool and referenced in this package
 * Core Nuxeo definitions are also stored here
 * 
 * @author mberhaut1
 */
public class NuxeoStudioConst {
	// document types
	public final static String CST_DOC_TYPE_WORKSPACE = "Workspace";
	public final static String CST_DOC_TYPE_USER_WORKSPACE = "UserWorkspace";
	public final static String CST_DOC_TYPE_PICTURE_BOOK = "PictureBook";
	public final static String CST_DOC_TYPE_SECTION = "Section";
	public final static String CST_DOC_TYPE_DOMAIN = "Domain";
	public final static String CST_DOC_TYPE_ROOT = "Root";
	public final static String CST_DOC_TYPE_DAGE_DEMANDE_RECEPTION = "demande-reception";
	public final static String CST_DOC_TYPE_DAGE_DEMANDE_SALLE = "demande-salle";
	public final static String CST_DOC_TYPE_DAGE_DEMANDE_TRAVAUX = "demande-travaux";
	public final static String CST_DOC_TYPE_CONTACT_MESSAGE = "ContactMessage";
	public final static String[] CST_DOC_TYPE_DAGE_LIST = new String[] {CST_DOC_TYPE_DAGE_DEMANDE_RECEPTION, 
																		CST_DOC_TYPE_DAGE_DEMANDE_SALLE,
																		CST_DOC_TYPE_DAGE_DEMANDE_TRAVAUX};

	// schemas
	public final static String CST_DOC_SCHEMA_ACAREN = "acaren";
	public final static String CST_DOC_SCHEMA_ACAREN_PREFIX = "acr";
	public final static String CST_DOC_SCHEMA_NUXEO_WEBCONTAINER = "webcontainer";
	public final static String CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_PREFIX = "webc";
	public final static String CST_DOC_SCHEMA_NUXEO_FILES = "files";
	public final static String CST_DOC_SCHEMA_NUXEO_FILES_PREFIX = "files";
	public final static String CST_DOC_SCHEMA_USER_PREFIX = "user";
	public final static String CST_DOC_SCHEMA_DC_PREFIX = "dc";
	public final static String CST_DOC_SCHEMA_TOUTATICE = "toutatice";
	public final static String CST_DOC_SCHEMA_TOUTATICE_PREFIX = "ttc";
	public final static String CST_DOC_SCHEMA_TOUTATICE_SPACEID = "ttc:spaceID";
	public final static String CST_DOC_SCHEMA_PUBLISHING = "publishing";
	public final static String CST_DOC_SCHEMA_NUXEO_PUBLISH_PREFIX = "publish";
	public final static String CST_DOC_SCHEMA_DEMANDE = "demande";
	public final static String CST_DOC_SCHEMA_DEMANDE_PREFIX = "dde";
	public final static String CST_DOC_SCHEMA_ANNONCE = "annonce";
	public final static String CST_DOC_SCHEMA_ANNONCE_PREFIX = "annonce";
	public static final String CST_DOC_SCHEMA_NUXEO_MAIL_PREFIX = "mail";
	public static final String CST_DOC_SCHEMA_CONTACT_MESSAGE_FOLDER_PREFIX = "cmf";
	public static final String CST_DOC_SCHEMA_PICTURE_BOOK_PREFIX = "picturebook";
	
	// facets
	public final static String CST_DOC_FACET_ORGANISATION_SOURCE = "OrganisationSource";
	public final static String CST_DOC_FACET_WEB_VIEW = "WebView";
	public static final String CST_FACET_SPACE = "Space";
	public static final String CST_FACET_SPACE_NAVIGATION_ITEM = "SpaceNavigationItem";
	public static final String CST_FACET_SPACE_CONTENT = "SpaceContent";


	// meta-data
	public final static String CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_EMAIL = "email";

	// meta-data xpath
	public final static String CST_DOC_XPATH_ACAREN_AUTHOR = CST_DOC_SCHEMA_ACAREN_PREFIX + ":auteur";
	public final static String CST_DOC_XPATH_ACAREN_PUBLISHER = CST_DOC_SCHEMA_ACAREN_PREFIX + ":publisher";
	public final static String CST_DOC_XPATH_USER_EMAIL = CST_DOC_SCHEMA_USER_PREFIX + ":email";
	public final static String CST_DOC_XPATH_TOUTATICE_IMAGES = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":images";
	public final static String CST_DOC_XPATH_TOUTATICE_STAMP = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":vignette";
	public final static String CST_DOC_XPATH_TOUTATICE_SIM = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":showInMenu";
	public static final String CST_DOC_TOUTATICE_INTERNAL_CONTEXTUALIZATION = CST_DOC_SCHEMA_TOUTATICE_PREFIX + ":contextualizeInternalContents";
	public final static String CST_DOC_XPATH_NUXEO_DC_CREATOR = CST_DOC_SCHEMA_DC_PREFIX + ":creator";
	public final static String CST_DOC_XPATH_NUXEO_DC_TITLE = CST_DOC_SCHEMA_DC_PREFIX + ":title";
	public final static String CST_DOC_XPATH_NUXEO_WEBCONTAINER_EMAIL = CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_PREFIX + ":email";
	public final static String CST_DOC_XPATH_NUXEO_WEBCONTAINER_IS_WEB_CONTAINER = CST_DOC_SCHEMA_NUXEO_WEBCONTAINER_PREFIX + ":isWebContainer";
	public static final String CST_DOC_XPATH_NUXEO_SECTIONS_PROPERTY_NAME = CST_DOC_SCHEMA_NUXEO_PUBLISH_PREFIX + ":sections";
	public static final String CST_DOC_XPATH_NUXEO_MAIL_RECIPIENTS = CST_DOC_SCHEMA_NUXEO_MAIL_PREFIX + ":recipients";
	public static final String CST_DOC_XPATH_NUXEO_PICTURE_BOOK_TEMPLATES = CST_DOC_SCHEMA_PICTURE_BOOK_PREFIX + ":picturetemplates";
	public static final String CST_DOC_XPATH_DEMANDEUR_NOM = CST_DOC_SCHEMA_DEMANDE_PREFIX + ":demandeur";
	public static final String CST_DOC_XPATH_HEAD_IMAGE = CST_DOC_SCHEMA_ANNONCE_PREFIX + ":image";
	public static final String CST_DOC_XPATH_NUXEO_FILES = CST_DOC_SCHEMA_NUXEO_FILES_PREFIX + ":files";
	public static final String CST_DOC_XPATH_CONTACT_MESSAGE_FOLDER_RECIPIENTS = CST_DOC_SCHEMA_CONTACT_MESSAGE_FOLDER_PREFIX + ":recipients";
	
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
	
	// vocabularies
	public final static String[] CST_VOCABULARY_ORGANISATION_SOURCES = new String[] {"sourcesOrganisationnelles", "sourcesOrganisationnelles_L2"};
}
