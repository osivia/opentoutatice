
package fr.toutatice.ecm.platform.service.permalink;

import org.nuxeo.common.xmap.annotation.XContent;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "param")
public class ParamPermalinkDescriptor
{


    @XNode("@name")
    protected String name;
    
    @XContent
    protected String paramValue;
    
    
    public String getName()
    {
        return name;
    }

    public String getParamValue()
    {
    	if (paramValue!=null) {
    		paramValue = paramValue.trim();
    		paramValue = paramValue.replace("\n", "");
    	
    	}  
        return paramValue;
    }

    public void setParamValue(String paramValue)
    {
        this.paramValue = paramValue;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    
}
