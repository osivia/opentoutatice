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
 * mberhaut1
 */
package org.nuxeo.ecm.platform.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.read.biff.MergedCellsRecord;

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
    private static final Log log = LogFactory.getLog(GenericUIServiceImpl.class);

    private static final String LAYOUTS_PT_EXT = "layouts";
    private static final String BASE_TYPE = "Document";
    private static final String ALL_TYPES = "*";

    private TypeManager typeManager;
    private List<GenericLayoutsDescriptor> allGenericLayoutsDescriptor = new ArrayList<GenericLayoutsDescriptor>();

    private enum Mode {
        VIEW("view"), EDIT("edit"), CREATE("create");

        private String value;

        Mode(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        typeManager = Framework.getService(TypeManager.class);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (LAYOUTS_PT_EXT.equals(extensionPoint)) {
            GenericLayoutsDescriptor genericLayoutsDescriptor = (GenericLayoutsDescriptor) contribution;
            allGenericLayoutsDescriptor.add(genericLayoutsDescriptor);
        }
    }

    @Override
    public void applicationStarted(ComponentContext context) throws Exception {

        Map<String, List<GenericLayoutsDescriptor>> sortedContribs = sortContribs(allGenericLayoutsDescriptor);
        if (sortedContribs != null) {
            for (List<GenericLayoutsDescriptor> contribsByMode : sortedContribs.values()) {
                GenericLayoutsDescriptor mergedContribs = mergeContribs(contribsByMode);
                addGenericUIElements(mergedContribs);
            }
        }
    }

    private Map<String, List<GenericLayoutsDescriptor>> sortContribs(List<GenericLayoutsDescriptor> allContribs) {
        Map<String, List<GenericLayoutsDescriptor>> sortedContribs = new HashMap<String, List<GenericLayoutsDescriptor>>();
        if (allContribs != null) {
            for (Mode modeType : Mode.values()) {
                String mode = modeType.value();
                for (GenericLayoutsDescriptor contrib : allContribs) {
                    String contribMode = contrib.getMode();
                    if (mode.equals(contribMode)) {
                        List<GenericLayoutsDescriptor> contribsOfMode = sortedContribs.get(mode);
                        if (sortedContribs.containsKey(mode)) {
                            contribsOfMode.add(contrib);
                        } else {
                            if (contribsOfMode == null) {
                                contribsOfMode = new ArrayList<GenericLayoutsDescriptor>();
                            }
                            contribsOfMode.add(contrib);
                            sortedContribs.put(mode, contribsOfMode);
                        }
                    }
                }
            }
        }
        return sortedContribs;
    }

    private GenericLayoutsDescriptor mergeContribs(List<GenericLayoutsDescriptor> contribsByMode) {
        GenericLayoutsDescriptor mergedContrib = new GenericLayoutsDescriptor();
        if (contribsByMode != null) {
            Map<String, GenericLayoutDescriptor> mergedLayouts = new HashMap<String, GenericLayoutDescriptor>();
            String mode = "";
            for (GenericLayoutsDescriptor contrib : contribsByMode) {
                mode = contrib.getMode();
                GenericLayoutDescriptor[] layouts = contrib.getLayoutsToInsert();
                if (layouts != null) {
                    for (GenericLayoutDescriptor layout : layouts) {
                        String layoutName = layout.getName();
                        if (!mergedLayouts.containsKey(layoutName)) {
                            mergedLayouts.put(layoutName, layout);
                        } else {
                            GenericLayoutDescriptor storedLayout = mergedLayouts.get(layoutName);
                            PositionLayoutDescriptor position = layout.getPositionLayoutDescriptor();
                            if (position != null) {
                                storedLayout.positionDescriptor = position;
                            }
                            String[] excludedTypes = layout.getExcludedTypes();
                            String[] storedExcludedTypes = storedLayout.getExcludedTypes();
                            if (excludedTypes != null && storedExcludedTypes != null) {
                                storedLayout.excludedTypes = (String[]) ArrayUtils.addAll(excludedTypes, storedExcludedTypes);
                            }
                            mergedLayouts.put(layoutName, storedLayout);
                        }
                    }
                }
            }
            mergedContrib.mode = mode;
            Collection<GenericLayoutDescriptor> finalMergedLayouts = mergedLayouts.values();
            if (finalMergedLayouts != null) {
                mergedContrib.layouts = finalMergedLayouts.toArray(new GenericLayoutDescriptor[0]);
            } else {
                mergedContrib.layouts = new GenericLayoutDescriptor[0];
            }

        }
        return mergedContrib;
    }

    /*
     * (non-Javadoc)
     * 
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
                    finalLayoutsMap.putAll(layoutsOfTypeMap);
                    String mode = genericLayoutsDescriptor.getMode();
                    Layouts layoutsByModeAndType = layoutsOfTypeMap.get(mode);
                    if (layoutsByModeAndType != null) {
                        String[] layoutsNames = layoutsByModeAndType.getLayouts();

                        List<String> allLayoutsNames = new ArrayList<String>(Arrays.asList(layoutsNames));

                        for (GenericLayoutDescriptor genericLayout : genericLayouts) {
                            String genericLayoutName = genericLayout.getName();
                            String[] contribExcludedTypes = genericLayout.getExcludedTypes();
                            boolean allTypesExcluded = false;
                            if(contribExcludedTypes != null && contribExcludedTypes.length > 0){
                                allTypesExcluded = contribExcludedTypes[0].equals(ALL_TYPES);
                            }
                            String[] excludedTypes = (String[]) ArrayUtils.addAll(new String[1], contribExcludedTypes);
                            if (!allTypesExcluded) {
                                if (!ArrayUtils.contains(excludedTypes, docType)) {
                                    PositionLayoutDescriptor positionLayout = genericLayout.getPositionLayoutDescriptor();
                                    if (positionLayout != null) {
                                        String beforeLayout = positionLayout.getBeforeLayout();
                                        String afterLayout = positionLayout.getAfterLayout();
                                        String atLayout = positionLayout.getPositionLayout();
                                        if (beforeLayout != null) {
                                            placeLayout(allLayoutsNames, genericLayoutName, beforeLayout, false);
                                        } else if (afterLayout != null) {
                                            placeLayout(allLayoutsNames, genericLayoutName, afterLayout, true);
                                        } else {
                                            if (!atLayout.equals(PositionLayoutDescriptor.LAST_POSITION)) {
                                                int atPos = Integer.valueOf(atLayout);
                                                if (atPos != PositionLayoutDescriptor.POS_UNDEFINED) {
                                                    allLayoutsNames.add(atPos - 1, genericLayoutName);
                                                } else {
                                                    log.error("No defined position for generic layout " + genericLayoutName);
                                                }
                                            } else {
                                                allLayoutsNames.add(allLayoutsNames.size(), genericLayoutName);
                                            }
                                        }
                                    } else {
                                        log.error("No defined position for generic layout " + genericLayoutName);
                                    }
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

    /**
     * Méthode permettant d'insérer un layout selon la donnée "after" ou "before".
     * 
     * @throws Exception
     */
    private void placeLayout(List<String> allLayoutsNames, String genericLayoutName, String layout, boolean after) throws Exception {
        int layoutPos = findLayoutPosition(allLayoutsNames, layout);
        if (layoutPos != PositionLayoutDescriptor.POS_UNDEFINED) {
            if (after) {
                allLayoutsNames.add(layoutPos + 1, genericLayoutName);
            } else {
                allLayoutsNames.add(layoutPos, genericLayoutName);
            }
        } else {
            /*
             * Si le layout de référence pour le placement n'est pas trouvé,
             * on place le layout générique à la fin.
             */
            allLayoutsNames.add(allLayoutsNames.size(), genericLayoutName);
        }
    }

    /**
     * Méthode permettant de trouver la position d'un layout dans la vue.
     */
    private int findLayoutPosition(List<String> allLayoutsNames, String layout) {
        int position = PositionLayoutDescriptor.POS_UNDEFINED;
        boolean found = false;
        int index = 0;
        while (index < allLayoutsNames.size() && !found) {
            String currentLayout = allLayoutsNames.get(index);
            if (currentLayout.equals(layout)) {
                found = true;
                position = index;
            }
            index++;
        }
        return position;
    }

}
