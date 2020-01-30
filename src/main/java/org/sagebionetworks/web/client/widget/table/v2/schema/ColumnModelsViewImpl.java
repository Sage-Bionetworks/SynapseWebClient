package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.ButtonUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A table view of a list of ColumnModels
 * 
 * @author jmhill
 *
 */
public class ColumnModelsViewImpl extends Composite implements ColumnModelsView {

	public interface Binder extends UiBinder<Widget, ColumnModelsViewImpl> {
	}

	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	Button selectTogglebutton;
	@UiField
	Button selectDropDown;
	@UiField
	AnchorListItem selectAllItem;
	@UiField
	AnchorListItem selectNoneItem;
	@UiField
	Button moveUpButton;
	@UiField
	Button moveDownButton;
	@UiField
	Button deleteSelectedButton;
	@UiField
	Table table;
	@UiField
	TBody tableBody;
	@UiField
	Button addColumnButton;
	@UiField
	Button editColumnsButton;
	@UiField
	Button addDefaultViewColumnsButton;
	@UiField
	Button addAnnotationColumnsButton;
	@UiField
	Span extraButtonsContainer;
	ViewType viewType;
	Presenter presenter;


	@Inject
	public ColumnModelsViewImpl(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setEditHandler(final EditHandler handler) {
		// Edit clicks
		this.editColumnsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.onEditColumns();
			}
		});
	}

	@Override
	public void setPresenter(Presenter setPresenter) {
		this.presenter = setPresenter;
		// Add clicks
		this.addColumnButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addNewColumn();
			}
		});
		// Toggle select
		this.selectTogglebutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.toggleSelect();
			}
		});
		// Select all
		this.selectAllItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectAll();
			}
		});
		// select none
		this.selectNoneItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectNone();
			}
		});
		// move up
		this.moveUpButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMoveUp();
			}
		});
		// move down
		this.moveDownButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMoveDown();
			}
		});
		// delete selected
		this.deleteSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteSelected();
			}
		});
		addDefaultViewColumnsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddDefaultViewColumns();
			}
		});
		addAnnotationColumnsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddAnnotationColumns();
			}
		});
	}


	@Override
	public void configure(ViewType type, boolean isEditable) {
		// Clear any rows
		tableBody.clear();
		// hide the toolbar until data is added
		buttonToolbar.setVisible(false);
		this.viewType = type;
		if (ViewType.VIEWER.equals(type)) {
			editColumnsButton.setVisible(isEditable);
			addColumnButton.setVisible(false);
		} else {
			editColumnsButton.setVisible(false);
			addColumnButton.setVisible(true);
		}
	}

	@Override
	public void addColumn(ColumnModelTableRow row) {
		tableBody.add(row);
		if (ViewType.EDITOR.equals(this.viewType)) {
			if (!this.buttonToolbar.isVisible()) {
				this.buttonToolbar.setVisible(true);
			}
		}
	}

	@Override
	public void setCanDelete(boolean canDelete) {
		ButtonUtils.setEnabledAndType(canDelete, this.deleteSelectedButton, ButtonType.DANGER);
	}

	@Override
	public void setCanMoveUp(boolean canMoveUp) {
		ButtonUtils.setEnabledAndType(canMoveUp, this.moveUpButton, ButtonType.INFO);
	}

	@Override
	public void setCanMoveDown(boolean canMoveDown) {
		ButtonUtils.setEnabledAndType(canMoveDown, this.moveDownButton, ButtonType.INFO);
	}

	@Override
	public void moveColumn(ColumnModelTableRow row, int index) {
		this.tableBody.remove(row);
		this.tableBody.insert(row.asWidget(), index);
	}

	@Override
	public boolean isDeleteEnabled() {
		return this.deleteSelectedButton.isEnabled();
	}

	@Override
	public boolean isMoveUpEnabled() {
		return this.moveUpButton.isEnabled();
	}

	@Override
	public boolean isMoveDownEnabled() {
		return this.moveDownButton.isEnabled();
	}

	@Override
	public void setAddDefaultViewColumnsButtonVisible(boolean visible) {
		addDefaultViewColumnsButton.setVisible(visible);
	}

	@Override
	public void setAddAnnotationColumnsButtonVisible(boolean visible) {
		addAnnotationColumnsButton.setVisible(visible);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void addButton(IsWidget widget) {
		extraButtonsContainer.add(widget);
	}
}
