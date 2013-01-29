package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

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
	void init(List<String> columnData, AsyncCallback<Void> callback);
	/**
	 * @return the number of columns that this renderer produces (usually 1);
	 */
	int getColumnCount();
	
	/**
	 * Return the renderer specified column name for a particular index.  If null is returned (should be the case if this renderers a single column), then the user specified display column name is used)
	 * @param rendererColIndex
	 * @return
	 */
	String getRenderedColumnName(int rendererColIndex);
	
	/**
	 * Given the original column value, what value should be rendered for this renderer column (since renderers can produce >1 column).
	 * @param value
	 * @param colIndex
	 * @return
	 */
	String render(String value, int rendererColIndex);
}
