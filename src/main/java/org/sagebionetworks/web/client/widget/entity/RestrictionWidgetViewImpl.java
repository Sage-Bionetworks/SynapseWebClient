package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {
	
	public interface Binder extends UiBinder<Widget, RestrictionWidgetViewImpl> {}
	
	@UiField
	Div loadingUI;
	
	@UiField
	Span controlledUseUI;
	@UiField
	Image unmetRequirementsIcon;
	@UiField
	Image metRequirementsIcon;
	
	@UiField
	Span noneUI;
	
	@UiField
	Span linkUI;
	
	@UiField
	Anchor changeLink;
	@UiField
	Anchor showLink;
	
	@UiField
	Span flagUI;
	@UiField
	Anchor reportIssueLink;
	
	@UiField
	Span anonymousFlagUI;
	@UiField
	Anchor anonymousReportIssueLink;

	
	@UiField
	Modal imposeRestrictionModal;
	@UiField
	InlineRadio yesHumanDataRadio;
	@UiField
	InlineRadio noHumanDataRadio;
	@UiField
	Alert notSensitiveHumanDataMessage;
	
	@UiField
	Button imposeRestrictionOkButton;
	@UiField
	Button imposeRestrictionCancelButton;
	
	@UiField
	Modal flagModal;
	@UiField
	Button flagModalOkButton;
	
	@UiField
	Modal anonymousFlagModal;
	@UiField
	Button anonymousFlagModalOkButton;
	
	@UiField
	Span accessRestrictionDialogContainer;
	
	Presenter presenter;
	
	//this UI widget
	Widget widget;
	
	private ClickHandler changeLinkClickHandler, showLinkClickHandler;
	
	
	@Inject
	public RestrictionWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		yesHumanDataRadio.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.yesHumanDataClicked();
			}
		});
		noHumanDataRadio.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.notHumanDataClicked();
			}
		});

		imposeRestrictionOkButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.imposeRestrictionOkClicked();
			}
		});

		imposeRestrictionCancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.imposeRestrictionCancelClicked();
			}
		});

		changeLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeLinkClickHandler.onClick(event);
			}
		});
		
		showLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showLinkClickHandler.onClick(event);
			}
		});
		
		flagModalOkButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.flagData();
			}
		});
		
		anonymousFlagModalOkButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.anonymousFlagModalOkClicked();
			}
		});
		
		reportIssueLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.reportIssueClicked();
			}
		});
		
		anonymousReportIssueLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.anonymousReportIssueClicked();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter=presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void showVerifyDataSensitiveDialog() {
		resetImposeRestrictionModal();
		imposeRestrictionModal.show();
	}
	
	@Override
	public void open(String url) {
		Window.open(url, "_blank", "");	
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}
	@Override
	public void showControlledUseUI() {
		controlledUseUI.setVisible(true);
	}
	
	@Override
	public void showUnmetRequirementsIcon() {
		unmetRequirementsIcon.setVisible(true);
	}
	
	@Override
	public void showMetRequirementsIcon() {
		metRequirementsIcon.setVisible(true);
	}


	@Override
	public void showFlagUI() {
		flagUI.setVisible(true);
	}
	@Override
	public void showAnonymousFlagUI() {
		anonymousFlagUI.setVisible(true);
	}
	
	@Override
	public void showChangeLink(ClickHandler changeLinkClickHandler) {
		linkUI.setVisible(true);
		changeLink.setVisible(true);
		this.changeLinkClickHandler = changeLinkClickHandler;
	}
	@Override
	public void showShowLink(ClickHandler showLinkClickHandler) {
		linkUI.setVisible(true);
		showLink.setVisible(true);
		this.showLinkClickHandler = showLinkClickHandler;
	}
	@Override
	public void showNoRestrictionsUI() {
		noneUI.setVisible(true);
	}
	
	@Override
	public void clear() {
		loadingUI.setVisible(false);
		controlledUseUI.setVisible(false);
		noneUI.setVisible(false);
		linkUI.setVisible(false);
		flagUI.setVisible(false);
		anonymousFlagUI.setVisible(false);
		showLink.setVisible(false);
		changeLink.setVisible(false);
		showLinkClickHandler = null;
		changeLinkClickHandler = null;
		resetImposeRestrictionModal();
		unmetRequirementsIcon.setVisible(false);
		metRequirementsIcon.setVisible(false);
	}
	
	private void resetImposeRestrictionModal() {
		yesHumanDataRadio.setValue(false);
		noHumanDataRadio.setValue(false);
		notSensitiveHumanDataMessage.setVisible(false);
		imposeRestrictionOkButton.setEnabled(true);
	}
	
	@Override
	public void showFlagModal() {
		flagModal.show();
	}
	
	@Override
	public void showAnonymousFlagModal() {
		anonymousFlagModal.show();
	}
	
	@Override
	public void setAccessRequirementDialog(Widget dialog) {
		accessRestrictionDialogContainer.clear();
		accessRestrictionDialogContainer.add(dialog);
	}
	
	@Override
	public void setImposeRestrictionOkButtonEnabled(boolean enable) {
		imposeRestrictionOkButton.setEnabled(enable);
	}
	
	@Override
	public void setNotSensitiveHumanDataMessageVisible(boolean visible) {
		notSensitiveHumanDataMessage.setVisible(visible);
	}
	
	@Override
	public Boolean isNoHumanDataRadioSelected() {
		return noHumanDataRadio.getValue();
	}
	
	@Override
	public Boolean isYesHumanDataRadioSelected() {
		return yesHumanDataRadio.getValue();
	}

	@Override
	public void setImposeRestrictionModalVisible(boolean visible) {
		if (visible) {
			imposeRestrictionModal.show();
		} else {
			imposeRestrictionModal.hide();
		}
	}

	/*
	 * Private Methods
	 */
}
