package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsView;
import org.sagebionetworks.web.client.widget.table.modal.upload.Delimiter;

import com.google.gwt.user.client.ui.Widget;

public class CSVOptionsViewStub implements CSVOptionsView {

	Delimiter separator;
	String otherSeparatorValue;
	public Delimiter getSeparator() {
		return separator;
	}
	public void setSeparator(Delimiter separator) {
		this.separator = separator;
	}
	public String getOtherSeparatorValue() {
		return otherSeparatorValue;
	}
	public void setOtherSeparatorValue(String otherSeparatorValue) {
		this.otherSeparatorValue = otherSeparatorValue;
	}
	
	@Override
	public Widget asWidget() {
		return null;
	}
	
	
}
