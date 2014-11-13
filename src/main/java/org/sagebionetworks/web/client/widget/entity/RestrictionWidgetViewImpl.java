package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {
	
	public interface Binder extends UiBinder<Widget, RestrictionWidgetViewImpl> {}
	
	@UiField
	Div loadingUI;
	
	@UiField
	Span controlledUseUI;
	
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
	Modal flagModal;
	@UiField
	Button flagModalOkButton;
	
	@UiField
	Modal anonymousFlagModal;
	@UiField
	Button anonymousFlagModalOkButton;
	
	@UiField
	Div accessRestrictionDialogContainer;
	
	Presenter presenter;
	
	//this UI widget
	Widget widget;
	private Callback imposeRestrictionsCallback;
	private ClickHandler changeLinkClickHandler, showLinkClickHandler;
	
	
	@Inject
	public RestrictionWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		yesHumanDataRadio.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				notSensitiveHumanDataMessage.setVisible(false);
			}
		});
		noHumanDataRadio.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				notSensitiveHumanDataMessage.setVisible(true);
			}
		});

		imposeRestrictionOkButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (yesHumanDataRadio.getValue()) {
					imposeRestrictionModal.hide();
					imposeRestrictionsCallback.invoke();
				} else if (noHumanDataRadio.getValue()) {
					imposeRestrictionModal.hide();
				} else {
					//no selection
					DisplayUtils.showErrorMessage("You must make a selection before continuing.");
				}
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
	public void showVerifyDataSensitiveDialog(
			final Callback imposeRestrictionsCallback) {
		this.imposeRestrictionsCallback = imposeRestrictionsCallback;
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
	}
	
	private void resetImposeRestrictionModal() {
		yesHumanDataRadio.setValue(false);
		noHumanDataRadio.setValue(false);
		notSensitiveHumanDataMessage.setVisible(false);
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
	/*
	 * Private Methods
	 */

}
