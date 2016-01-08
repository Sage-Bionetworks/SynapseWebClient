package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Editor implementation of ColumnModelTableRow
 * 
 * @author John
 *
 */
public class ColumnModelTableRowEditorViewImpl extends AbstractColumnModelTableRow implements ColumnModelTableRowEditorView {
	
	public interface Binder extends UiBinder<TableRow, ColumnModelTableRowEditorViewImpl> {	}
	@UiField
	FormGroup nameGroup;
	@UiField
	TextBox name;
	@UiField
	HelpBlock nameHelp;
	@UiField
	ListBox type;
	@UiField
	FormGroup sizeGroup;
	@UiField
	TextBox maxSize;
	@UiField
	HelpBlock sizeHelp;	
	@UiField
	SimplePanel defaultPanel;
	CellEditor defaultWidget;
	@UiField
	TextBox restrictValues;
	String id;
	TypePresenter presenter;
	
	@Inject
	public ColumnModelTableRowEditorViewImpl(Binder uiBinder){
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
		return ColumnTypeViewEnum.valueOf(type.getSelectedValue());
	}

	@Override
	public String getMaxSize() {
		return maxSize.getText();
	}

	@Override
	public String getDefaultValue() {
		return defaultWidget.getValue();
	}

	@Override
	public void setTypePresenter(TypePresenter presenterIn) {
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
		int index = 0;
		String targetName = type.name();
		for (int i = 0; i < this.type.getItemCount(); i++) {
			if (this.type.getValue(i).equals(targetName)){
				index = i;
				break;
			}
		}
		this.type.setSelectedIndex(index);
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
		this.defaultWidget.setValue(defaultValue);
	}

	@Override
	public void setSizeFieldVisible(boolean visible) {
		maxSize.setVisible(visible);
	}

	@Override
	public void setEnumValues(List<String> enums) {
		restrictValues.setText(ColumnModelUtils.listToCSV(enums));
	}

	@Override
	public List<String> getEnumValues() {
		return ColumnModelUtils.csvToList(restrictValues.getText());
	}

	@Override
	public IsWidget getWidget(int index) {
		switch (index) {
		case 0:
			return name;
		case 1:
			return type;
		case 2:
			return maxSize;
		case 3:
			return defaultWidget;
		case 4:
			return restrictValues;
		default:
			throw new IllegalArgumentException("Unknown index: "+index);
		}
	}

	@Override
	public int getWidgetCount() {
		return 5;
	}

	@Override
	public void setDefaultEditor(final CellEditor defaultEditor) {
		this.defaultWidget = defaultEditor;
		defaultPanel.clear();
		defaultPanel.add(defaultEditor);
	}

	@Override
	public void setNameError(String error) {
		this.nameGroup.setValidationState(ValidationState.ERROR);
		this.nameHelp.setText(error);
	}

	@Override
	public void clearNameError() {
		this.nameGroup.setValidationState(ValidationState.NONE);
		this.nameHelp.setText("");
	}

	@Override
	public void setSizeError(String error) {
		this.sizeGroup.setValidationState(ValidationState.ERROR);
		this.sizeHelp.setText(error);
	}

	@Override
	public void clearSizeError() {
		this.sizeGroup.setValidationState(ValidationState.NONE);
		this.sizeHelp.setText("");
	}

	@Override
	public boolean validateDefault() {
		return this.defaultWidget.isValid();
	}

	@Override
	public void setRestrictValuesVisible(boolean showRestrictValues) {
		this.restrictValues.setVisible(showRestrictValues);
		this.restrictValues.clear();
	}
}
