package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView.Presenter;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * A view of the CSV options used for CSV file upload.
 * 
 * @author jhill
 *
 */
public interface CSVOptionsView extends IsWidget {
	
	public interface Presenter{
		/**
		 * Called when the separator option changes.
		 */
		void onSeparatorChanged();

		/**
		 * Refresh the preview upon request.
		 */
		void onRefreshPreview();
	}

	/**
	 * Comma, tab, or other.
	 * @param delimiter
	 */
	void setSeparator(Delimiter delimiter);

	/**
	 * For the case of other, the text of the separator.
	 * @param separator
	 */
	void setOtherSeparatorValue(String separator);
	
	/**
	 * Enable/disable the text box for the other value.
	 * @param enabled
	 */
	void setOtherSeparatorTextEnabled(boolean enabled);
	
	/**
	 * Clear the text in the other separtor box.
	 */
	void clearOtherSeparatorText();

	/**
	 * Comma, tab, or other.
	 * @return
	 */
	Delimiter getSeparator();

	/**
	 * For the case of other, the text of the separator.
	 * @return
	 */
	String getOtherSeparatorValue();
	
	/**
	 * Bind the presenter to the view.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Is the first line the header.
	 * @param isFirstLineHeader
	 */
	void setFirsLineIsHeader(boolean isFirstLineHeader);

	/**
	 * Is the first line the header
	 * @return
	 */
	boolean getIsFristLineHeader();

}
