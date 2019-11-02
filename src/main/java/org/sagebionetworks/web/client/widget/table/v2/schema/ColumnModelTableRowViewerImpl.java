package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A viewer for a ColumnModel
 * 
 * @author John
 *
 */
public class ColumnModelTableRowViewerImpl extends AbstractColumnModelTableRow implements ColumnModelTableRowViewer {

	public interface Binder extends UiBinder<TableRow, ColumnModelTableRowViewerImpl> {
	}

	@UiField
	FormControlStatic name;
	@UiField
	FormControlStatic type;
	@UiField
	FormControlStatic facetType;
	@UiField
	FormControlStatic maxSize;
	@UiField
	FormControlStatic defaultValue;
	@UiField
	FormControlStatic restrictValues;

	String id;

	@Inject
	public ColumnModelTableRowViewerImpl(Binder uiBinder) {
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
		return ColumnTypeViewEnum.valueOf(type.getText());
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
	public Widget asWidget() {
		return row;
	}

	@Override
	public void setSelectable(boolean isSelectable) {
		this.select.setVisible(isSelectable);
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setColumnName(String name) {
		this.name.setText(name);;
	}

	@Override
	public void setColumnType(ColumnTypeViewEnum type) {
		this.type.setText(type.name());
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
	public void setEnumValues(List<String> enums) {
		restrictValues.setText(ColumnModelUtils.listToCSV(enums));
	}

	@Override
	public List<String> getEnumValues() {
		return ColumnModelUtils.csvToList(restrictValues.getText());
	}

	@Override
	public ColumnFacetTypeViewEnum getFacetType() {
		return ColumnFacetTypeViewEnum.getEnumForFriendlyName(facetType.getText());
	}

	@Override
	public void setFacetType(ColumnFacetTypeViewEnum type) {
		this.facetType.setText(type.toString());
	}

}
