package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.table.ColumnModel;

import static org.sagebionetworks.repo.model.table.ColumnType.*;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple for for editing a column model.
 * @author John
 *
 */
public class ColumnModelFormImpl extends Composite implements ColumnModelForm {
	
	public interface Binder extends UiBinder<Widget, ColumnModelFormImpl> {	}
	
	@UiField
	TextBox columnName;
	@UiField
	ButtonGroup columnType;
	@UiField
	RadioButton stringButton;
	@UiField
	RadioButton integerButton;
	@UiField
	RadioButton doubleButton;
	@UiField
	RadioButton dateButton;
	@UiField
	RadioButton fileButton;
	
	@Inject
	public ColumnModelFormImpl(final Binder uiBinder){
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setColumnModel(ColumnModel columnModel) {
		this.columnName.setText(columnModel.getName());
		stringButton.setActive(STRING.equals(columnModel.getColumnType()));
		integerButton.setActive(LONG.equals(columnModel.getColumnType()));
		doubleButton.setActive(DOUBLE.equals(columnModel.getColumnType()));
		dateButton.setActive(DATE.equals(columnModel.getColumnType()));
		fileButton.setActive(FILEHANDLEID.equals(columnModel.getColumnType()));
	}

	@Override
	public ColumnModel getColumnModel() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
