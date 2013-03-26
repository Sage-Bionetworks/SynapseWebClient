package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityListConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	
	public void configure();
	
	public void setEntityGroupRecordDisplay(int rowIndex,
			EntityGroupRecordDisplay entityGroupRecordDisplay,
			boolean isLoggedIn);

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void addRecord(String entityId, Long versionNumber, String note);
		
		void removeRecord(int row);

		void updateNote(int row, String note);

	}


}
