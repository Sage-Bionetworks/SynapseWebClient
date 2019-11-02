package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Contract between the AsynchronousProgressView and its presenter.
 * 
 * @author John
 *
 */
public interface AsynchronousProgressView extends IsWidget {

	public interface Presenter {
	}

	/**
	 * Bind the view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Set the title of the progress message
	 * 
	 * @param title
	 */
	public void setTitle(String title);

	/**
	 * Is this determinate or indeterminate.
	 * 
	 * @param isDeterminate
	 */
	public void setIsDetermiante(boolean isDeterminate);

	/**
	 * Set the determinate progress data
	 * 
	 * @param percent
	 * @param text
	 */
	public void setDeterminateProgress(double percent, String text, String message);

	/**
	 * Set the indeterminate progress data
	 * 
	 * @param text
	 * @param message
	 */
	public void setIndetermianteProgress(String message);

	/**
	 * Is this view still attached to the UI.
	 * 
	 * @return
	 */
	public boolean isAttached();

	public void showWhiteSpinner();

	public void setProgressMessageVisible(boolean visible);
}
