package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class InviteWidgetViewImpl extends FlowPanel implements InviteWidgetView {
	
	private static final int FIELD_WIDTH = 500;
	
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	private UrlCache urlCache;
	private InviteWidgetView.Presenter presenter;
	private LayoutContainer inviteUIPanel;
	private Button inviteButton;
	private TextArea messageArea;
	private UserGroupSuggestBox peopleSuggestBox;
	
	@Inject
	public InviteWidgetViewImpl(SageImageBundle sageImageBundle,
			UrlCache urlCache, SynapseJSNIUtils synapseJSNIUtils,
			UserGroupSuggestBox peopleSuggestBox) {
		this.sageImageBundle = sageImageBundle;
		this.urlCache = urlCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.peopleSuggestBox = peopleSuggestBox;
	}
	
	@Override
	public void configure() {
		initView();
		clear();
		add(inviteButton);
		add(inviteUIPanel);
		peopleSuggestBox.clear();
		inviteUIPanel.setVisible(false);
	}
	
	private void initView() {
		if (inviteUIPanel == null) {
			inviteUIPanel = new LayoutContainer();
			inviteUIPanel.addStyleName("margin-top-0 highlight-box highlight-line-min");
			inviteButton = DisplayUtils.createIconButton("Invite Members", ButtonType.DEFAULT, "glyphicon-plus");
			inviteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					inviteUIPanel.setVisible(!inviteUIPanel.isVisible());
					inviteUIPanel.layout(true);
				}
			});
			
			// user/group Suggest Box
			peopleSuggestBox.configureURLs(synapseJSNIUtils.getBaseFileHandleUrl(), synapseJSNIUtils.getBaseProfileAttachmentUrl());
			peopleSuggestBox.setPlaceholderText("Enter a user name...");
			peopleSuggestBox.setWidth(FIELD_WIDTH + "px");
			inviteUIPanel.add(peopleSuggestBox.asWidget());
			
			messageArea = new TextArea();
			messageArea.setWidth(FIELD_WIDTH);
			messageArea.setEmptyText("Enter invitation message... (optional)");
			messageArea.addStyleName("margin-top-5");
			inviteUIPanel.add(messageArea);
			
			Button sendInviteButton = DisplayUtils.createButton("Send Invitation");
			sendInviteButton.addStyleName("margin-top-5");
			sendInviteButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					if(peopleSuggestBox.getSelectedSuggestion() != null) {
						UserGroupHeader header = peopleSuggestBox.getSelectedSuggestion().getHeader();
						String principalIdStr = header.getOwnerId();
						String firstName = header.getFirstName();
						String lastName = header.getLastName();
						String userName = header.getUserName();
						
						presenter.sendInvitation(principalIdStr, messageArea.getValue(), DisplayUtils.getDisplayName(firstName, lastName, userName));
						//do not clear message, but do clear the target user
						peopleSuggestBox.clear();
					}
					else {
						showErrorMessage("Please select a user to send an invite to.");
					}
	
					
				}
			});
			inviteUIPanel.add(sendInviteButton);
		}
	}
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
