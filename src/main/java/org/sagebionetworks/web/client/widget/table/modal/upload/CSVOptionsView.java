package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;

public interface CSVOptionsView extends IsWidget {

	void setSeparator(Delimiter delimiter);

	void setOtherSeparatorValue(String separator);

	Delimiter getSeparator();

	String getOtherSeparatorValue();
}
