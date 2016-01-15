package fr.toutatice.ecm.platform.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;

@Operation(id = SetDocumentACL.ID, 
	category = Constants.CAT_DOCUMENT, 
	label = "Set complete ACL", 
	description = "Set the complete Acces Control List on the input document(s). Parameters: 'acl' is the name of the access control list to set ('local' as default). Set 'break' to true if you want to break rights inheritance. 'entries' must respect the format '<principal>:<permission>:<grant>,<principal>:<permission>:<grant>,...'. . As default, the ACL list is overwritten. Returns the document(s).")
public class SetDocumentACL {

	private static final Log log = LogFactory.getLog(SetDocumentACL.class);

	public static final String ID = "Document.SetACL";
	public static final String ACE_DELIMITER = ",";
	public static final String VALUE_DELIMITER = ":";	

	@Context
	protected CoreSession session;

	@Param(name = "acl", required = false, values = ACL.LOCAL_ACL)
	String aclName = ACL.LOCAL_ACL;

	@Param(name = "entries")
	protected String entries;
	
	@Param(name = "overwrite", required = false, values = "false,true")
	boolean doOverwrite = true;

	@Param(name = "break", required = false, values = "false,true")
	boolean doBreakInheritance = false;

	@OperationMethod(collector=DocumentModelCollector.class)
	public DocumentModel run(DocumentModel doc) throws Exception {
		setACE(doc.getRef());
		return session.getDocument(doc.getRef());
	}

	@OperationMethod(collector=DocumentModelCollector.class)
	public DocumentModel run(DocumentRef doc) throws Exception {
		setACE(doc);
		return session.getDocument(doc);
	}
		
	protected void setACE(DocumentRef ref) throws ClientException {
		List<ACE>[] aceList = slurpACEs(entries);
		
		ACP acp = session.getACP(ref);	
		
		ACLImpl acl = (ACLImpl) acp.getACL(aclName);
		if(acl==null || doOverwrite){
			acl = new ACLImpl(aclName);
		}
		
		acl.addAll(aceList[0]);
		acl.removeAll(aceList[1]);	

		acp.addACL(acl);

		session.setACP(ref, acp, true);
	}

	private List<ACE>[] slurpACEs(String entries) {
		List<ACE>[] tabList = new ArrayList[2];
		List<ACE> lstAcePos = new ArrayList<ACE>();
		List<ACE> lstAceNeg = new ArrayList<ACE>();
		
		StringTokenizer aceTokenizer = new StringTokenizer(entries, ACE_DELIMITER);
		while (aceTokenizer.hasMoreTokens()) {
			String aceStg = aceTokenizer.nextToken();
			Pattern p = Pattern.compile("^(.+?)" + VALUE_DELIMITER + "(.+?)" + VALUE_DELIMITER + "(.+?)$");
			Matcher m = p.matcher(aceStg);
			if (m.find()) {
				String user = m.group(1);
				String permission = m.group(2);
				boolean granted = Boolean.parseBoolean(m.group(3));
				ACE ace = new ACE(user, permission, true);
				if(granted){
					lstAcePos.add(ace);
				}else{
					lstAceNeg.add(ace);
				}
			} else {
				log.warn("ACE doesn't respect the format <principal>" + VALUE_DELIMITER + "<permission>" + VALUE_DELIMITER + "<grant>, entry='" + aceStg + "'");
			}
		}
		
		// always break inheritance at end 
		if (doBreakInheritance) {
			lstAcePos.add(new ACE(SecurityConstants.EVERYONE, SecurityConstants.EVERYTHING, false));
		}
		tabList[0] = lstAcePos;
		tabList[1] = lstAceNeg;
		return tabList;
	}

}
