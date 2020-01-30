package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {

	public interface Binder extends UiBinder<Widget, RestrictionWidgetViewImpl> {
	}

	@UiField
	LoadingSpinner loadingUI;
	@UiField
	Span controlledUseUI;
	@UiField
	Image unmetRequirementsIcon;
	@UiField
	Image metRequirementsIcon;
	@UiField
	Span synAlertContainer;

	@UiField
	Span noneUI;

	@UiField
	Span linkUI;

	@UiField
	Button changeLink;
	@UiField
	Button showLink;
	@UiField
	Button showUnmetLink;

	@UiField
	Span flagUI;
	@UiField
	Anchor reportIssueLink;

	@UiField
	Div folderRestrictionUI;
	@UiField
	Paragraph folderRestrictedMessage;
	@UiField
	Paragraph folderUnrestrictedMessage;
	@UiField
	Span modalsContainer;

	Presenter presenter;
	// this UI widget
	Widget widget;
	RestrictionWidgetModalsViewImpl modals;

	@Inject
	public RestrictionWidgetViewImpl(Binder binder, RestrictionWidgetModalsViewImpl modals) {
		this.widget = binder.createAndBindUi(this);
		this.modals = modals;
		modalsContainer.add(modals);

		changeLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.changeClicked();
			}
		});

		showLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.linkClicked();
			}
		});

		showUnmetLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.linkClicked();
			}
		});

		reportIssueLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.reportIssueClicked();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		modals.setPresenter(presenter);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public void showVerifyDataSensitiveDialog() {
		modals.resetImposeRestrictionModal();
		modals.lazyConstruct();
		modals.imposeRestrictionModal.show();
	}

	@Override
	public void open(String url) {
		Window.open(url, "_blank", "");
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
		folderRestrictedMessage.setVisible(true);
		folderUnrestrictedMessage.setVisible(false);
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
	public void showChangeLink() {
		linkUI.setVisible(true);
		changeLink.setVisible(true);
	}

	@Override
	public void showShowLink() {
		linkUI.setVisible(true);
		showLink.setVisible(true);
	}

	@Override
	public void showShowUnmetLink() {
		linkUI.setVisible(true);
		showUnmetLink.setVisible(true);
	}

	@Override
	public void showNoRestrictionsUI() {
		noneUI.setVisible(true);
		folderRestrictedMessage.setVisible(false);
		folderUnrestrictedMessage.setVisible(true);
	}

	@Override
	public void clear() {
		loadingUI.setVisible(false);
		controlledUseUI.setVisible(false);
		noneUI.setVisible(false);
		linkUI.setVisible(false);
		flagUI.setVisible(false);
		showLink.setVisible(false);
		showUnmetLink.setVisible(false);
		changeLink.setVisible(false);
		modals.resetImposeRestrictionModal();
		unmetRequirementsIcon.setVisible(false);
		metRequirementsIcon.setVisible(false);
	}

	@Override
	public void setNotSensitiveHumanDataMessageVisible(boolean visible) {
		modals.lazyConstruct();
		modals.notSensitiveHumanDataMessage.setVisible(visible);
	}

	@Override
	public Boolean isNoHumanDataRadioSelected() {
		modals.lazyConstruct();
		return modals.noHumanDataRadio.getValue();
	}

	@Override
	public Boolean isYesHumanDataRadioSelected() {
		modals.lazyConstruct();
		return modals.yesHumanDataRadio.getValue();
	}

	@Override
	public void setImposeRestrictionModalVisible(boolean visible) {
		modals.lazyConstruct();
		if (visible) {
			modals.imposeRestrictionModal.show();
		} else {
			modals.imposeRestrictionModal.hide();
		}
	}

	@Override
	public void showFolderRestrictionUI() {
		folderRestrictionUI.setVisible(true);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
