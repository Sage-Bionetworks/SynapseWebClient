package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.StringUtils;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;

/**
 * An editor for list type columns renders raw json array!).
 */
public class JSONListCellEditor extends AbstractCellEditor implements CellEditor, JSONListCellEditorView.Presenter {

	public static final String MUST_BE = "Must be ";
	public static final String CHARACTERS_OR_LESS = " characters or less";
	public static final String ITEMS_OR_LESS = " items or less";
	public static final String VALID_JSON_ARRAY = " a valid json array";
	public static final String VALID_VALUE = "a valid value";
	JSONArrayAdapter jsonArrayAdapter;
	ColumnModel columnModel;
	PortalGinInjector ginInjector;

	EditJSONModal editJSONModal;

	@Inject
	public JSONListCellEditor(JSONListCellEditorView view, JSONArrayAdapter jsonArrayAdapter, PortalGinInjector ginInjector) {
		super(view);
		this.jsonArrayAdapter = jsonArrayAdapter;
		this.ginInjector = ginInjector;
		view.setEditor(this);
	}

	public EditJSONModal getEditJSONModal() {
		if (editJSONModal == null) {
			editJSONModal = ginInjector.getEditJsonModal();
//			((JSONListCellEditorView) view).addEditorToPage(editJSONModal.asWidget());
		}
		return editJSONModal;
	}

	@Override
	public void onEditButtonClick(){
		getEditJSONModal().configure(this.getValue(), this::setValues, this.columnModel);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.emptyAsNull(this.getValue());
		if (value != null) {
			// parse value
			try {
				JSONArrayAdapter adapter = jsonArrayAdapter.createNewArray(value);
				Long maximumListLength = columnModel.getMaximumListLength();
				if (maximumListLength != null) {
					boolean isListLengthValid = adapter.length() <= maximumListLength;
					if (!isListLengthValid) {
						view.setValidationState(ValidationState.ERROR);
						view.setHelpText(MUST_BE + maximumListLength + ITEMS_OR_LESS);
						return false;
					}	
				}

				Long maximumSize = columnModel.getMaximumSize();
				if (maximumSize != null) {
					// check that each value is under the max string length
					for (int i = 0; i < adapter.length(); i++) {
						if (adapter.getString(i).length() > maximumSize) {
							view.setValidationState(ValidationState.ERROR);
							view.setHelpText(MUST_BE + maximumSize + CHARACTERS_OR_LESS);
							return false;
						}					
					}
				}
			} catch (JSONObjectAdapterException e) {
				// failed to parse, show error
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(MUST_BE + VALID_JSON_ARRAY);
				return false;
			}
			
			
		}
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}

	public void setValues(List<String> values) {
		if (values == null || values.isEmpty()) {
			this.setValue(null);
			return;
		}

		//put each value into the JSON array
		JSONArrayAdapter adapter = jsonArrayAdapter.createNewArray();
		try {
			for (int i = 0; i < values.size(); i++) {
				adapter.put(i, values.get(i));
			}
		} catch (JSONObjectAdapterException e){
			// failed to add value, show error
			view.setValidationState(ValidationState.ERROR);
			view.setHelpText(MUST_BE + VALID_VALUE);
		}
		//set JSON string as the editor's value
		this.setValue(adapter.toJSONString());
	}

	public ColumnModel getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(ColumnModel columnModel) {
		this.columnModel = columnModel;
	}
}
