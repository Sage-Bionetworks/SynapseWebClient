package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
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
	Column suggestBoxPanel;
	@UiField
	Button addPersonButton;
	@UiField
	Button makePublicButton;
	@UiField
	CheckBox notifyPeopleCheckBox;
	@UiField
	Tooltip notifyTooltip;
	@UiField
	Button permDropDownButton;
	@UiField
	DropDownMenu permDropDownMenu;
	
	
	private UserGroupSuggestBox suggestBox;
	
	private PermissionLevel selectedPermissionLevel;
	private HandlerRegistration publicButtonReg;
	private HandlerRegistration makePublicReg;
	
	@Inject
	public AclAddPeoplePanel(AclAddPeoplePanelUiBinder uiBinder, UserGroupSuggestBox suggestBox) {
		initWidget(uiBinder.createAndBindUi(this));
		this.suggestBox = suggestBox;
		suggestBox.asWidget().addStyleName("form-control input-sm");
		
		suggestBoxPanel.add(suggestBox.asWidget());
		notifyTooltip.setTitle(DisplayConstants.NOTIFY_PEOPLE_TOOLTIP);
	}
	
	public UserGroupSuggestBox getSuggestBox() {
		return suggestBox;
	}
	
	public CheckBox getNotifyPeopleCheckBox() {
		return notifyPeopleCheckBox;
	}
	
	public void configure(PermissionLevel[] permLevels, Map<PermissionLevel, String> permDisplay, CallbackP<Void> selectPermissionCallback,  final CallbackP<Void> addPersonCallback,
						final CallbackP<Void> makePublicCallback, Boolean isPubliclyVisible) {
		clear();
		permDropDownButton.setText(ACCESS_LEVEL_PLACEHOLDER_TEXT);
		
		configureDropdownButton(permLevels, permDisplay, selectPermissionCallback);
		
		if (publicButtonReg != null) {
			publicButtonReg.removeHandler();
		}
		
		publicButtonReg = addPersonButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addPersonCallback.invoke(null);
			}
			
		});
		
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

	public PermissionLevel getSelectedPermissionLevel() {
		return selectedPermissionLevel;
	}
	
	@Override
	public void clear() {
		suggestBox.clear();
		permDropDownMenu.clear();
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
	
	
	/*
	 * Private Methods
	 */

	private void configureDropdownButton(PermissionLevel[] permLevels, final Map<PermissionLevel, String> permDisplay, final CallbackP<Void> selectPermissionCallback) {
		for (final PermissionLevel permLevel : permLevels) {
			AnchorListItem item = new AnchorListItem(permDisplay.get(permLevel));
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					permDropDownButton.setText(permDisplay.get(permLevel));
					selectedPermissionLevel = permLevel;
					selectPermissionCallback.invoke(null);
				}
			});
			permDropDownMenu.add(item);
		}
	}
}