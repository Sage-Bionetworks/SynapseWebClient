package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.PanelFooter;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A table view of a list of ColumnModels
 * @author jmhill
 *
 */
public class ColumnModelsViewImpl extends Composite implements ColumnModelsView {
	
	public interface Binder extends UiBinder<Widget, ColumnModelsViewImpl> {	}
	@UiField
	PanelHeader panelHeader;
	@UiField
	Table table;
	@UiField
	TBody tableBody;
	@UiField
	PanelFooter panelFooter;
	@UiField
	Button addColumnButton;
	
	Presenter presenter;
	
	private boolean isEditable = false;
	
	@Inject
	public ColumnModelsViewImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
		addColumnButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addNewColumnModel();
			}
		});
		presenter = new ColumnModelsViewPresenter(this);
	}

	@Override
	public void configure(String headerText, List<ColumnModel> models,
			boolean isEditable) {
		this.isEditable = isEditable;
		if(headerText != null){
			panelHeader.setText(headerText);
			panelHeader.setVisible(true);
		}
		// Add each model
		for(ColumnModel cm: models){
			// All existing columns are not editable.
			addColumnToTable(cm, false);
		}
		// make the body visible
		this.tableBody.setVisible(true);
		if(isEditable){
			this.panelFooter.setVisible(true);
		}
		
	}
	
	/**
	 * Add a column model to the table
	 * @param column
	 * @param isColumnEditable
	 */
	private void addColumnToTable(ColumnModel column, boolean isColumnEditable){
		// Column name
		TableData columnNameData = new TableData();
		if(isColumnEditable){
			TextBox nameEditor = new TextBox();
			nameEditor.setText(column.getName());
			columnNameData.add(nameEditor);
		}else{
			columnNameData.setText(column.getName());
		}
		// Column type
		TableData columnTypeData = new TableData();
		if(isColumnEditable){
			columnTypeData.add(buildColumnTypeSelect(column.getColumnType()));
		}else{
			columnTypeData.setText(column.getColumnType().name());
		}
		// Max string size
		TableData maxLengthData = new TableData();
		if(isColumnEditable){
			TextBox maxEditor = new TextBox();
			if(column.getMaximumSize() != null){
				maxEditor.setText(column.getMaximumSize().toString());
			}
			maxLengthData.add(maxEditor);
		}else{
			if(column.getMaximumSize() != null){
				maxLengthData.setText(column.getMaximumSize().toString());
			}
		}
		// Default value
		TableData defaultData = new TableData();
		if(isColumnEditable){
			TextBox defaultEditor = new TextBox();
			defaultEditor.setText(column.getDefaultValue());
			defaultData.add(defaultEditor);
		}else{
			defaultData.setText(column.getDefaultValue());
		}
		TableRow tr = new TableRow();
		tr.add(columnNameData);
		tr.add(columnTypeData);
		tr.add(maxLengthData);
		tr.add(defaultData);
		tableBody.add(tr);
	}

	/**
	 * Add a new column to the models
	 */
	private void addNewColumnModel(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		addColumnToTable(cm, true);
	}
	
	/**
	 * Build a new column type selector.
	 * @param currentType
	 * @return
	 */
	private Select buildColumnTypeSelect(ColumnType currentType){
		Select select = new Select();
		Option current = null;
		for(ColumnType type: ColumnType.values()){
			Option op = new Option();
			op.setText(type.name());
			if(type.equals(currentType)){
				current = op;
			}
			select.add(op);
		}
		if(current != null){
			select.setValue(current);
		}
		return select;
	}

	@Override
	public List<ColumnModel> getCurrentModels() {
		return presenter.getCurrentModels();
	}

	@Override
	public boolean validateModel() {
		return presenter.validateModel();
	}

	@Override
	public void clear() {
		tableBody.clear();
	}

	@Override
	public void showError(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNewColumn(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String id, String name, boolean isEditable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnType(String id, ColumnType columnType,
			boolean isEditable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnMaxSize(String id, String string, boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnDefault(String id, String defaultValue,
			boolean editable) {
		// TODO Auto-generated method stub
		
	}
	
}
