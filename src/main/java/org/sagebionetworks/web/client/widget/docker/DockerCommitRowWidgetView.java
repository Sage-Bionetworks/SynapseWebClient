package org.sagebionetworks.web.client.widget.docker;

import java.util.Date;
import com.google.gwt.user.client.ui.IsWidget;

public interface DockerCommitRowWidgetView extends IsWidget {

	public interface Presenter {

		void onClick();
	}

	void setPresenter(Presenter dockerCommitRowWidget);

	void setTag(String tag);

	void setCreatedOn(Date date);

	void setDigest(String widget);
}
