package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageParamsPanelView extends IsWidget {

	void setNoneButtonActive();

	void setLeftButtonActive();

	void setCenterButtonActive();

	void setRightButtonActive();

	void setPresenter(Presenter presenter);

	void setScale(Integer scale);

	Integer getScale();

	void setAltText(String altText);

	String getAltText();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void noneButtonClicked();

		void leftButtonClicked();

		void centerButtonClicked();

		void rightButtonClicked();
	}
}
