package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.ListBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddPeopleToAclPanelViewImpl  extends Composite implements AddPeopleToAclPanelView {
	public interface AddPeopleToAclPanelViewImplUiBinder extends UiBinder<Widget, AddPeopleToAclPanelViewImpl> {};
	
	@UiField
	Column suggestBoxPanel;
	@UiField
	Column permissionLevelPanel;
	
	private Presenter presenter;
	
	@Inject
	public AddPeopleToAclPanelViewImpl(AddPeopleToAclPanelViewImplUiBinder uiBinder, UserGroupSuggestBox suggestBox) {
		initWidget(uiBinder.createAndBindUi(this));
		suggestBox.asWidget().addStyleName("form-control input-xs");
		ListBox listBox = new ListBox();
		listBox.addStyleName("input-xs");
		
		suggestBoxPanel.add(suggestBox.asWidget());
		permissionLevelPanel.add(listBox);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

}
