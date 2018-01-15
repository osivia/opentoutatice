/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction;

import java.util.HashMap;

import fr.toutatice.ecm.platform.automation.transaction.TransactionalConversation;


/**
 * FIXME: use ThreadPool instead (pattern Executor)?
 * 
 * @author david
 *
 */
public class TransactionalConversationPool extends HashMap<String, TransactionalConversation> {

    private static final long serialVersionUID = -2126420321964388425L;

    // FIXME: make configurable by nuxeo.conf
    private static final int ttlInPool = 500;

}
