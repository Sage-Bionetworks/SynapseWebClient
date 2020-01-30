package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A simple model dialog to ask for reasons specifying why a user was rejected
 *
 */
public interface RejectReasonView extends IsWidget {

	/**
	 * @return String representation of rejected reason
	 */
	String getValue();

	String getSelectedCheckboxText();

	/**
	 * Show an error message..
	 * 
	 * @param error
	 */
	void showError(String error);

	void setValue(String value);

	/**
	 * Show the view
	 */
	void show();

	/**
	 * Hide the dialog.
	 */
	void hide();

	/**
	 * Clear name and errors.
	 */
	void clear();

	/**
	 * clear error state
	 */
	void clearError();

	/**
	 * Set Presenter
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void updateResponse();

		void onSave();
	}
}
