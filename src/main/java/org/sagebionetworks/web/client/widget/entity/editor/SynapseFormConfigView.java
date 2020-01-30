package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseFormConfigView extends IsWidget, WidgetEditorView {

	void setPresenter(Presenter presenter);

	void setEntityId(String entityId);

	String getEntityId();

	public interface Presenter {
		void onEntityFinderButtonClicked();
	}
}
