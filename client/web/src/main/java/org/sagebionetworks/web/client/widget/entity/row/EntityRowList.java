package org.sagebionetworks.web.client.widget.entity.row;

import java.util.List;

/**
 * Marker for a row backed by a list.
 * @author jmhill
 *
 * @param <T>
 */
public interface EntityRowList<T> extends EntityRow<List<T>> {

	/**
	 * The type of the list
	 * @return
	 */
	Class<? extends T> getListClass();

}
