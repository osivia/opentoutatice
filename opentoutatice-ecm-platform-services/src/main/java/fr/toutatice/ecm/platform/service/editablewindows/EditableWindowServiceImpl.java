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
package fr.toutatice.ecm.platform.service.editablewindows;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.service.editablewindows.types.EditableWindow;

/**
 * Generic fragment class
 * 
 */
public class EditableWindowServiceImpl extends DefaultComponent implements EditableWindowService {


    private static final Log log = LogFactory.getLog(EditableWindowServiceImpl.class);

    /** Map of all fragment types */
    private static final Map<EwDescriptor, EditableWindow> ewMap = new HashMap<EwDescriptor, EditableWindow>();

    /** Main nuxeo schema shared by all fragments */
    public static String SCHEMA = "fragments";


    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {

        EwDescriptor contribDescriptor = (EwDescriptor) contribution;

        contribDescriptor.initFragment();

        addEwType(contribDescriptor);
    }


    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {

        EwDescriptor contribDescriptor = (EwDescriptor) contribution;

        removeEwType(contribDescriptor);
    }



    private void addEwType(EwDescriptor contribution) {
        if (ewMap.get(contribution) == null) {
            ewMap.put(contribution, contribution.getInstance());

        } else {
            log.warn("Contribution " + contribution.code + " has already been registered.");
        }
    }

    private void removeEwType(EwDescriptor contribution) {
        ewMap.remove(contribution);

    }

    @Override
	public Map.Entry<EwDescriptor, EditableWindow> findByCode(String code) throws EwServiceException {
        for (Map.Entry<EwDescriptor, EditableWindow> entry : ewMap.entrySet()) {
            if (entry.getKey().getCode().equals(code)) {
                return entry;
            }
        }
        throw new EwServiceException("osivia.error.fragment_not_found");

    }

    /**
     * @param doc nuxeo document
     * @param uri id of the EW
     * @return the category
     */
    @Override
	public Entry<EwDescriptor, EditableWindow> getEwEntry(DocumentModel doc, String uri) throws EwServiceException {

        String category = null;
        Map<String, Object> properties;

        try {
            properties = doc.getProperties(SCHEMA);

            Collection<Object> values = properties.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, Object>> listeEw = (List<Map<String, Object>>) liste;

                for (Map<String, Object> ew : listeEw) {
                    if (uri.equals(ew.get("uri"))) {

                        category = ew.get("fragmentCategory").toString();
                        break;
                    }
                }
            }
        } catch (ClientException e) {
            throw new EwServiceException(e);
        }

        if (category == null)
            throw new EwServiceException("osivia.error.fragment_not_found");

        Entry<EwDescriptor, EditableWindow> findByCode = findByCode(category);


        return findByCode;

    }


    /**
     * Initialize a default entry in the main EW schema
     * 
     * @param doc the current simplepage
     * @param region the cms region
     * @param belowUri the position where the fragment goes
     * @param code2
     * @return the new id (timestamp)
     */
    @Override
	public String prepareCreation(DocumentModel doc, EditableWindow specific, String category, String region, String belowUri, String code2)
            throws EwServiceException {

        String uri = null;

        Map<String, Object> properties;

        try {
            properties = doc.getProperties(SCHEMA);

            Collection<Object> values = properties.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, Object>> listeEw = (List<Map<String, Object>>) liste;

                // ================== Calcul des propriétés du schéma
                // Génération d'une URI
                uri = Long.toString(new Date().getTime());

                // Calcul du positionnement
                // Par défaut en haut de la région (0)
                String regionId = region;
                Integer order = new Integer(0);
                // Si l'uri du fragment au dessus est précisé, récupération de
                // la position et de la région du fragment
                if (belowUri != null) {
                    for (Map<String, Object> window : listeEw) {
                        if (belowUri.equals(window.get("uri"))) {
                            regionId = window.get("regionId").toString();
                            String orderStr = window.get("order").toString();
                            order = Integer.parseInt(orderStr) + 1;
                            break;
                        }
                    }
                }


                // ================== Décalage des autres fragments de cette région
                for (Map<String, Object> window : listeEw) {
                    String regionCompare = window.get("regionId").toString();
                    String orderCompare = window.get("order").toString();
                    if (regionId.equals(regionCompare) && Integer.parseInt(orderCompare) >= order) {
                        Integer newOrder = Integer.parseInt(orderCompare) + 1;
                        window.put("order", newOrder.toString());
                    }
                }

                // ================== Ajout d'une nouvelle entrée au schéma
                Map<String, Object> newEntry = new HashMap<String, Object>();

                newEntry.put("fragmentCategory", category);
                newEntry.put("order", order.toString());
                newEntry.put("regionId", regionId);
                newEntry.put("uri", uri);

                newEntry.put("title", "Nouveau fragment");
                newEntry.put("hideTitle", Boolean.FALSE.toString());
				newEntry.put("collapsed", Boolean.FALSE.toString());
                newEntry.put("style", "");

                listeEw.add(newEntry);

                doc.setProperties(SCHEMA, properties);

            }
        } catch (ClientException e) {
            throw new EwServiceException(e);
        }

        specific.prepareCreation(doc, uri, region, belowUri, code2);

        return uri;
    }



}
