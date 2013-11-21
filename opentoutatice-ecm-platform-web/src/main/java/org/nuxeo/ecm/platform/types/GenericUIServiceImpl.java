/**
 * 
 */
package org.nuxeo.ecm.platform.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author david
 * 
 */
public class GenericUIServiceImpl extends DefaultComponent implements GenericUIService {

	private static final long serialVersionUID = -3335398967270359400L;
	private static final Log log = LogFactory
			.getLog(GenericUIServiceImpl.class);

	private static final String LAYOUTS_PT_EXT = "layouts";
	private static final int LAST_LAYOUT_INDEX = 0;
	private static final String BASE_TYPE = "Document";
	
	private TypeManager typeManager;

	@Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        typeManager = Framework.getService(TypeManager.class);
    }
	
	@Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if(LAYOUTS_PT_EXT.equals(extensionPoint)){
           GenericLayoutsDescriptor genericLayoutsDescriptor = (GenericLayoutsDescriptor) contribution;
           addGenericUIElements(genericLayoutsDescriptor);
        }
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.nuxeo.ecm.platform.types.GenericUIService#addGenericUIElements(org.nuxeo.ecm.platform.types.GenericLayoutsDescriptor)
	 */
    public void addGenericUIElements(GenericLayoutsDescriptor genericLayoutsDescriptor) throws Exception {

        GenericLayoutDescriptor[] genericLayouts = genericLayoutsDescriptor.getLayoutsToInsert();

        if (genericLayouts != null && genericLayouts.length > 0) {
            Collection<Type> types = typeManager.getTypes();
            for (Type type : types) {
                String docType = type.getId();
                if (!BASE_TYPE.equals(docType)) {
                    Map<String, Layouts> finalLayoutsMap = new HashMap<String, Layouts>(0);
                    Layouts finalLayouts = new Layouts();

                    Map<String, Layouts> layoutsOfTypeMap = type.getLayouts();
                    String mode = genericLayoutsDescriptor.getMode();
                    Layouts layoutsByModeAndType = layoutsOfTypeMap.get(mode);
                    if (layoutsByModeAndType != null) {
                        String[] layoutsNames = layoutsByModeAndType.getLayouts();

                        List<String> allLayoutsNames = new ArrayList<String>(Arrays.asList(layoutsNames));

                        for (GenericLayoutDescriptor genericLayout : genericLayouts) {
                            String genericLayoutName = genericLayout.getName();
                            String[] excludedTypes = genericLayout.getExcludedTypes();
                            if (!ArrayUtils.contains(excludedTypes, docType)) {
                                PositionLayoutDescriptor positionLayout = genericLayout.getPositionLayoutDescriptor();
                                String beforeLayout = positionLayout.getBeforeLayout();
                                String afterLayout = positionLayout.getAfterLayout();
                                int atLayout = positionLayout.getPositionLayout();
                                if (beforeLayout != null) {
                                    placeLayout(allLayoutsNames, genericLayoutName, beforeLayout, false);
                                } else if (afterLayout != null) {
                                    placeLayout(allLayoutsNames, genericLayoutName, afterLayout, true);
                                } else if (atLayout != PositionLayoutDescriptor.POS_UNDEFINED) {
                                    allLayoutsNames.add(atLayout - 1, genericLayoutName);
                                } else {
                                    log.error("No defined position for generic layout " + genericLayoutName);
                                }
                            }
                        }
                        finalLayouts.layouts = allLayoutsNames.toArray(new String[allLayoutsNames.size()]);
                        finalLayoutsMap.put(mode, finalLayouts);
                        type.setLayouts(finalLayoutsMap);
                    }
                }
            }
        }
    }

    private void placeLayout(List<String> allLayoutsNames, String genericLayoutName, String layout, boolean after) {
        int layoutPos = findLayoutPosition(allLayoutsNames, layout);
        if(layoutPos != PositionLayoutDescriptor.POS_UNDEFINED){
            if(after){
            allLayoutsNames.add(layoutPos + 1, genericLayoutName);
            }else{
                allLayoutsNames.add(layoutPos, genericLayoutName);
            }
        }else{
            /* Si le layout de référence pour le placement n'est pas trouvé,
             * on place le layout générique à la fin.
             */
            allLayoutsNames.add(allLayoutsNames.size(), genericLayoutName);
        }
    }

    private int findLayoutPosition(List<String> allLayoutsNames, String layout) {
        int position = PositionLayoutDescriptor.POS_UNDEFINED;
        boolean found = false;
        int index = 0;
        while (index < allLayoutsNames.size() && !found){
            String currentLayout = allLayoutsNames.get(index);
            if(currentLayout.equals(layout)){
                found = true;
                position = index;
            }
            index++;
        }
        return position;
    }

}
