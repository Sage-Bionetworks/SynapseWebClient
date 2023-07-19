package org.sagebionetworks.web.client.widget.table.v2.schema;

import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnFacetTypeViewEnum.None;
import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnFacetTypeViewEnum.Range;
import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnFacetTypeViewEnum.Values;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;

/**
 * Control logic for a single ColumnModelTableRowEditor.
 *
 * @author John
 *
 */
public class ColumnModelTableRowEditorWidgetImpl
  implements
    ColumnModelTableRowEditorWidget,
    ColumnModelTableRowEditorView.TypePresenter {

  public static final String MUST_BE_A_NUMBER = "Must be: 1 - 1000";
  public static final String NAME_CANNOT_BE_EMPTY = "Name cannot be empty";
  public static final int DEFAULT_STRING_SIZE = 50;
  public static final int MAX_STRING_SIZE = 1000;
  public static final int DEFAULT_LIST_LENGTH = 10;
  public static final String MUST_BE_A_NUMBER_ONE_AND_HUNDRED =
    "Must be: 1 - 100";
  public static final int MAX_LIST_LENGTH = 100;
  ColumnModelTableRowEditorView view;
  ColumnTypeViewEnum currentType;
  CellFactory factory;
  private final CookieProvider cookies;

  String maxSize = null;
  boolean canHaveDefault = true;
  boolean isView = false;

  @Inject
  public ColumnModelTableRowEditorWidgetImpl(
    ColumnModelTableRowEditorView view,
    CellFactory factory,
    CookieProvider cookies
  ) {
    this.view = view;
    this.factory = factory;
    this.cookies = cookies;
    view.setTypePresenter(this);
  }

  @Override
  public void onTypeChanged() {
    // Is this a change
    ColumnTypeViewEnum newType = view.getColumnType();
    if (!currentType.equals(newType)) {
      configureViewForType(newType);
    }
  }

  /**
   * Setup the view for the given type.
   *
   * @param newType
   */
  public void configureViewForType(ColumnTypeViewEnum newType) {
    if (canHaveSize(newType)) {
      // SWC-5738: Special case.
      // If the max size is already defined in the view (from a previous column type that supports max size), then use it.
      if (view.getMaxSize().isEmpty()) {
        view.setMaxSize(getMaxSizeForType(newType));
      }
      view.setSizeFieldVisible(true);
    } else {
      view.setMaxSize(null);
      view.setSizeFieldVisible(false);
    }
    if (canHaveMaxListLength(newType)) {
      view.setMaxListLength("" + DEFAULT_LIST_LENGTH);
      view.setMaxListLengthFieldVisible(true);
    } else {
      view.setMaxListLength(null);
      view.setMaxListLengthFieldVisible(false);
    }

    configureFacetsForType(newType);
    view.setRestrictValuesVisible(canHaveRestrictedValues(newType));
    view.clearSizeError();
    view.clearMaxListLengthError();
    this.currentType = newType;
    CellEditor defaultEditor = getDefaultEditorForType(newType);
    view.setDefaultEditor(defaultEditor);
    view.setDefaultEditorVisible(canHaveDefault(newType));
  }

  public boolean canHaveDefault(ColumnTypeViewEnum type) {
    if (canHaveDefault) {
      switch (type.getType()) {
        case ENTITYID:
        case FILEHANDLEID:
        case USERID:
        case MEDIUMTEXT:
        case LARGETEXT:
        case JSON:
          return false;
        default:
          return true;
      }
    } else {
      return false;
    }
  }

  /**
   * Can the given type have a size?
   *
   * @param type
   * @return
   */
  public boolean canHaveSize(ColumnTypeViewEnum type) {
    switch (type) {
      case String:
      case StringList:
      case Link:
        return true;
      default:
        // all other are false
        return false;
    }
  }

  /**
   * Can the given type have a maximum list length?
   *
   * @param type
   * @return
   */
  public boolean canHaveMaxListLength(ColumnTypeViewEnum type) {
    switch (type) {
      case StringList:
      case BooleanList:
      case DateList:
      case IntegerList:
        return true;
      default:
        // all other are false
        return false;
    }
  }

  /**
   * Can the given type have a restricted value?
   *
   * @param type
   * @return
   */
  public boolean canHaveRestrictedValues(ColumnTypeViewEnum type) {
    switch (type) {
      case String:
      case Integer:
      case Entity:
        return true;
      default:
        // all other are false
        return false;
    }
  }

  /**
   * Configure the facet selection based on the column type
   *
   * @param type
   * @return
   */
  public void configureFacetsForType(ColumnTypeViewEnum type) {
    switch (type) {
      case Integer:
      case IntegerList:
        view.setFacetValues(
          None.toString(),
          Values.toString(),
          Range.toString()
        );
        view.setFacetVisible(true);
        break;
      case String:
      case Boolean:
      case User:
      case Entity:
      case StringList:
      case BooleanList:
      case EntityIdList:
      case UserIdList:
      case EvaluationId:
        view.setFacetValues(None.toString(), Values.toString());
        view.setFacetVisible(true);
        break;
      case Double:
      case Date:
      case DateList:
        view.setFacetValues(None.toString(), Range.toString());
        view.setFacetVisible(true);
        break;
      default:
        view.setFacetVisible(false);
    }
  }

  public boolean canHaveFacet(ColumnTypeViewEnum type) {
    switch (type) {
      case String:
      case Integer:
      case Boolean:
      case Double:
      case Date:
      case User:
      case Entity:
      case StringList:
      case IntegerList:
      case BooleanList:
      case DateList:
      case EntityIdList:
      case UserIdList:
      case EvaluationId:
        return true;
      default:
        return false;
    }
  }

  /**
   * Get the default cell editor to be used for a type.
   *
   * @param newType
   * @return
   */
  private CellEditor getDefaultEditorForType(ColumnTypeViewEnum newType) {
    ColumnModel forFactory = new ColumnModel();
    forFactory.setColumnType(newType.getType());
    CellEditor defaultEditor = factory.createEditor(forFactory);
    return defaultEditor;
  }

  @Override
  public void setIsView() {
    // SWC-6333:  if this column is in a view, only allow column types that are mapped to annotation types.
    // And maintain the behavior that these columns cannot have default values.
    canHaveDefault = false;
    isView = true;
  }

  /**
   * Get the default max size for a given type.
   *
   * @param type
   * @return
   */
  private String getMaxSizeForType(ColumnTypeViewEnum type) {
    switch (type) {
      case String:
      case StringList:
        return "" + DEFAULT_STRING_SIZE;
      case Link:
        return "" + MAX_STRING_SIZE;
      default:
        throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  Set<ColumnTypeViewEnum> unsupportedTypesForViews =
    new HashSet<ColumnTypeViewEnum>(
      Arrays.asList(
        ColumnTypeViewEnum.LargeText,
        ColumnTypeViewEnum.MediumText,
        ColumnTypeViewEnum.JSON
      )
    );

  @Override
  public void configure(
    ColumnModel model,
    SelectionPresenter selectionPresenter
  ) {
    view.setSelectionPresenter(selectionPresenter);

    // add column types to the dropdown:
    Arrays
      .asList(ColumnTypeViewEnum.values())
      .stream()
      .filter(val ->
        // Only show JSON ColumnType in experimental mode
        (
          DisplayUtils.isInTestWebsite(cookies) ||
          !ColumnTypeViewEnum.JSON.equals(val)
        )
      )
      .filter((val -> (!isView || !unsupportedTypesForViews.contains(val)))) // keep all values for tables, or keep value if not found in unsupportedTypesForViews.
      .forEach(val -> {
        view.addColumnType(val);
      }); // and add this filtered set to the view
    // this will setup the view with the correct widgets.
    configureViewForType(
      ColumnTypeViewEnum.getViewForType(model.getColumnType())
    );
    // Apply the column model to the view
    ColumnModelUtils.applyColumnModelToRow(model, view);
  }

  @Override
  public void setSelected(boolean select) {
    view.setSelected(select);
  }

  @Override
  public boolean isSelected() {
    return view.isSelected();
  }

  @Override
  public void delete() {
    view.delete();
  }

  @Override
  public void setSelectionPresenter(SelectionPresenter selectionPresenter) {
    view.setSelectionPresenter(selectionPresenter);
  }

  @Override
  public void setSelectVisible(boolean visible) {
    view.setSelectVisible(visible);
  }

  @Override
  public String getId() {
    return view.getId();
  }

  @Override
  public void setId(String id) {
    view.setId(id);
  }

  @Override
  public String getColumnName() {
    return view.getColumnName();
  }

  @Override
  public void setColumnName(String name) {
    view.setColumnName(name);
  }

  @Override
  public ColumnTypeViewEnum getColumnType() {
    return view.getColumnType();
  }

  @Override
  public void setColumnType(ColumnTypeViewEnum type) {
    view.setColumnType(type);
  }

  @Override
  public ColumnFacetTypeViewEnum getFacetType() {
    if (canHaveFacet(view.getColumnType())) {
      return view.getFacetType();
    } else {
      return ColumnFacetTypeViewEnum.None;
    }
  }

  @Override
  public void setFacetType(ColumnFacetTypeViewEnum type) {
    view.setFacetType(type);
  }

  @Override
  public String getMaxSize() {
    return view.getMaxSize();
  }

  @Override
  public void setMaxSize(String maxSize) {
    view.setMaxSize(maxSize);
  }

  @Override
  public String getDefaultValue() {
    return view.getDefaultValue();
  }

  @Override
  public void setDefaultValue(String defaultValue) {
    view.setDefaultValue(defaultValue);
  }

  @Override
  public void setEnumValues(List<String> enums) {
    view.setEnumValues(enums);
  }

  @Override
  public List<String> getEnumValues() {
    return view.getEnumValues();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public boolean validate() {
    boolean isValid = true;
    if (!validateName()) {
      isValid = false;
    }
    if (!validateSize()) {
      isValid = false;
    }
    if (!validateMaxListLength()) {
      isValid = false;
    }
    if (!view.validateDefault()) {
      isValid = false;
    }
    return isValid;
  }

  /**
   * Validate the name
   *
   * @param isValid
   * @return
   */
  public boolean validateName() {
    boolean isValid = true;
    String name = StringUtils.emptyAsNull(view.getColumnName());
    if (name == null) {
      view.setNameError(NAME_CANNOT_BE_EMPTY);
      isValid = false;
    } else {
      view.clearNameError();
    }
    return isValid;
  }

  /**
   * Validate the size
   *
   * @return
   */
  public boolean validateSize() {
    boolean isValid = true;
    ColumnTypeViewEnum type = view.getColumnType();
    if (canHaveSize(type)) {
      String sizeString = view.getMaxSize();
      try {
        int size = Integer.parseInt(sizeString);
        if (size < 1 || size > MAX_STRING_SIZE) {
          view.setSizeError(MUST_BE_A_NUMBER);
          isValid = false;
        }
      } catch (NumberFormatException e) {
        view.setSizeError(MUST_BE_A_NUMBER);
        isValid = false;
      }
    }
    if (isValid) {
      view.clearSizeError();
    }
    return isValid;
  }

  /**
   * Validate the max list length
   *
   * @return
   */
  public boolean validateMaxListLength() {
    boolean isValid = true;
    ColumnTypeViewEnum type = view.getColumnType();
    if (canHaveMaxListLength(type)) {
      String lengthString = view.getMaxListLength();
      try {
        int length = Integer.parseInt(lengthString);
        if (length < 1 || length > MAX_LIST_LENGTH) {
          view.setMaxListLengthError(MUST_BE_A_NUMBER_ONE_AND_HUNDRED);
          isValid = false;
        }
      } catch (NumberFormatException e) {
        view.setMaxListLengthError(MUST_BE_A_NUMBER_ONE_AND_HUNDRED);
        isValid = false;
      }
    }
    if (isValid) {
      view.clearMaxListLengthError();
    }
    return isValid;
  }

  @Override
  public void setToBeDefaultFileViewColumn() {
    view.setToBeDefaultFileViewColumn();
  }

  @Override
  public String getMaxListLength() {
    return view.getMaxListLength();
  }

  @Override
  public void setMaxListLength(String maxListLength) {
    view.setMaxListLength(maxListLength);
  }
}
