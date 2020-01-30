package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.SelectableListView;
import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityListConfigView extends IsWidget, WidgetEditorView, SelectableListView {

	void setPresenter(Presenter presenter);

	void setEntityListWidget(Widget w);

	void setCanEditNote(boolean canEditNote);

	void setButtonToolbarVisible(boolean visible);

	void addWidget(Widget w);

	public interface Presenter {
		void onAddRecord();

		void onUpdateNote();
	}
}
