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
package fr.toutatice.ecm.platform.core.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ToutaticeImageCollectionHelper {

	@SuppressWarnings("serial")
	private static final Map<String, Object> fakeItem = new HashMap<String, Object>() { {put("file", null); put("filename", null);} };
	private static ToutaticeImageCollectionHelper instance;
	
	private ToutaticeImageCollectionHelper() {
		// singleton
	}
	
	public static ToutaticeImageCollectionHelper instance() {
		if (null == instance) {
			instance = new ToutaticeImageCollectionHelper();
		}
		return instance;
	}
	
	public boolean isEmpty(List<Map<String, Object>> list) {
		boolean status = list.isEmpty();
		
		if (!status) {
			// vérifier que les éléments présents ne sont pas l'élément 'fake'
			status = true;
			Iterator<Map<String, Object>> itr = list.iterator();
			while (itr.hasNext()) {
				Map<String, Object> item = itr.next();
				if (!ToutaticeImageCollectionHelper.fakeItem.equals(item)) {
					status = false;
					break;
				}
			}
		}
		
		return status;
	}
	
	public int size(List<Map<String, Object>> list) {
		int size = 0;
		
		for (int i=0; i < list.size(); i++) {
			Map<String, Object> item = list.get(i);
			if (!ToutaticeImageCollectionHelper.fakeItem.equals(item)) {
				size++;
			}
		}
		
		return size;
	}

	public boolean add(List<Map<String, Object>> list, Map<String, Object> e) {
		boolean status = false;
		
		if (list.isEmpty()) {
			// aucun élément dans la structure: ajouter
			status = list.add(e);
		} else {
			// au moins un élément dans la structure: chercher un emplacement de libre ou étendre la taille de l'objet
			int index = 0;
			do {
				Map<String, Object> item = list.get(index);
				if (ToutaticeImageCollectionHelper.fakeItem.equals(item)) {
					// emplacement libre: remplacer l'élément 'fake'
					list.set(index, e);
					status = true;
					break;
				}
				index++;
			} while (index < list.size());
			
			if (false == status) {
				// étendre la taille: ajouter
				status = list.add(e);
			}
		}
		
		return status;
	}

	public Map<String, Object> remove(List<Map<String, Object>> list, int index) {
		Map<String, Object> item = null;
		
		if (index >= list.size()) {
			throw new IndexOutOfBoundsException();
		}
		
		if (index == (list.size() - 1)) {
			// dernier élément: le supprimer ainsi que ceux éventuellement 'null' précédents dans la liste
			item = list.remove(index);
			flushListTail(list);
		} else {
			// élément intermédiaire: le remplacer par 'null'
			item = list.set(index, ToutaticeImageCollectionHelper.fakeItem);
		}
		
		return item;
	}
	
	public boolean remove(List<Map<String, Object>> list, Object o) {
		boolean status = false;
		
		int index = list.indexOf(o);
		if (0 <= index) {
			remove(list, index);
			status = true;
		}
		
		return status;
	}
	
	// libère les éléments en queue de liste si désalloués
	private void flushListTail(List<Map<String, Object>> list) {
		for (int i = (list.size() - 1); i >= 0; i--) {
			Map<String, Object> item = list.get(i);
			if (ToutaticeImageCollectionHelper.fakeItem.equals(item)) {
				list.remove(i);
			} else {
				break;
			}
		}
	}
	
	public Iterator<Map<String, Object>> iterator(List<Map<String, Object>> list) {
		return instance.new ToutaticeImageCollectionIterator(list);
	}
	
	private class ToutaticeImageCollectionIterator implements Iterator<Map<String, Object>> {
		private List<Map<String, Object>> list;
		private boolean opRemoveAlreadyCalled;
		private int index;

		public ToutaticeImageCollectionIterator(List<Map<String, Object>> list) {
			this.index = 0;
			this.list = list;
			this.opRemoveAlreadyCalled = false;
		}
		
		public boolean hasNext() {
			boolean status = false;
			Map<String, Object> item = ToutaticeImageCollectionHelper.fakeItem;
			
			if (!ToutaticeImageCollectionHelper.instance().isEmpty(list)) {
				while (ToutaticeImageCollectionHelper.fakeItem.equals(item) && this.index < this.list.size()) {
					item = this.list.get(this.index++);
				}
				
				if (!ToutaticeImageCollectionHelper.fakeItem.equals(item)) {
					status = true;
				}
			}
			
			return status;
		}
		
		public Map<String, Object> next() {
			opRemoveAlreadyCalled = false;
			if (0 == this.index || this.index > this.list.size()) {
				throw new NoSuchElementException();
			}
			
			return this.list.get(this.index - 1);
		}
		
		@Override
		public void remove() {
			if (opRemoveAlreadyCalled) {
				throw new IllegalStateException();
			}
			
			if (0 == this.index || this.index > this.list.size()) {
				throw new NoSuchElementException();
			}
			
			ToutaticeImageCollectionHelper.instance().remove(this.list, this.index - 1);
			opRemoveAlreadyCalled = true;
		}
	}
	
}
