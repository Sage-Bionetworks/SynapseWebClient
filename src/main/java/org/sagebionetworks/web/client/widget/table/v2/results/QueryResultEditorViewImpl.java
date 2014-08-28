package org.sagebionetworks.web.client.widget.table.v2.results;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * An implementation of the QueryResultEditorView with zero business logic.
 * 
 * @author John
 *
 */
public class QueryResultEditorViewImpl implements QueryResultEditorView {
	
	public interface Binder extends UiBinder<Widget, QueryResultEditorViewImpl> {}

	
	@UiField
	ButtonToolBar buttonToolbar;
	@UiField
	SimplePanel tablePanel;
	@UiField
	Button addRowButton;
	@UiField
	Button selectTogglebutton;
	@UiField
	Button selectDropDown;
	@UiField
	AnchorListItem selectAllItem;
	@UiField
	AnchorListItem selectNoneItem;
	@UiField
	Button addRowToolButton;
	@UiField
	Button deleteSelectedButton;
	Presenter presenter;
	
	Widget widget;
	
	@Inject
	public QueryResultEditorViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenterin) {
		this.presenter = presenterin;
		this.addRowButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddRow();
			}
		});
		this.selectTogglebutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onToggleSelect();
			}
		});
		this.selectAllItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectAll();
			}
		});
		this.selectNoneItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSelectNone();
			}
		});
		this.addRowToolButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddRow();
			}
		});
		this.deleteSelectedButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDeleteSelected();
			}
		});
	}

	@Override
	public void setTablePageWidget(TablePageWidget pageWidget) {
		this.tablePanel.add(pageWidget);
	}

}
