package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

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
	
	private Header headerWidget;
	
	@Inject
	public RegisterAccountViewImpl(RegisterAccountViewImplUiBinder binder, Header headerWidget, Footer footerWidget) {		
		initWidget(binder.createAndBindUi(this));
		this.headerWidget = headerWidget;
		header.setWidget(headerWidget.asWidget());
		footer.setWidget(footerWidget.asWidget());
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		headerWidget.configure(false);
		headerWidget.refresh();
	}
	
	@Override
	public void setRegisterWidget(Widget w) {
		registerWidgetContainer.clear();
		registerWidgetContainer.add(w);
	}
	
}
