package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidgetView.Presenter;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizInfoWidget implements Presenter, SynapseWidgetPresenter {
	private QuizInfoWidgetView view;

	@Inject
	public QuizInfoWidget(QuizInfoWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure() {
		view.configure();
	}
}
