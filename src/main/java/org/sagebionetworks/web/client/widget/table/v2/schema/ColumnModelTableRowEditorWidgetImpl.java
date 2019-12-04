package org.sagebionetworks.web.client.widget.table.v2.schema;

import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnFacetTypeViewEnum.None;
import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnFacetTypeViewEnum.Range;
import static org.sagebionetworks.web.client.widget.table.v2.schema.ColumnFacetTypeViewEnum.Values;
import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Control logic for a single ColumnModelTableRowEditor.
 * 
 * @author John
 *
 */
public class ColumnModelTableRowEditorWidgetImpl implements ColumnModelTableRowEditorWidget, ColumnModelTableRowEditorView.TypePresenter {

	public static final String MUST_BE_A_NUMBER = "Must be: 1 - 1000";
	public static final String NAME_CANNOT_BE_EMPTY = "Name cannot be empty";
	public static final int DEFAULT_STRING_SIZE = 50;
	public static final int MAX_STRING_SIZE = 1000;
	ColumnModelTableRowEditorView view;
	ColumnTypeViewEnum currentType;
	CellFactory factory;
	String maxSize = null;
	boolean canHaveDefault = true;

	@Inject
	public ColumnModelTableRowEditorWidgetImpl(ColumnModelTableRowEditorView view, CellFactory factory) {
		this.view = view;
		this.factory = factory;
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
			view.setMaxSize(getMaxSizeForType(newType));
			view.setSizeFieldVisible(true);
		} else {
			view.setMaxSize(null);
			view.setSizeFieldVisible(false);
		}

		configureFacetsForType(newType);
		view.setRestrictValuesVisible(canHaveRestrictedValues(newType));
		view.clearSizeError();
		this.currentType = newType;
		CellEditor defaultEditor = getDefaultEditorForType(newType);
		view.setDefaultEditor(defaultEditor);
		view.setDefaultEditorVisible(canHaveDefault(newType));
	}

	@Override
	public void setCanHaveDefault(boolean canHaveDefault) {
		this.canHaveDefault = canHaveDefault;
	}

	public boolean canHaveDefault(ColumnTypeViewEnum type) {
		if (canHaveDefault) {
			switch (type.getType()) {
				case ENTITYID:
				case FILEHANDLEID:
				case USERID:
				case LARGETEXT:
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
				return true;
			case Link:
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
				return true;
			case Integer:
				return true;
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
				view.setFacetValues(None.toString(), Values.toString(), Range.toString());
				view.setFacetVisible(true);
				break;
			case String:
			case Boolean:
			case User:
			case Entity:
				view.setFacetValues(None.toString(), Values.toString());
				view.setFacetVisible(true);
				break;
			case Double:
			case Date:
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

	/**
	 * Get the default max size for a given type.
	 * 
	 * @param type
	 * @return
	 */
	private String getMaxSizeForType(ColumnTypeViewEnum type) {
		switch (type) {
			case String:
				return "" + DEFAULT_STRING_SIZE;
			case Link:
				return "" + MAX_STRING_SIZE;
			default:
				throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	@Override
	public void configure(ColumnModel model, SelectionPresenter selectionPresenter) {
		view.setSelectionPresenter(selectionPresenter);
		// this will setup the view with the correct widgets.
		configureViewForType(ColumnTypeViewEnum.getViewForType(model.getColumnType()));
		// Apply the column model to the view
		ColumnModelUtils.applyColumnModelToRow(model, view);
	}


	@Override
	public IsWidget getWidget(int index) {
		return view.getWidget(index);
	}

	@Override
	public int getWidgetCount() {
		return view.getWidgetCount();
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

	@Override
	public void setToBeDefaultFileViewColumn() {
		view.setToBeDefaultFileViewColumn();
	}

}
