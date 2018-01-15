/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.infos;

import java.util.Map;
import java.util.Map.Entry;


/**
 * @author david
 *
 */
public class OperationInfos {

    private String opId;
    private Object input;
    private Map<String, Object> params;

    public OperationInfos() {
        super();
    }


    /**
     * @return the opId
     */
    public String getOpId() {
        return opId;
    }


    /**
     * @param opId the opId to set
     */
    public void setOpId(String opId) {
        this.opId = opId;
    }


    /**
     * @return the input
     */
    public Object getInput() {
        return input;
    }


    /**
     * @param input the input to set
     */
    public void setInput(Object input) {
        this.input = input;
    }


    /**
     * @return the params
     */
    public Map<String, Object> getParams() {
        return params;
    }


    /**
     * @param params the params to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuffer bf = new StringBuffer();

        bf.append("id: ").append(this.opId).append(" | input: ").append(this.input).append(" | params: ");
        if (this.params != null) {
            for (Entry<String, Object> entry : this.params.entrySet()) {
                bf.append(entry.getKey()).append(" , ").append(entry.getValue()).append(" \n");
            }
        } else {
            bf.append(" null ");
        }

        return bf.toString();
    }


}
