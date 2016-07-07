package org.sagebionetworks.web.client.view.users;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RegisterAccountView extends IsWidget {
	
	void setRegisterWidget(Widget w);
	void setHeaderWidget(Widget w);
	void setFooterWidget(Widget w);
	public interface Presenter {	
	}
}
