package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Editor implementation of ColumnModelTableRow
 * 
 * @author John
 *
 */
public class ColumnModelTableRowEditorImpl implements ColumnModelTableRowEditor {
	
	public interface Binder extends UiBinder<TableRow, ColumnModelTableRowEditorImpl> {	}
	
	/*
	 * The actual <tr> for this editor.
	 */
	TableRow row;
	@UiField
	CheckBoxButton select;
	@UiField
	TextBox name;
	@UiField
	Select type;
	@UiField
	TextBox maxSize;
	@UiField
	TextBox defaultValue;
	String id;
	Presenter presenter;
	
	@Inject
	public ColumnModelTableRowEditorImpl(Binder uiBinder){
		row = uiBinder.createAndBindUi(this);
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getColumnName() {
		return name.getText();
	}

	@Override
	public ColumnTypeViewEnum getColumnType() {
		return ColumnTypeViewEnum.valueOf(type.getValue());
	}

	@Override
	public String getMaxSize() {
		return maxSize.getText();
	}

	@Override
	public String getDefaultValue() {
		return defaultValue.getText();
	}

	@Override
	public void setSelected(boolean select) {
		this.select.setActive(select);
	}

	@Override
	public boolean isSelected() {
		return this.select.isActive();
	}

	@Override
	public void setPresenter(Presenter presenterIn) {
		this.presenter = presenterIn;
		type.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.onTypeChanged();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return row;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setColumnType(ColumnTypeViewEnum type) {
		this.type.setValue(type.name());
	}

	@Override
	public void setColumnName(String name) {
		this.name.setText(name);
	}

	@Override
	public void setMaxSize(String maxSize) {
		this.maxSize.setText(maxSize);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue.setText(defaultValue);
	}

	@Override
	public void setSizeFieldVisible(boolean visible) {
		maxSize.setVisible(visible);
	}

}
