package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;


/**
 * Interface used to render columns
 * @author jayhodgson
 *
 */
public interface APITableInitializedColumnRenderer {
	
	/**
	 * Return the renderers output column data.  Precalculate and cache this map, because this may be called many times
	 * @return
	 */
	Map<String, List<String>> getColumnData();
	
	/**
	 * return the renderers output column names
	 * @return
	 */
	List<String> getColumnNames();
}
