package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a file input view.
 * 
 * @author jhill
 *
 */
public interface FileInputView extends IsWidget {

	/**
	 * Business logic for the file input view.
	 *
	 */
	public interface Presenter {

	}

	/**
	 * Bind the view to the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Get the ID of the file input DOM.
	 * 
	 * @return
	 */
	String getInputId();

	/**
	 * Update the progress.
	 * 
	 * @param currentProgress
	 * @param progressText
	 */
	void updateProgress(double currentProgress, String progressText);

	/**
	 * Show/hide the progress bar.
	 * 
	 * @param b
	 */
	void showProgress(boolean visible);

	/**
	 * Reset the form back to its original state.
	 */
	void resetForm();

	/**
	 * Enable/disable the input box.
	 * 
	 * @param b
	 */
	void setInputEnabled(boolean b);


}
