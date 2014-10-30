package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;

public interface AccessControlListModalWidgetView extends IsWidget {

	void showDialog();

	void setDefaultButtonText(String string);

	void setPrimaryButtonVisible(boolean b);

	void setPrimaryButtonEnabled(boolean b);

	void addEditor(IsWidget editor);

}
