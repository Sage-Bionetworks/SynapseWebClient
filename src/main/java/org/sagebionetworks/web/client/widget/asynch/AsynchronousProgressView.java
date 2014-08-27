package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Contract between the AsynchronousProgressView and its presenter.
 * 
 * @author John
 *
 */
public interface AsynchronousProgressView extends IsWidget{

	public interface Presenter {
		/**
		 * Called when the user clicks cancel.
		 */
		void onCancel();
		
	}
	
	/**
	 * Bind the view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Set the title of the progress message
	 * @param title
	 */
	public void setTitle(String title);

	/**
	 * Set the progress data
	 * @param percent
	 * @param text
	 */
	public void setProgress(double percent, String text, String toolTips);
}
