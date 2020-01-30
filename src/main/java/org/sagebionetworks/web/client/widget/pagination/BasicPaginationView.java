package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Contract between the BasicPagination view and presenter.
 * 
 * @author John
 *
 */
public interface BasicPaginationView extends IsWidget {

	public interface Presenter {

		/**
		 * Called when the next button is pushed
		 */
		public void onNext();

		/**
		 * Called when the previous button is pushed.
		 */
		public void onPrevious();

	}

	/**
	 * Bind this view to its presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Enable/disable the next button.
	 * 
	 * @param enabled
	 */
	public void setNextVisible(boolean visible);

	/**
	 * Enable/disable the previous button.
	 * 
	 * @param enabled
	 */
	public void setPreviousVisible(boolean visible);

	public void setCurrentPage(long currentPageNumber);

	void setVisible(boolean visible);

	public void hideClearFix();
}
