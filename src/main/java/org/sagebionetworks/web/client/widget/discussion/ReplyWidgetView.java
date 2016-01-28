package org.sagebionetworks.web.client.widget.discussion;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ReplyWidgetView extends IsWidget{

	public interface Presenter {

		Widget asWidget();
	}

	void setPresenter(ReplyWidget presenter);

	void setAuthor(Widget widget);

	void setCreatedOn(String createdOn);

	void setMessage(String message);

	void clear();

	void setAlert(Widget w);

	void setDeleteButtonVisibility(Boolean visible);
}
