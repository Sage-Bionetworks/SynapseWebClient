package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserSelectorView extends IsWidget {
	
	void show();
	void hide();
	void setSelectBox(Widget w);
	void setSynAlert(Widget w);
	void setPresenter(Presenter p);
	public interface Presenter {
		void onModalShown();
	}
}
