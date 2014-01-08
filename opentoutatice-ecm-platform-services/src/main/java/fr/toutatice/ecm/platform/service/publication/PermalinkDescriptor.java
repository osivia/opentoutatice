package fr.toutatice.ecm.platform.service.publication;

import java.util.*;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "permalink")
public class PermalinkDescriptor {
	@XNode("@name")
	private String name;

	@XNode("@classpath")
	private String classpath;
	@XNode("@default")
	protected boolean defaultPermalink;
	@XNode("@enabled")
	protected boolean enabled;

	@XNode("host")
	private String hostServer;

	protected Map<String, String> parameters;
	protected List<ParamPermalinkDescriptor> paramsDescriptor;

	@XNodeList(value = "parameters/param", type = ParamPermalinkDescriptor[].class, componentType = ParamPermalinkDescriptor.class)
	public void setParameters(ParamPermalinkDescriptor params[]) {
		this.parameters = new HashMap<String, String>();
		this.paramsDescriptor = new ArrayList<ParamPermalinkDescriptor>(params.length);
		
		for (ParamPermalinkDescriptor param : params) {
			this.parameters.put(param.getName(), param.getParamValue());
			this.paramsDescriptor.add(param);
		}

	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public List<ParamPermalinkDescriptor> getParamsDescriptor(){
		return paramsDescriptor;
	}

	public String getHostServer() {
		return hostServer;
	}

	public void setHostServer(String hostServer) {
		this.hostServer = hostServer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public boolean getDefaultPermalink() {
		return defaultPermalink;
	}

	public void setDefaultPermalink(boolean defaultCodec) {
		defaultPermalink = defaultCodec;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
