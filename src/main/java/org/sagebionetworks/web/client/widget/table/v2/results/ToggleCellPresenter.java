package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Cell that can toggle between an editor and viewer. This class is a
 * presenter that encapsulates the state and business logic.
 * 
 * @author John
 * 
 */
public class ToggleCellPresenter implements ToggleCell {

	HasWidgets container;
	ColumnTypeViewEnum type;
	CellFactory factory;
	Cell renderer;
	Cell editor;
	boolean autoFocus;

	/**
	 * Create a new toggle cell for each cell. This cell can be re-used for any
	 * cell of the same type.
	 * 
	 * Note: By default this cell starts off as a renderer. The editor is only
	 * created when toggled into editing.
	 * 
	 * @param type
	 * @param factory
	 * @param container
	 */
	public ToggleCellPresenter(ColumnTypeViewEnum type, CellFactory factory,
			HasWidgets container) {
		super();
		this.type = type;
		this.factory = factory;
		this.container = container;
		// Start with a viewer
		renderer = this.factory.createRenderer(type);
		this.container.add(renderer.asWidget());
		renderer.setVisible(true);
	}

	@Override
	public void toggleEdit(boolean isEditing) {
		if (isEditing && (editor == null || !editor.isVisible())) {
			// enter edit
			if (editor == null) {
				editor = factory.createEditor(type);
			}
			// transfer from the view to the editor
			transferValueAndToggle(renderer, editor, container);
		} else if (!renderer.isVisible()) {
			// enter render
			// transfer from the view to the editor
			transferValueAndToggle(editor, renderer, container);
		}
	}

	@Override
	public String getValue() {
		return getActive().getValue();
	}

	/**
	 * Transfer data form the source to destination. Then hide the sources and
	 * show the destination.
	 * 
	 * @param source
	 * @param destination
	 */
	private static void transferValueAndToggle(Cell source, Cell destination, HasWidgets container) {
		destination.setValue(source.getValue());
		source.setVisible(false);
		destination.setVisible(true);
		container.clear();
		container.add(destination.asWidget());
	}

	@Override
	public void setValue(String value) {
		getActive().setValue(value);
	}

	/**
	 * Get the active cell is always the visible cell.
	 * 
	 * @return
	 */
	private Cell getActive() {
		if (this.renderer.isVisible()) {
			return renderer;
		} else {
			return editor;
		}
	}

	@Override
	public Widget asWidget() {
		return (Widget) container;
	}

	@Override
	public boolean isVisible() {
		return asWidget().isVisible();
	}

	@Override
	public void setVisible(boolean isVisible) {
		asWidget().setVisible(isVisible);
	}

}
