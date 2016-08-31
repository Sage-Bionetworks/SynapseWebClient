package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeWidgetViewImpl implements ChallengeWidgetView {
	public interface Binder extends UiBinder<Widget, ChallengeWidgetViewImpl> {}
	
	private Presenter presenter;
	@UiField
	Div teamWidgetContainer;
	@UiField
	Div widgetsContainer;
	@UiField
	Button newChallengeButton;
	@UiField
	Button deleteChallengeButton;
	@UiField
	Panel challengeUI;
	@UiField
	Heading challengeIdHeading;
	@UiField
	Panel newChallengeUI;
	
	@UiField
	Button challengeTeamButton;
	@UiField
	Div teamSelectBoxContainer;
	
	@UiField
	Modal teamModal;
	@UiField
	Button editTeamButton;
	
	Widget widget;
	@Inject
	public ChallengeWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newChallengeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCreateChallengeClicked();
			}
		});
		deleteChallengeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog("Delete Challenge?", DisplayConstants.CONFIRM_DELETE_CHALLENGE, 
					new Callback() {
						@Override
						public void invoke() {
							presenter.onDeleteChallengeClicked();
						}
					});
			}
		});
		challengeTeamButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectChallengeTeam();
			}
		});
		editTeamButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditTeamClicked();
			}
		});
	}
	
	@Override
	public void setChallengeTeamWidget(Widget w) {
		teamWidgetContainer.clear();
		teamWidgetContainer.add(w);
	}
	
	@Override
	public void setCreateChallengeVisible(boolean visible) {
		newChallengeUI.setVisible(visible);
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
	public void setSuggestWidget(Widget w) {
		teamSelectBoxContainer.clear();
		teamSelectBoxContainer.add(w);
	}
	@Override
	public void add(Widget w) {
		widgetsContainer.add(w);
	}
	
	@Override
	public void showTeamSelectionModal() {
		teamModal.show();
	}
}
