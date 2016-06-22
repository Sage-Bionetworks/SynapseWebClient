package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ScopeWidgetViewImpl implements ScopeWidgetView {

	public interface Binder extends UiBinder<Widget, ScopeWidgetViewImpl> {}
	
	@UiField
	SimplePanel scopeContainer;
	@UiField
	FormGroup scopeUI;
	
	Widget widget;
	
	@Inject
	public ScopeWidgetViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
}
