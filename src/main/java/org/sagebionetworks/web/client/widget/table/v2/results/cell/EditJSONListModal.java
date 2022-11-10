package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;

public class EditJSONListModal implements EditJSONListModalView.Presenter {

  public static final String SEE_THE_ERRORS_ABOVE = "See the error(s) above.";

  private static final Map<ColumnType, ColumnType> LIST_TYPE_TO_NON_LIST = ImmutableMap
    .<ColumnType, ColumnType>builder()
    .put(ColumnType.STRING_LIST, ColumnType.STRING)
    .put(ColumnType.INTEGER_LIST, ColumnType.INTEGER)
    .put(ColumnType.DATE_LIST, ColumnType.DATE)
    .put(ColumnType.BOOLEAN_LIST, ColumnType.BOOLEAN)
    .put(ColumnType.USERID_LIST, ColumnType.USERID)
    .put(ColumnType.ENTITYID_LIST, ColumnType.ENTITYID)
    .build();
  public static final String NOT_A_VALID_JSON_ARRAY = "Not a valid JSON Array";
  public static final String EXCEEDED_MAXIMUM_NUMBER_OF_VALUES_DEFINED_IN_SCHEMA =
    "Exceeded maximum number of values defined in schema: ";
  public static final long DEFAULT_MAX_SIZE = 50L;
  public static final long DEFAULT_MAX_LIST_LENGTH = 100L;

  private final PortalGinInjector ginInjector;
  private final EditJSONListModalView view;
  private CommaSeparatedValuesParser commaSeparatedValuesParser;

  private List<CellEditor> cellEditors;

  private Consumer<List<String>> onSaveCallback;

  private long maxListLength;
  private ColumnModel effectiveSingleValueColumnModel;
  private CellFactory cellFactory;
  private GWTWrapper gwtWrapper;

  @Inject
  public EditJSONListModal(
    EditJSONListModalView view,
    PortalGinInjector ginInjector,
    CellFactory cellFactory,
    GWTWrapper gwtWrapper
  ) {
    this.ginInjector = ginInjector;
    this.view = view;
    this.cellFactory = cellFactory;
    this.gwtWrapper = gwtWrapper;
    view.setPresenter(this);
  }

  public void configure(
    String jsonString,
    Consumer<List<String>> onSaveCallback,
    ColumnModel columnModel
  ) {
    view.clearEditors();
    this.effectiveSingleValueColumnModel = new ColumnModel();
    this.effectiveSingleValueColumnModel.setMaximumSize(
        Optional
          .ofNullable(columnModel.getMaximumSize())
          .orElse(DEFAULT_MAX_SIZE)
      );
    this.effectiveSingleValueColumnModel.setColumnType(
        LIST_TYPE_TO_NON_LIST.get(columnModel.getColumnType())
      );

    this.maxListLength =
      Optional
        .ofNullable(columnModel.getMaximumListLength())
        .orElse(DEFAULT_MAX_LIST_LENGTH);

    this.onSaveCallback = onSaveCallback;
    this.cellEditors = new ArrayList<>();

    if (jsonString != null && !jsonString.isEmpty()) {
      try {
        JSONArray jsonArray = gwtWrapper.parseJSONStrict(jsonString).isArray();
        if (jsonArray == null) {
          view.showError(NOT_A_VALID_JSON_ARRAY);
        } else {
          //replace currently tracked editors with new list
          for (int i = 0; i < jsonArray.size(); i++) {
            JSONValue currValue = jsonArray.get(i);

            // if the value is a json string , we want unquoted version, else we want its string representation;
            String strVal = currValue.isString() != null
              ? currValue.isString().stringValue()
              : currValue.toString();

            addNewValue(strVal);
          }
        }
      } catch (JSONException e) {
        view.showError(NOT_A_VALID_JSON_ARRAY);
      }
    }

    // if no values, then add a single editor (allows edit or delete of annotation)
    if (cellEditors.isEmpty()) {
      view.addNewEditor(createNewEditor());
    }
    view.showEditor();
  }

  public CellEditor createNewEditor() {
    CellEditor editor = cellFactory.createEditor(
      effectiveSingleValueColumnModel
    );
    editor.addKeyDownHandler(
      new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
          // on enter, add a new field (empty fields are ignored on save)
          if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            onAddNewEmptyValue();
          }
        }
      }
    );
    cellEditors.add(editor);
    return editor;
  }

  @Override
  public void onSave() {
    //check that value size does not exceed the defined limit
    if (cellEditors.size() > maxListLength) {
      view.showError(
        EXCEEDED_MAXIMUM_NUMBER_OF_VALUES_DEFINED_IN_SCHEMA + maxListLength
      );
      return;
    }

    // check all annotation editor validity
    boolean isValid = true;
    for (CellEditor cellEditor : cellEditors) {
      isValid = isValid & cellEditor.isValid();
    }
    if (!isValid) {
      view.showError(SEE_THE_ERRORS_ABOVE);
      return;
    }

    List<String> values = cellEditors
      .stream()
      .map(CellEditor::getValue)
      .filter(DisplayUtils::isDefined)
      .collect(Collectors.toList());

    if (this.onSaveCallback != null) {
      this.onSaveCallback.accept(values);
    }
    view.hideEditor();
  }

  @Override
  public void onClickPasteNewValues() {
    //do not add another parser if one is already active
    if (this.commaSeparatedValuesParser != null) {
      this.commaSeparatedValuesParser.show();
      return;
    }
    this.commaSeparatedValuesParser =
      ginInjector.getCommaSeparatedValuesParser();

    this.commaSeparatedValuesParser.configure(this::addNewValues);
    view.addCommaSeparatedValuesParser(
      this.commaSeparatedValuesParser.asWidget()
    );
  }

  @Override
  public void onAddNewEmptyValue() {
    CellEditor editor = addNewValue(null);
    // after attaching, set focus to the new editor
    editor.setFocus(true);
  }

  @Override
  public void addNewValues(Iterable<String> values) {
    for (String val : values) {
      addNewValue(val);
    }
  }

  public CellEditor addNewValue(String value) {
    CellEditor editor = createNewEditor();
    if (value != null) {
      editor.setValue(value);
    }
    view.addNewEditor(editor);
    return editor;
  }

  @Override
  public void onValueDeleted(CellEditor editor) {
    int editorIndex = cellEditors.indexOf(editor);
    boolean editorAtEndOfList = editorIndex == cellEditors.size() - 1;

    cellEditors.remove(editorIndex);
    if (cellEditors.size() == 0) {
      view.addNewEditor(createNewEditor());
    } else if (editorAtEndOfList) {
      //if last row removed, we need to move the addValues button
      view.moveAddNewAnnotationValueButtonToRowToLastRow();
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  /**
   * for testing
   */
  public ColumnModel getEffectiveSingleValueColumnModel() {
    return this.effectiveSingleValueColumnModel;
  }

  /**
   * for testing
   */
  public long getMaxListLength() {
    return this.maxListLength;
  }

  /**
   * for testing
   */
  public List<CellEditor> getCellEditors() {
    return cellEditors;
  }
}
