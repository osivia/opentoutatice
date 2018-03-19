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

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;

public class ToutaticeSerializationHelper {

	/**
	 * Serialize the object passed-in parameter and encapsulate it into a Nuxeo blob object
	 * 
	 * @param object the object to serialize into a Nuxeo Blob object
	 * @return the Nuxeo Blob object
	 * @throws Exception if any processing error occurs during serialization
	 */
	public Blob serializeObjectIntoBlob(Serializable object) throws Exception {
		Blob blob = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream bos = null;

		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			oos.close();
			
			blob = new ByteArrayBlob(bos.toByteArray());
		} finally {
			if (null != bos) bos.close();
		}

		return blob;
	}

	/**
	 * 
	 * @param blob the blob containing the serialized object
	 * @return the unserialized object
	 * @throws Exception if any processing error occurs during unserialization
	 */
	public Serializable unserializeObjectIntoBlob(Blob blob) throws Exception {
		Serializable object = null;
		ObjectInputStream ois = null;

		ois = new ObjectInputStream(blob.getStream());
		object = (Serializable) ois.readObject();
		ois.close();

		return object;
	}	
}
