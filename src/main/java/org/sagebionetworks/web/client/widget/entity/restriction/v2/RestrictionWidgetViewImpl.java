package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {

	public interface Binder extends UiBinder<Widget, RestrictionWidgetViewImpl> {
	}

	@UiField
	Span synAlertContainer;

	@UiField
	Span linkUI;

	@UiField
	Button changeLink;
	
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
	@UiField
	Span hasAccessContainer;
	
	Presenter presenter;
	// this UI widget
	Widget widget;
	RestrictionWidgetModalsViewImpl modals;
	AuthenticationController authController;
	@Inject
	public RestrictionWidgetViewImpl(Binder binder,
			RestrictionWidgetModalsViewImpl modals,
			PortalGinInjector ginInjector,
			AuthenticationController authController) {
		this.widget = binder.createAndBindUi(this);
		this.modals = modals;
		this.authController = authController;
		modalsContainer.add(modals);

		changeLink.addClickHandler(event -> {
				presenter.changeClicked();
		});
		reportIssueLink.addClickHandler(event -> {
			presenter.reportIssueClicked();
		});
		widget.addAttachHandler(event -> {
			if (!event.isAttached()) {
				// detach event, clean up react component
				ginInjector.getSynapseJSNIUtils().unmountComponentAtNode(hasAccessContainer.getElement());
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showControlledUseUI() {
		folderRestrictedMessage.setVisible(true);
		folderUnrestrictedMessage.setVisible(false);
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
	public void showNoRestrictionsUI() {
		folderRestrictedMessage.setVisible(false);
		folderUnrestrictedMessage.setVisible(true);
	}

	@Override
	public void clear() {
		linkUI.setVisible(false);
		flagUI.setVisible(false);
		changeLink.setVisible(false);
		modals.resetImposeRestrictionModal();
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
	
	@Override
	public void setEntityId(String entityId) {
		String sessionToken = authController.getCurrentUserSessionToken();
		_showHasAccess(hasAccessContainer.getElement(), entityId, sessionToken);
	}
	private static native void _showHasAccess(Element el, String entityId, String sessionToken) /*-{
		try {
			var props = {
				synapseId: entityId,
				token: sessionToken
			};
	
			$wnd.ReactDOM.render($wnd.React.createElement(
					$wnd.SRC.SynapseComponents.HasAccess, props, null),
					el);
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
