package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

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
	Anchor changeLink;
	@UiField
	Span flagUI;
	@UiField
	Anchor reportIssueLink;
	@UiField
	Anchor folderViewTermsLink;

	@UiField
	Div folderRestrictionUI;
	@UiField
	Paragraph folderRestrictedMessage;
	@UiField
	Paragraph folderUnrestrictedMessage;
	@UiField
	Span modalsContainer;
	@UiField
	Div hasAccessContainerParent;
	@UiField
	ReactComponentDiv hasAccessContainer;
	
	Presenter presenter;
	// this UI widget
	Widget widget;
	RestrictionWidgetModalsViewImpl modals;
	SynapseContextPropsProvider propsProvider;
	@Inject
	public RestrictionWidgetViewImpl(Binder binder,
			RestrictionWidgetModalsViewImpl modals,
            SynapseContextPropsProvider propsProvider) {
		this.widget = binder.createAndBindUi(this);
		this.modals = modals;
		this.propsProvider = propsProvider;
		modalsContainer.add(modals);

		changeLink.addClickHandler(event -> {
				presenter.changeClicked();
		});
		reportIssueLink.addClickHandler(event -> {
			presenter.reportIssueClicked();
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
	public void showFolderRestrictionsLink(String entityId) {
		folderViewTermsLink.setVisible(true);
		folderViewTermsLink.setHref("#!AccessRequirements:TYPE=ENTITY&ID="+entityId);
		folderViewTermsLink.setTarget("_blank");
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
	public void configureCurrentAccessComponent(String entityId, Long versionNumber) {
		String versionNumberString = versionNumber == null ? null : versionNumber.toString();
		// SWC-5821: force remount
		hasAccessContainer.removeFromParent();
		hasAccessContainer.clear();
		hasAccessContainerParent.add(hasAccessContainer);
		_showHasAccess(hasAccessContainer.getElement(), entityId, versionNumberString, propsProvider.getJsniContextProps());
	}
	
	private static native void _showHasAccess(Element el, String synapseEntityId, String versionNumber, SynapseContextProviderPropsJSNIObject wrapperProps) /*-{
		try {
			var props = {
				entityId: synapseEntityId,
				entityVersionNumber: versionNumber
			};
			var component = $wnd.React.createElement($wnd.SRC.SynapseComponents.HasAccess, props, null)
			var wrapper = $wnd.React.createElement($wnd.SRC.SynapseContext.SynapseContextProvider, wrapperProps, component)

			$wnd.ReactDOM.render(wrapper, el);
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
