package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A view of the CSV options used for CSV file upload.
 * 
 * @author jhill
 *
 */
public interface CSVOptionsView extends IsWidget {

	public interface Presenter {
		/**
		 * Called when the separator option changes.
		 */
		void onSeparatorChanged();

		/**
		 * Called when the escape character option changes.
		 */
		void onEscapeCharacterChanged();

		/**
		 * Refresh the preview upon request.
		 */
		void onRefreshPreview();
	}

	/**
	 * Comma, tab, or other.
	 * 
	 * @param delimiter
	 */
	void setSeparator(Delimiter delimiter);

	/**
	 * Backslash, or other.
	 * 
	 * @param delimiter
	 */
	void setEscapeCharacter(EscapeCharacter character);

	/**
	 * For the case of other, the text of the separator.
	 * 
	 * @param separator
	 */
	void setOtherSeparatorValue(String separator);

	/**
	 * For the case of other escape character, the text of the character.
	 * 
	 * @param separator
	 */
	void setOtherEscapeCharacterValue(String character);

	/**
	 * Enable/disable the text box for the other value.
	 * 
	 * @param enabled
	 */
	void setOtherSeparatorTextEnabled(boolean enabled);

	/**
	 * Enable/disable the text box for the other value.
	 * 
	 * @param enabled
	 */
	void setOtherEscapeCharacterTextEnabled(boolean enabled);


	/**
	 * Clear the text in the other separtor box.
	 */
	void clearOtherSeparatorText();

	/**
	 * Clear the text in the other separtor box.
	 */
	void clearOtherEscapeCharacterText();

	/**
	 * Comma, tab, or other.
	 * 
	 * @return
	 */
	Delimiter getSeparator();

	/**
	 * Backslash, or other.
	 * 
	 * @return
	 */
	EscapeCharacter getEscapeCharacter();

	/**
	 * For the case of other, the text of the separator.
	 * 
	 * @return
	 */
	String getOtherSeparatorValue();

	/**
	 * For the case of other, the text of the escape character.
	 * 
	 * @return
	 */
	String getOtherEscapeCharacterValue();

	/**
	 * Bind the presenter to the view.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Is the first line the header.
	 * 
	 * @param isFirstLineHeader
	 */
	void setFirsLineIsHeader(boolean isFirstLineHeader);

	/**
	 * Is the first line the header
	 * 
	 * @return
	 */
	boolean getIsFristLineHeader();

}
