package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.widget.sharing.UserGroupSearchBox;

import com.extjs.gxt.ui.client.data.ModelData;
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
	private ComboBox<ModelData> peopleCombo;
	@Inject
	public InviteWidgetViewImpl(SageImageBundle sageImageBundle,
			UrlCache urlCache, SynapseJSNIUtils synapseJSNIUtils) {
		this.sageImageBundle = sageImageBundle;
		this.urlCache = urlCache;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void configure() {
		initView();
		clear();
		add(inviteButton);
		add(inviteUIPanel);
		peopleCombo.clearSelections();
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
			
			peopleCombo = UserGroupSearchBox.createUserGroupSearchSuggestBox(urlCache.getRepositoryServiceUrl(), synapseJSNIUtils.getBaseFileHandleUrl(), synapseJSNIUtils.getBaseProfileAttachmentUrl(), null);
			peopleCombo.setEmptyText("Enter a user name...");
			peopleCombo.setWidth(FIELD_WIDTH);
			peopleCombo.setForceSelection(true);
			peopleCombo.setTriggerAction(TriggerAction.ALL);
			inviteUIPanel.add(peopleCombo);
			
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
					if(peopleCombo.getValue() != null) {
						ModelData selectedModel = peopleCombo.getValue();
						String principalIdStr = (String) selectedModel.get(UserGroupSearchBox.KEY_PRINCIPAL_ID);
						String firstName = (String) selectedModel.get(UserGroupSearchBox.KEY_FIRSTNAME);
						String lastName = (String) selectedModel.get(UserGroupSearchBox.KEY_LASTNAME);
						String userName = (String) selectedModel.get(UserGroupSearchBox.KEY_USERNAME);
						
						presenter.sendInvitation(principalIdStr, messageArea.getValue(), DisplayUtils.getDisplayName(firstName, lastName, userName));
						//do not clear message, but do clear the target user
						peopleCombo.clearSelections();
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
