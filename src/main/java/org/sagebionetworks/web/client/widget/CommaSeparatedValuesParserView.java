package org.sagebionetworks.web.client.widget;

import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface CommaSeparatedValuesParserView extends IsWidget {
	public interface Presenter {
		void configure(Consumer<List<String>> onAddCallback, Consumer<CommaSeparatedValuesParser> onCancelCallback);
		void onCancel();
		void onAdd();

		List<String> parseToStringList();

		Widget asWidget();
	}

	String getText();

	/**
	 * Clears the text box
	 */
	void clearTextBox();

	void setPresenter(Presenter presenter);
}
