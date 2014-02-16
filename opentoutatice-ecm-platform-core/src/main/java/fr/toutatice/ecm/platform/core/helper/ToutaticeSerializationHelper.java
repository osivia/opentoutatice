package fr.toutatice.ecm.platform.core.helper;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;

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
			blob = StreamingBlob.createFromByteArray(bos.toByteArray());
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
