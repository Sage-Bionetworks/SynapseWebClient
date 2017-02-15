package org.sagebionetworks.web.client.widget.pagination;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Contract between the BasicPagination view and presenter.
 * @author John
 *
 */
public interface BasicPaginationView extends IsWidget{
	
	public interface Presenter{
		
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
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Enable/disable the next button.
	 * 
	 * @param enabled
	 */
	public void setNextEnabled(boolean enabled);
	
	/**
	 * Enable/disable the previous button.
	 * @param enabled
	 */
	public void setPreviousEnabled(boolean enabled);
	
	/**
	 * Set the page numbers.
	 * @param currentPageNumber
	 */
	public void setCurrentPage(long currentPageNumber);

}
