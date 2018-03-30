/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.toutatice.ecm.platform.automation.transaction.component.ToutaticeAutomationServiceHandler;


/**
 * @author david
 *
 */
public class TransactionalConversationFilter implements Filter {

    private static final Log log = LogFactory.getLog(TransactionalConversationFilter.class);

    public static final String TX_CONVERSATION_ID = "Tx-conversation-id";

    private static final String NOTIFY_TX_MANAGER_OP_ID = "Conversation.NotifyManager";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest == false) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String txId = httpReq.getHeader(TX_CONVERSATION_ID);

        if (StringUtils.isNotBlank(txId)) {
            String opId = getCallingOperationId(httpReq);

            if (log.isDebugEnabled()) {
                log.debug("Calling operation: " + opId + " in " + txId + " transaction");
            }

            if (!StringUtils.equals(NOTIFY_TX_MANAGER_OP_ID, opId)) {

                ToutaticeAutomationServiceHandler.threadLocal.set(txId);

            } else {

                if (log.isDebugEnabled()) {
                    log.debug("Not forwaded");
                }
            }
        }
        try  {
            chain.doFilter(request, response);
        } finally {
            ToutaticeAutomationServiceHandler.threadLocal.remove();
        }
    }

    private String getCallingOperationId(HttpServletRequest httpReq) {
        return StringUtils.substringAfterLast(httpReq.getPathInfo(), "/");
    }

    @Override
    public void destroy() {
        // Nothing
    }

}
