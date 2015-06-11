package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceListRowViewImpl extends Composite implements ProvenanceListRowView {

	public interface ProvenanceListRowViewImplUiBinder extends UiBinder<Widget, ProvenanceListRowViewImpl> {}
	
	@UiField
	SimplePanel entitySearchPanel;
	
	@UiField
	Button entityLookupButton;
	
	@UiField
	TextBox urlNameField;
	
	@UiField
	TextBox urlAddressField;
	
	@UiField
	Button removeRowButton;
	
	@UiField
	Row entityPanel;
	
	@UiField
	Row urlPanel;
	
	@UiField
	Text entityIdField;
	
	@UiField
	Text itemLabel;
	
	Widget widget;
	
	@Inject
	public ProvenanceListRowViewImpl(ProvenanceListRowViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setEntityFieldsVisible(boolean isVisible) {
		entityPanel.setVisible(isVisible);
	}

	@Override
	public void setURLFieldsVisible(boolean isVisible) {
		urlPanel.setVisible(isVisible);
	}

	@Override
	public void setEntityText(String entityId) {
		entityIdField.setText(entityId);
	}

	@Override
	public void setURLName(String title) {
		urlNameField.setText(title);
	}

	@Override
	public void setURLAddress(String address) {
		urlAddressField.setText(address);
	}

	@Override
	public void setEntityFinderWidget(Widget entityFinder) {
		entitySearchPanel.setWidget(entityFinder);
	}
	
	@Override
	public void setItemText(String text) {
		itemLabel.setText(text);
	}
	
}
