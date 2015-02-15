package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;

public interface ImageParamsPanelView extends IsWidget {

	void setNoneButtonActive();
	void setLeftButtonActive();
	void setCenterButtonActive();
	void setRightButtonActive();
	void setPresenter(Presenter presenter);
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
