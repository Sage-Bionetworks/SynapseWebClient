package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeWidgetViewImpl implements ChallengeWidgetView {
	public interface Binder extends UiBinder<Widget, ChallengeWidgetViewImpl> {
	}

	private Presenter presenter;
	@UiField
	Div teamWidgetContainer;
	@UiField
	Div widgetsContainer;
	@UiField
	Panel challengeUI;
	@UiField
	Heading challengeIdHeading;
	@UiField
	Div submitToChallengeContainer;
	@UiField
	Div selectTeamModalContainer;

	@UiField
	Button editTeamButton;

	Widget widget;

	@Inject
	public ChallengeWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		editTeamButton.addClickHandler(event -> {
			presenter.onEditTeamClicked();
		});
	}

	@Override
	public void setChallengeTeamWidget(Widget w) {
		teamWidgetContainer.clear();
		teamWidgetContainer.add(w);
	}

	@Override
	public void setChallengeVisible(boolean visible) {
		challengeUI.setVisible(visible);
	}

	@Override
	public void setChallengeId(String challengeId) {
		challengeIdHeading.setText(challengeId);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSelectTeamModal(Widget w) {
		selectTeamModalContainer.clear();
		selectTeamModalContainer.add(w);
	}

	@Override
	public void add(Widget w) {
		widgetsContainer.add(w);
	}

	@Override
	public void setSubmitToChallengeWidget(IsWidget submitToChallengeWidget) {
		submitToChallengeContainer.clear();
		submitToChallengeContainer.add(submitToChallengeWidget);
	}
}
