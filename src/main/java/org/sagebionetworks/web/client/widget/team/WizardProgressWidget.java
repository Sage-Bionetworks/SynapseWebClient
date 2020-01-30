package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WizardProgressWidget implements WizardProgressWidgetView.Presenter, IsWidget {
	private WizardProgressWidgetView view;

	@Inject
	public WizardProgressWidget(WizardProgressWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}

	/**
	 * @param current 0-based index
	 * @param total count of total
	 */
	public void configure(int current, int total) {
		view.configure(current, total);
	};


	public void clear() {
		view.clear();
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
