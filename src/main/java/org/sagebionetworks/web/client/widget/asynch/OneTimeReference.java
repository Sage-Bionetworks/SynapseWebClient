package org.sagebionetworks.web.client.widget.asynch;

/**
 * Holds a reference to on object that can only be retrieved once.
 * 
 * @author John
 *
 * @param <T>
 */
public class OneTimeReference<T> {

	private T refrence;

	/**
	 * @param toRefrence The object to be referenced.
	 */
	public OneTimeReference(T toRefrence) {
		this.refrence = toRefrence;
	}

	/**
	 * The first time this method is called the reference will be returned. All subsequent calls will
	 * return null.
	 * 
	 * @return
	 */
	public T getReference() {
		T local = refrence;
		refrence = null;
		return local;
	}

}
