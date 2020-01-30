package org.sagebionetworks.web.client.widget.entity;

public class WidgetSelectionState {
	private boolean isWidgetSelected = false;
	private String innerWidgetText = null;
	private int widgetStartIndex = -1, widgetEndIndex = -1;

	public WidgetSelectionState() {}

	public boolean isWidgetSelected() {
		return isWidgetSelected;
	}

	public void setWidgetSelected(boolean isWidgetSelected) {
		this.isWidgetSelected = isWidgetSelected;
	}

	public String getInnerWidgetText() {
		return innerWidgetText;
	}

	public void setInnerWidgetText(String innerWidgetText) {
		this.innerWidgetText = innerWidgetText;
	}

	public int getWidgetStartIndex() {
		return widgetStartIndex;
	}

	public void setWidgetStartIndex(int widgetStartIndex) {
		this.widgetStartIndex = widgetStartIndex;
	}

	public int getWidgetEndIndex() {
		return widgetEndIndex;
	}

	public void setWidgetEndIndex(int widgetEndIndex) {
		this.widgetEndIndex = widgetEndIndex;
	}


}
