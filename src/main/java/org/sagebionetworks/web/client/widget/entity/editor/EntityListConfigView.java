package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityListConfigView extends IsWidget, WidgetEditorView {

	void setPresenter(Presenter presenter);
	void setEntityListWidget(Widget w);
	void setCanEditNote(boolean canEditNote);
	//selection toolbar state
	void setCanDelete(boolean canDelete);
	void setCanMoveUp(boolean canMoveUp);
	void setCanMoveDown(boolean canMoveDown);
	void setButtonToolbarVisible(boolean visible);
	void addWidget(Widget w);
	public interface Presenter {
		void onAddRecord();
		void onMoveDown();
		void onMoveUp();
		void deleteSelected();
		void selectNone();
		void selectAll();
		void onUpdateNote();
	}
}
