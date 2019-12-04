package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserSelectorView extends IsWidget {

	void show();

	void hide();

	void setSelectBox(Widget w);

	void setPresenter(Presenter p);

	void addModalShownHandler(ModalShownHandler modalShownHandler);

	public interface Presenter {
		void onModalShown();

		void onModalHidden();
	}
}
