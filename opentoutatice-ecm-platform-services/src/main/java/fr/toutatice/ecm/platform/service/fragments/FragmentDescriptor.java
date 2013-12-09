package fr.toutatice.ecm.platform.service.fragments;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

import fr.toutatice.ecm.platform.service.fragments.types.Fragment;

@XObject("fragment")
public class FragmentDescriptor implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7956864563251815573L;

    protected Fragment instance;

    @XNode("@class")
    protected Class fragmentClass;

    @XNode("@code")
    protected String code;

    @XNode("@layout")
    protected String layout;

    @XNode("@title")
    protected String title;

    public void initFragment() throws Exception {
        instance = (Fragment) fragmentClass.newInstance();
    }


    /**
     * @return the instance
     */
    public Fragment getInstance() {
        return instance;
    }


    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }


    /**
     * @return the layout
     */
    public String getLayout() {
        return layout;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }




}
