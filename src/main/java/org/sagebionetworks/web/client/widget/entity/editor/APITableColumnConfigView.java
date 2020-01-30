package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

public interface APITableColumnConfigView extends IsWidget {
	void configure(APITableColumnConfig data);

	void setSelectionChangedCallback(Callback selectionChangedCallback);

	APITableColumnConfig getConfig();

	boolean isSelected();

	void setSelected(boolean selected);
}
