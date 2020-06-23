package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.Widget;

public interface EditJSONListModalView {

	void showError(String message);

	void addCommaSeparatedValuesParser(Widget asWidget);

	void removeCommaSeparatedValuesParser(Widget asWidget);

	public interface Presenter{

		void onSave();

		void onClickPasteNewValues();
	}
}
