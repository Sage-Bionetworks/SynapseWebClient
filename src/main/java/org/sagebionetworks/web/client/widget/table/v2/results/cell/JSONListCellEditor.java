package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.StringUtils;

/**
 * An editor for list type columns renders raw json array!).
 */
public class JSONListCellEditor
  extends AbstractCellEditor
  implements CellEditor, JSONListCellEditorView.Presenter {

  public static final String MUST_BE = "Must be ";
  public static final String CHARACTERS_OR_LESS = " characters or less";
  public static final String ITEMS_OR_LESS = " items or less";
  public static final String VALID_JSON_ARRAY = " a valid json array";
  public static final String VALID_VALUE = "a valid value";
  JSONArrayAdapter jsonArrayAdapter;
  ColumnModel columnModel;
  PortalGinInjector ginInjector;
  GWTWrapper gwt;
  EditJSONListModal editJSONListModal;

  @Inject
  public JSONListCellEditor(
    JSONListCellEditorView view,
    JSONArrayAdapter jsonArrayAdapter,
    PortalGinInjector ginInjector,
    GWTWrapper gwt
  ) {
    super(view);
    this.jsonArrayAdapter = jsonArrayAdapter;
    this.gwt = gwt;
    this.ginInjector = ginInjector;
    view.setPresenter(this);
  }

  public EditJSONListModal getEditJSONListModal() {
    if (editJSONListModal == null) {
      editJSONListModal = ginInjector.getEditJsonModal();
    }
    return editJSONListModal;
  }

  @Override
  public void onEditButtonClick() {
    getEditJSONListModal()
      .configure(this.getValue(), this::setValues, this.columnModel);
  }

  @Override
  public boolean isValid() {
    String value = StringUtils.emptyAsNull(this.getValue());
    if (value != null) {
      // parse value
      if (!gwt.isValidJSONArray(value)) {
        view.setValidationState(ValidationState.ERROR);
        view.setHelpText(MUST_BE + VALID_JSON_ARRAY);
        return false;
      }
      try {
        JSONArrayAdapter adapter = jsonArrayAdapter.createNewArray(value);
        Long maxListLength = columnModel.getMaximumListLength();
        if (maxListLength != null) {
          boolean isListLengthValid = adapter.length() <= maxListLength;
          if (!isListLengthValid) {
            view.setValidationState(ValidationState.ERROR);
            view.setHelpText(MUST_BE + maxListLength + ITEMS_OR_LESS);
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
        // gwt.isValidJSONArray() will catch json parsing errors.
        // Is this now unreachable?
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
    String jsonArrayString = jsonArrayString(values);
    this.setValue(jsonArrayString);
  }

  public String jsonArrayString(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }

    //put each value into the JSON array
    JSONArrayAdapter adapter = jsonArrayAdapter.createNewArray();
    try {
      for (int i = 0; i < values.size(); i++) {
        adapter.put(i, values.get(i));
      }
    } catch (JSONObjectAdapterException e) {
      // should never happen since we're just adding strings
      view.setValidationState(ValidationState.ERROR);
      view.setHelpText(MUST_BE + VALID_VALUE);
    }
    //set JSON string as the editor's value
    return adapter.toJSONString();
  }

  public ColumnModel getColumnModel() {
    return columnModel;
  }

  public void setColumnModel(ColumnModel columnModel) {
    this.columnModel = columnModel;
  }
}
