package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerCommitListWidgetView extends IsWidget {

	public interface Presenter {
	}

	void setCommitsContainer(IsWidget widget);

	void setPresenter(Presenter presenter);

	void setSynAlert(Widget widget);

}
