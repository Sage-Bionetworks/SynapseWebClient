package org.sagebionetworks.web.client.widget.sharing;

import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddPeopleToAclPanelViewImpl  extends Composite implements AddPeopleToAclPanelView {
	public interface AddPeopleToAclPanelViewImplUiBinder extends UiBinder<Widget, AddPeopleToAclPanelViewImpl> {};
	
	@UiField
	Column suggestBoxPanel;
//	@UiField
//	Column permissionLevelPanel;
	@UiField
	Button addPersonButton;
	@UiField
	Button makePublicButton;
	@UiField
	CheckBox notifyPeopleCheckBox;
	@UiField
	Tooltip notifyTooltip;
	@UiField
	ButtonGroup permButtonGroup;
	@UiField
	Button permDropDownButton;
	@UiField
	DropDownMenu permDropDownMenu;
	
	
	private Presenter presenter;
	private UserGroupSuggestBox suggestBox;
	
	private HandlerRegistration publicButtonReg;	// TODO: Get rid of this logic when only build window once (if I do that)?
	private HandlerRegistration makePublicReg;
	
	@Inject
	public AddPeopleToAclPanelViewImpl(AddPeopleToAclPanelViewImplUiBinder uiBinder, UserGroupSuggestBox suggestBox) {
		initWidget(uiBinder.createAndBindUi(this));
		this.suggestBox = suggestBox;
		suggestBox.asWidget().addStyleName("form-control input-xs");
		
		suggestBoxPanel.add(suggestBox.asWidget());
		notifyTooltip.setText(DisplayConstants.NOTIFY_PEOPLE_TOOLTIP);
		permButtonGroup.addStyleName("input-xs");
	}
	
	@Override
	public UserGroupSuggestBox getSuggestBox() {
		return suggestBox;
	}
	
	@Override
	public CheckBox getNotifyPeopleCheckBox() {
		return notifyPeopleCheckBox;
	}
	
	@Override
	public void configure(PermissionLevel[] permLevels, Map<PermissionLevel, String> permDisplay, CallbackP<Void> selectPermissionCallback,  final CallbackP<Void> addPersonCallback,
						final CallbackP<Void> makePublicCallback, Boolean isPubliclyVisible) {
		permDropDownMenu.clear();
		
		//permissionLevelPanel.clear();
		//permissionListBox.addStyleName("input-xs");
		//permissionLevelPanel.add(permissionListBox);
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
	
	@Override
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	private void configureDropdownButton(PermissionLevel[] permLevels, final Map<PermissionLevel, String> permDisplay, final CallbackP<Void> selectPermissionCallback) {
		for (final PermissionLevel permLevel : permLevels) {
			AnchorListItem item = new AnchorListItem(permDisplay.get(permLevel));
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					permDropDownButton.setText(permDisplay.get(permLevel));
					presenter.setSelectedPermissionLevel(permLevel);
					selectPermissionCallback.invoke(null);
				}
			});
			permDropDownMenu.add(item);
		}
	}
}