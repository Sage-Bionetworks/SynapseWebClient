package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The view of this editor with zero business logic.
 * 
 * @author jhill
 *
 */
public class EnumCellEditorViewImpl implements EnumCellEditorView {

	public interface Binder extends UiBinder<Widget, EnumCellEditorViewImpl> {}
	
	@UiField
	Select select;
	
	Widget widget;
	
	@Inject
	public EnumCellEditorViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setValue(String value) {
		select.setValue(value);
	}

	@Override
	public String getValue() {
		return select.getValue();
	}

	@Override
	public int getTabIndex() {
		return select.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		select.setAccessKey(key);	
	}

	@Override
	public void setFocus(boolean focused) {
		select.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		select.setTabIndex(index);
	}

	@Override
	public void addOption(String value) {
		Option option = new Option();
		option.setText(value);
		select.add(option);
	}
}
