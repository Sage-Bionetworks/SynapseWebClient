package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestOracle.UserGroupSuggestion;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AclAddPeoplePanel extends Composite implements SynapseView {
	public interface AclAddPeoplePanelUiBinder extends UiBinder<Widget, AclAddPeoplePanel> {};
	
	public static final String ACCESS_LEVEL_PLACEHOLDER_TEXT = "Select access level...";
	
	@UiField
	Div suggestBoxPanel;
	@UiField
	Button makePublicButton;
	@UiField
	CheckBox notifyPeopleCheckBox;
	@UiField
	Tooltip notifyTooltip;
	
	private UserGroupSuggestBox suggestBox;
	
	private HandlerRegistration makePublicReg;
	
	@Inject
	public AclAddPeoplePanel(AclAddPeoplePanelUiBinder uiBinder, UserGroupSuggestBox suggestBox) {
		initWidget(uiBinder.createAndBindUi(this));
		this.suggestBox = suggestBox;
		
		suggestBoxPanel.add(suggestBox.asWidget());
		notifyTooltip.setTitle(DisplayConstants.NOTIFY_PEOPLE_TOOLTIP);
	}
	
	public UserGroupSuggestBox getSuggestBox() {
		return suggestBox;
	}
	
	public CheckBox getNotifyPeopleCheckBox() {
		return notifyPeopleCheckBox;
	}
	
	public void configure(PermissionLevel[] permLevels, Map<PermissionLevel, String> permDisplay, final CallbackP<UserGroupSuggestion> addPersonCallback,
						final CallbackP<Void> makePublicCallback, Boolean isPubliclyVisible) {
		clear();
		
		suggestBox.addItemSelectedHandler(addPersonCallback);
		
		if (makePublicReg != null) {
			makePublicReg.removeHandler();
		}
		makePublicReg = makePublicButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				makePublicCallback.invoke(null);
			}
			
		});
		
		if (isPubliclyVisible != null) {
			setMakePublicButtonDisplay(!isPubliclyVisible);
		}
		
	}
	
	public void setMakePublicButtonDisplay(boolean isPubliclyVisible) {
		if (!isPubliclyVisible) {
			makePublicButton.setText("Make Public");
			makePublicButton.setIcon(IconType.GLOBE);
		} else {
			makePublicButton.setText("Make Private");
			makePublicButton.setIcon(IconType.LOCK);
		}
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void clear() {
		suggestBox.clear();
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}
}