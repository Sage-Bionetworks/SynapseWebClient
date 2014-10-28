package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * Basic implementation with zero business logic.
 * 
 * @author jhill
 *
 */
public class ActionMenuWidgetViewImpl implements ActionMenuWidgetView {
	
	public interface Binder extends UiBinder<Widget, ActionMenuWidgetViewImpl> {}

	@UiField
	ButtonGroup buttonGroup;
	
	@UiField
	DropDownMenu toolsDropDown;
	
	Widget widget;
	
	@Inject
	public ActionMenuWidgetViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}


	@Override
	public void clear() {
		buttonGroup.clear();
		toolsDropDown.clear();
	}

	@Override
	public void addButton(ActionView actionView) {
		buttonGroup.add(actionView);
	}

	@Override
	public void addMenuItem(ActionView actionView) {
		toolsDropDown.add(actionView);
	}

}
