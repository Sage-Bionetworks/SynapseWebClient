package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterAccountViewImpl extends Composite implements RegisterAccountView {

	public interface RegisterAccountViewImplUiBinder extends UiBinder<Widget, RegisterAccountViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;	

	@UiField
	Div registerWidgetContainer;
	
	@Inject
	public RegisterAccountViewImpl(RegisterAccountViewImplUiBinder binder) {		
		initWidget(binder.createAndBindUi(this));
	}
	
	@Override
	public void setFooterWidget(Widget w) {
		footer.clear();
		footer.setWidget(w);
	}
	
	@Override
	public void setHeaderWidget(Widget w) {
		header.clear();
		header.setWidget(w);
	}
	@Override
	public void setRegisterWidget(Widget w) {
		registerWidgetContainer.clear();
		registerWidgetContainer.add(w);
	}
	
}
