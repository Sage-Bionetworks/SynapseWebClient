package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerTabView extends IsWidget{

	public interface Presenter {

	}

	void setPresenter(Presenter presenter);
	void setTitlebar(Widget widget);
	void setDockerList(Widget widget);
	void setBreadcrumb(Widget widget);
	void setEntityMetadata(Widget widget);
	void setSynapseAlert(Widget widget);
	void setModifiedCreatedBy(IsWidget widget);
}
