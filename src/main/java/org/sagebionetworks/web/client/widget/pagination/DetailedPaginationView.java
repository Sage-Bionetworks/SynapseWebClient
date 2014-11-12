package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.IsWidget;

public interface DetailedPaginationView extends IsWidget {
	
	/**
	 * Business logic for this class.
	 */
	public interface Presenter {
		/**
		 * User clicked a button.
		 * @param clickedOffset The offset to go to when clicked.
		 */
		public void onPageChange(long clickedOffset);
		
	}
	
	/**
	 * Add a button to the pager.
	 * @param count
	 */
	void addButton(Long offsetIfClicked, String text, boolean isActive);

	/**
	 * Show/hide the pager.
	 * @param visibile
	 */
	void setPagerVisible(boolean visible);
	
	/**
	 * Bind this view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Remove all of the buttons
	 */
	void removeAllButtons();
}
