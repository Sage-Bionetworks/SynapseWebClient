package org.sagebionetworks.web.client.widget.table.v2;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.PanelFooter;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;

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
	
	private boolean isEditable;
	
	@Inject
	public ColumnModelsViewImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void configure(String headerText, List<ColumnModel> models,
			boolean isEditable) {
		this.isEditable = isEditable;
		if(headerText != null){
			panelHeader.setText(headerText);
		}
		// Add each model
		for(ColumnModel cm: models){
			addColumnToTable(cm, isEditable);
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
			Label nameLabel = new Label();
			nameLabel.setText(column.getName());
			columnNameData.add(new Label(column.getName()));
		}
		// Column type
		TableData columnTypeData = new TableData();
		if(isColumnEditable){
			DropDown typeDropDown = new DropDown();
			for(ColumnType type: ColumnType.values()){
				AnchorListItem ali = new AnchorListItem(type.name());
				typeDropDown.add(ali);
			}
			columnTypeData.add(typeDropDown);
		}else{
			columnTypeData.add(new Label(column.getColumnType().name()));
		}
		
		TableRow tr = new TableRow();
		tr.add(columnNameData);
		tr.add(columnTypeData);
		tableBody.add(tr);
	}

}
