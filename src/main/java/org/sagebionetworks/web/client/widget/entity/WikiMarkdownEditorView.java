package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface WikiMarkdownEditorView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void setSaving(boolean isSaving);

	void setTitleEditorVisible(boolean visible);

	String getTitle();

	void setTitle(String title);

	void showEditorModal();

	void hideEditorModal();

	void setDeleteButtonVisible(boolean visible);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void cancelClicked();

		void deleteClicked();

		void saveClicked();
	}

	void setMarkdownEditorWidget(Widget markdownEditorWidget);
}
