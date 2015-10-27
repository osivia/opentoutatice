/**
 * 
 */
package fr.toutatice.ecm.platform.core.persistence;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;

/**
 * Allows to write "old" documents in current Nuxeo version.
 * 
 * @author david
 *
 */
public class ToutaticeDocumentModelWriter extends DocumentModelWriter {
	
	private static final Log log = LogFactory.getLog(ToutaticeDocumentModelWriter.class);

	public ToutaticeDocumentModelWriter(CoreSession session, String parentPath, int i) {
		super(session, parentPath, i);
	}
	
	@Override
	protected DocumentModel createDocument(ExportedDocument xdoc, Path toPath)
			throws ClientException {

		DocumentModel createDocument = super.createDocument(xdoc, toPath);
		
		fixInheritedACP(createDocument);
		authorizeCurrentUser(createDocument);

		return createDocument;

	}
	
    /**
     * Disable blocking of inheritance if any
     * @param createDocument
     */
    private void fixInheritedACP(DocumentModel createDocument) {
        CoreSession originalSession = this.session;

        try {
            this.session = CoreInstance.openCoreSession("default", SecurityConstants.SYSTEM_USERNAME);

            ACP acp = this.session.getACP(createDocument.getRef());
            if (acp != null) {
                ACL acl = acp.getACL(ACL.LOCAL_ACL);
                if (acl != null) {
                    ACE[] acEs = acl.getACEs();

                    if (ArrayUtils.isNotEmpty(acEs)) {
                        Iterator<ACE> acEsIt = Arrays.asList(acEs).iterator();

                        boolean blockInherit = false;
                        ACE blockInheritACE = null;
                        while (acEsIt.hasNext() && !blockInherit) {
                            ACE ace = acEsIt.next();
                            if ("Everyone".equals(ace.getUsername()) && "Everything".equals(ace.getPermission())) {
                                blockInherit = ace.isDenied();
                                if (blockInherit) {
                                    blockInheritACE = ace;
                                }
                            }
                        }

                        if (blockInheritACE != null) {
                            acEs = (ACE[]) ArrayUtils.removeElement(acEs, blockInheritACE);
                            acl.setACEs(acEs);
                            acp.addACL(acl);
                            this.session.setACP(createDocument.getRef(), acp, true);
                        }
                    }
                }
            }
        } finally {
            this.session = originalSession;
        }
    }
	
    /**
     * Add authorizations to user who imports
     * and have Write permission.
     */
	private void authorizeCurrentUser(DocumentModel createDocument) {

        if (!this.session.hasPermission(createDocument.getRef(),
                SecurityConstants.WRITE)) {
            CoreSession originalSession = this.session;

            try {
                this.session = CoreInstance.openCoreSession("default",
                        SecurityConstants.SYSTEM_USERNAME);

                ACP acp = this.session.getACP(createDocument.getRef());
                if (acp != null) {
                    ACL acl = acp.getACL(ACL.LOCAL_ACL);
                    if (acl != null) {
                        ACE[] acEs = acl.getACEs();

                        if (ArrayUtils.isNotEmpty(acEs)) {

                            NuxeoPrincipal principal = (NuxeoPrincipal) originalSession
                                    .getPrincipal();
                            ACE ace = new ACE(principal.getName(),
                                    SecurityConstants.WRITE, true);

                            acEs = (ACE[]) ArrayUtils.add(acEs, ace);
                            acl.setACEs(acEs);
                            acp.addACL(acl);
                            this.session.setACP(createDocument.getRef(), acp,
                                    true);

                        }
                    }
                }
            } finally {
                this.session = originalSession;
            }
        }

    }

}
