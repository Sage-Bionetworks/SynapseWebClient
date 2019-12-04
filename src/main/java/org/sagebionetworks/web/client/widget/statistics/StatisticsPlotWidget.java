package org.sagebionetworks.web.client.widget.statistics;

import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StatisticsPlotWidget {

	private StatisticsPlotWidgetView view;
	private AuthenticationController authController;

	@Inject
	public StatisticsPlotWidget(StatisticsPlotWidgetView view, AuthenticationController authController) {
		this.view = view;
		this.authController = authController;
	}

	public Widget asWidget() {
		view.setVisible(!authController.isLoggedIn());
		return view.asWidget();
	}

	public void configureAndShow(String projectId) {
		if (authController.isLoggedIn()) {
			view.configureAndShow(projectId, authController.getCurrentUserSessionToken());
		}
	}

	public void clear() {
		view.clear();
	}
}
