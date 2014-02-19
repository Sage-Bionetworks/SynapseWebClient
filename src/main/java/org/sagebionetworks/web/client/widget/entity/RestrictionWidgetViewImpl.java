package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {
	
	IconsImageBundle iconsImageBundle;
	SynapseJSNIUtils synapseJSNIUtils;
	Presenter presenter;
	
	@Inject
	public RestrictionWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter=presenter;
	}

	@Override
	public Widget asWidget(String jiraFlagLink,
			boolean isAnonymous, 
			boolean hasAdministrativeAccess,
			Callback loginCallback, 
			RESTRICTION_LEVEL restrictionLevel,
			ClickHandler aboutLinkClickHandler,
			boolean showFlagLink, 
			boolean showChangeLink) {

		return EntityViewUtils.createRestrictionsWidget(
				jiraFlagLink,
				isAnonymous,
				hasAdministrativeAccess,
				loginCallback,
				restrictionLevel,
				iconsImageBundle,
				synapseJSNIUtils,
				aboutLinkClickHandler,
				showFlagLink,
				showChangeLink);
	}
	
	@Override
	public void showAccessRequirement(RESTRICTION_LEVEL restrictionLevel,
			APPROVAL_TYPE approvalType, 
			boolean isAnonymous,
			boolean hasAdministrativeAccess,
			boolean hasFulfilledAccessRequirements,
			IconsImageBundle iconsImageBundle, 
			String accessRequirementText,
			Callback imposeRestrictionsCallback,
			Callback touAcceptanceCallback, 
			Callback requestACTCallback,
			Callback loginCallback, 
			String jiraFlagLink, 
			Callback onHideCallback) {
		GovernanceDialogHelper.showAccessRequirement(
				restrictionLevel,
				approvalType,
				isAnonymous,
				hasAdministrativeAccess,
				hasFulfilledAccessRequirements,
				iconsImageBundle,
				accessRequirementText,
				imposeRestrictionsCallback,
				touAcceptanceCallback,
				requestACTCallback,
				loginCallback,
				jiraFlagLink, 
				onHideCallback);	
	}
	
	@Override
	public void showVerifyDataSensitiveDialog(
			final Callback imposeRestrictionsCallback) {
		
		final Dialog window = new Dialog();
		window.setPlain(true);
		window.setModal(true);
		window.setHeaderVisible(true);
		InlineHTML question = new InlineHTML(DisplayConstants.IS_SENSITIVE_DATA_MESSAGE);
		question.addStyleName("margin-left-10");
		window.add(question);
		final RadioButton yesButton = new RadioButton(Dialog.YES);
		yesButton.addStyleName("margin-left-5");
		final RadioButton noButton = new RadioButton(Dialog.NO);
		noButton.addStyleName("margin-left-5");
		window.add(yesButton);
		InlineHTML label = new InlineHTML("Yes");
		label.addStyleName("margin-left-5");
		window.add(label);
		window.add(noButton);
		label = new InlineHTML("No");
		label.addStyleName("margin-left-5");
		window.add(label);
		
		
		window.setSize(430, 100);
		// configure buttons
	    window.setButtons(Dialog.OKCANCEL);
	    window.setButtonAlign(HorizontalAlignment.RIGHT);
	    window.setHideOnButtonClick(false);
		window.setResizable(true);
		
		//when yes is clicked, hide DisplayConstants.IS_SENSITIVE_DATA_CONTACT_ACT_MESSAGE.  when no is clicked, show DisplayConstants.IS_SENSITIVE_DATA_CONTACT_ACT_MESSAGE
		final FlowPanel messageContainer = new FlowPanel();
		messageContainer.addStyleName("margin-top-10 margin-left-15 margin-right-15");
		final HTML message = new HTML(DisplayConstants.IS_SENSITIVE_DATA_CONTACT_ACT_MESSAGE);
		
		window.add(messageContainer);
		
		yesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				noButton.setValue(false);
				messageContainer.clear();
				window.setHeight(100);
				window.layout(true);
			}
		});
		
		noButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				yesButton.setValue(false);
				if (messageContainer.getWidgetCount() == 0) {
					messageContainer.add(message);
					window.setHeight(200);
					window.layout(true);
				}
			}
		});
		//define button listeners.		
		final Button okButton = window.getButtonById(Dialog.OK);
		okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (yesButton.getValue()) {
					window.hide();
					imposeRestrictionsCallback.invoke();
				} else if (noButton.getValue()) {
					window.hide();
				} else {
					//no selection
					DisplayUtils.showErrorMessage("You must make a selection before continuing.");
				}
			}
		});
		
		Button cancelButton = window.getButtonById(Dialog.CANCEL);
		cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.hide();
			}
		});
		window.show();
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
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public Widget asWidget() {
		return null;
	}
	/*
	 * Private Methods
	 */

}
