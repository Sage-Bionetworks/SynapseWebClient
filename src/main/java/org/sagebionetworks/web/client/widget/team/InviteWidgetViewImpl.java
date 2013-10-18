package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.UrlCache;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorViewImpl;
import org.sagebionetworks.web.client.widget.sharing.UserGroupSearchBox;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
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
	private UrlCache urlCache;
	private InviteWidgetView.Presenter presenter;
	private AnimationProtector versionAnimation;
	private LayoutContainer inviteUIPanel;
	private Button inviteButton;
	private TextArea messageArea;
	private ComboBox<ModelData> peopleCombo;
	@Inject
	public InviteWidgetViewImpl(SageImageBundle sageImageBundle,
			UrlCache urlCache) {
		this.sageImageBundle = sageImageBundle;
		this.urlCache = urlCache;
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
			versionAnimation = new AnimationProtector(new AnimationProtectorViewImpl(inviteButton, inviteUIPanel));
			FxConfig hideConfig = new FxConfig(400);
			hideConfig.setEffectCompleteListener(new Listener<FxEvent>() {
				@Override
				public void handleEvent(FxEvent be) {
					// This call to layout is necessary to force the scroll bar to appear on page-load
					inviteUIPanel.layout(true);
				}
			});
			versionAnimation.setHideConfig(hideConfig);
			FxConfig showConfig = new FxConfig(400);
			showConfig.setEffectCompleteListener(new Listener<FxEvent>() {
				@Override
				public void handleEvent(FxEvent be) {
					// This call to layout is necessary to force the scroll bar to appear on page-load
					inviteUIPanel.layout(true);
				}
			});
			versionAnimation.setShowConfig(showConfig);
			
			peopleCombo = UserGroupSearchBox.createUserGroupSearchSuggestBox(urlCache.getRepositoryServiceUrl(), null);
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
						String displayName = (String) selectedModel.get(UserGroupSearchBox.KEY_DISPLAY_NAME);
						Long principalId = (Long.parseLong(principalIdStr));
						presenter.sendInvitation(principalIdStr, messageArea.getValue(), displayName);
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
