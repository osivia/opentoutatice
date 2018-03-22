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
package fr.toutatice.ecm.platform.automation.transaction.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.automation.transaction.TransactionalConversationManager;
import fr.toutatice.ecm.platform.automation.transaction.infos.OperationInfos;
import fr.toutatice.ecm.platform.automation.transaction.infos.OperationInfosFactory;
import fr.toutatice.ecm.platform.core.components.ToutaticeAbstractServiceHandler;
import fr.toutatice.ecm.platform.core.services.maintenance.ToutaticeMaintenanceService;

public class ToutaticeAutomationServiceHandler<T> extends ToutaticeAbstractServiceHandler<T> {

	private static final Log log = LogFactory.getLog(ToutaticeAutomationServiceHandler.class);
	private ToutaticeMaintenanceService mntService;
	
	public static ThreadLocal<String> threadLocal  = new ThreadLocal<>();

	@Override
	public T newProxy(T object, Class<T> itf) {
		setObject(object);
		return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { itf }, this));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String ops = "";
		String inputStg = "";
		String uid = "";
		String type = "";
		String principalStg = "";
		CoreSession session = null;

		try {
			if ("run".equals(method.getName()) && log.isDebugEnabled()) {
				for (Object arg : args) {
					if (arg instanceof String) {
						ops = "operation " + (String) arg;
					} else if (arg instanceof OperationChain) {
						OperationChain opc = (OperationChain) arg;
						ops = "chain " + opc.getId() + " (";
						List<OperationParameters> operations = opc.getOperations();
						for (OperationParameters operation : operations) {
							ops = ops + operation.id() + " {";
							Map<String, Object> params = operation.map();
							if (0 < params.size()) {
								Set<String> paramKS = params.keySet();
								for (String pk : paramKS) {
									Object value = params.get(pk);
									if (value instanceof String) {
										ops = ops + pk + ":" + (String) params.get(pk) + " ";
										if (null != session && ("path".equals(pk) || ("value".equals(pk)))) {
											try {
												DocumentModel doc = session.getDocument(new PathRef((String) value));
												uid = doc.getId();
												type = doc.getType();
											} catch (Exception e) {
												log.debug("might have no permission to fetch the document with the user session or not found");
											}
										}
									}
								}
							}
							ops = ops.trim() + "} ";
						}
						ops = ops.trim() + ")";
					} else if (arg instanceof OperationContext) {
						OperationContext oc = (OperationContext) arg;
						Object input = oc.getInput();
						session = oc.getCoreSession();
						if (input instanceof DocumentModel) {
							if (null != input) {
								inputStg = ((DocumentModel) input).getPathAsString();
								uid = ((DocumentModel) input).getId();
								type = ((DocumentModel) input).getType();
							}
						}

						Principal principal = oc.getPrincipal();
						if (null != principal) {
							principalStg = principal.getName();
						}
					}
				}

				if (isLogEnabled(session)) {
					long startTime = System.currentTimeMillis();
					Object result = method.invoke(object, args);
					long stopTime = System.currentTimeMillis();

					if ((stopTime - startTime) > getLogThreshold(session)) {
						log.debug(String.format("Duration:%d ms - %s [input:%s, uid:%s, type:%s, principal:%s]", (stopTime - startTime), ops, inputStg, uid, type, principalStg));
					}

					return result;
				} else {
					return invoke(method, args);
				}
			} else {
			    return invoke(method, args);
			}
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	private Object invoke(Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException
	{
	    String txId = threadLocal.get();
	    if (StringUtils.isNotBlank(txId) && "run".equals(method.getName()))
	    {   // Ctx = args[0], opId = args[1], params = args [2]
	        
	        return TransactionalConversationManager.getInstance().notify(txId, args[0], args[1], args[2]);
	    }
	    else
	    {
	        return method.invoke(object, args);
	    }

	}

	private long getLogThreshold(CoreSession session) {
		mntService=getMntService();
		if(mntService==null){
			return 1000;
		}else{
			return mntService.getAutomationLogsThreshold(session);
		}
	}

	private boolean isLogEnabled(CoreSession session) {
		boolean res = false;
		mntService=getMntService();
		if(mntService!=null){			
			res = mntService.isAutomationLogsEnabled(session);
		}
		return res;
	}

	private ToutaticeMaintenanceService getMntService() {
		if (null == this.mntService) {
			this.mntService = Framework.getLocalService(ToutaticeMaintenanceService.class);
		}
		return this.mntService;
	}

}
