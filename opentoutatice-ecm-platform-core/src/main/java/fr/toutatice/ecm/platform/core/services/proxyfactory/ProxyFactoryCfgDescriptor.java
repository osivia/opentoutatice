package fr.toutatice.ecm.platform.core.services.proxyfactory;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "handler")
public class ProxyFactoryCfgDescriptor {
	
	@XNode("@serviceClass")
	private String serviceClass;

	@XNode("@handlerClass")
	private String handlerClass;
	
	@XNode("@enabled")
	protected boolean enabled;

	public String getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	public String getHandlerClass() {
		return handlerClass;
	}

	public void setHandlerClass(String handlerClass) {
		this.handlerClass = handlerClass;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
