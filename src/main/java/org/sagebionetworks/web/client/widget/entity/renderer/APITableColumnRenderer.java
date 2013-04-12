package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface used to render a column value
 * @author jayhodgson
 *
 */
public interface APITableColumnRenderer {
	/**
	 * renderer is initialized with the model data
	 * @param columnData
	 * @param callback
	 */
	void init(Map<String, List<String>> columnData, APITableColumnConfig config, AsyncCallback<APITableInitializedColumnRenderer> callback);
}
