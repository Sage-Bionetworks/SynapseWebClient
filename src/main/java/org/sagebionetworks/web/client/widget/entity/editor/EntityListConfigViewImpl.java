package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.SelectableListView;
import org.sagebionetworks.web.client.widget.SelectionToolbar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListConfigViewImpl implements EntityListConfigView {
	public interface Binder extends UiBinder<Widget, EntityListConfigViewImpl> {}
	Widget widget;
	private Presenter presenter;
	private SelectableListView.Presenter selectionHandler;
	@UiField
	Button addEntityButton;
	@UiField
	SelectionToolbar selectionToolbar;
	@UiField
	Button editNoteButton;
	@UiField
	Div entityListContainer;
	@UiField
	Div widgets;
	
	@Inject
	public EntityListConfigViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		addEntityButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddRecord();
			}
		});
		selectionToolbar.setDeleteClickedCallback(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionHandler.deleteSelected();
			}
		});
		selectionToolbar.setMovedownClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionHandler.onMoveDown();
			}
		});
		
		selectionToolbar.setMoveupClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionHandler.onMoveUp();
			}
		});
		selectionToolbar.setSelectAllClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionHandler.selectAll();
			}
		});
		selectionToolbar.setSelectNoneClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionHandler.selectNone();
			}
		});
		editNoteButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUpdateNote();
			}
		});
	}
	@Override
	public void setSelectionToolbarHandler(
			org.sagebionetworks.web.client.widget.SelectableListView.Presenter selectableItemList) {
		selectionHandler = selectableItemList;
	}
	
	@Override
	public void initView() {
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}	
	
	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void setCanDelete(boolean canDelete) {
		selectionToolbar.setCanDelete(canDelete);
	}
	
	@Override
	public void setCanMoveDown(boolean canMoveDown) {
		selectionToolbar.setCanMoveDown(canMoveDown);
	}
	@Override
	public void setCanMoveUp(boolean canMoveUp) {
		selectionToolbar.setCanMoveUp(canMoveUp);
	}
	@Override
	public void setButtonToolbarVisible(boolean visible) {
		selectionToolbar.setVisible(visible);
		editNoteButton.setVisible(visible);
	}
	@Override
	public void setCanEditNote(boolean canEditNote) {
		editNoteButton.setEnabled(canEditNote);
	}
	@Override
	public void clear() {
	}

	@Override
	public void setEntityListWidget(Widget w) {
		entityListContainer.clear();
		entityListContainer.add(w);
	}
	
	@Override
	public void addWidget(Widget w) {
		widgets.add(w);
	}
	
	/*
	 * Private Methods
	 */


}
