package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;

public interface DockerTabView extends IsWidget{

	public interface Presenter {

	}

	void setPresenter(Presenter presenter);
}
