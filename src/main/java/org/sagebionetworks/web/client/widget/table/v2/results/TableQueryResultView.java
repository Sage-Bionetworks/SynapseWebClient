package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TableQueryResultView extends IsWidget {

	public interface Presenter {

		/**
		 * Called when the user selected the edit row button.
		 */
		void onEditRows();

	}

	/**
	 * Bind the view to the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Bind the page widget to this view.
	 * 
	 * @param pageWidget
	 */
	void setPageWidget(TablePageWidget pageWidget);

	/**
	 * Add the service error widget to the view.
	 * 
	 * @param w
	 */
	void setSynapseAlertWidget(Widget w);

	/**
	 * Show or hide the error alert.
	 * 
	 * @param b
	 */
	void setErrorVisible(boolean visible);

	/**
	 * Set the editor widget
	 * 
	 * @param queryResultEditor
	 */
	void setEditorWidget(QueryResultEditorWidget queryResultEditor);

	/**
	 * The progress widget shows query progress.s
	 * 
	 * @param progressWidget
	 */
	void setProgressWidget(JobTrackingWidget progressWidget);

	/**
	 * Show or hide the progress widget.
	 * 
	 * @param visible
	 */
	void setProgressWidgetVisible(boolean visible);

	void setFacetsWidget(IsWidget w);

	void setFacetsVisible(boolean visible);

	void scrollTableIntoView();

}
