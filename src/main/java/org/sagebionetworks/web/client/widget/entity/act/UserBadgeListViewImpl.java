package org.sagebionetworks.web.client.widget.entity.act;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.widget.SelectionToolbar;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class UserBadgeListViewImpl implements UserBadgeListView {

	public interface Binder extends UiBinder<Widget, UserBadgeListViewImpl> {}
	
	Widget widget;
	Presenter presenter;
	
	@UiField
	SelectionToolbar selectionToolbar;
	@UiField
	TBody userBadgeContainer;
	@UiField
	Div userSelectorContainer;
	@Inject
	public UserBadgeListViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		
		selectionToolbar.hideReordering();
		
		selectionToolbar.setDeleteClickedCallback(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteSelected();
			}
		});
		selectionToolbar.setSelectAllClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectAll();
			}
		});
		selectionToolbar.setSelectNoneClicked(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.selectNone();
			}
		});
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
	public void setSelectorWidget(Widget widget) {
		userSelectorContainer.clear();
		userSelectorContainer.add(widget);
	}
	
	@Override
	public void setUploadWidgetVisible(boolean visible) {
		userSelectorContainer.setVisible(visible);
	}
	@Override
	public void addUserBadge(String userId) {
		HTML ta = new HTML();
		ta.setHTML(userId);
		userBadgeContainer.add(ta.asWidget());
	}
	
	@Override
	public void clearFileLinks() {
		userBadgeContainer.clear();		
	}
	
	@Override
	public void setToolbarVisible(boolean visible) {
		selectionToolbar.setVisible(visible);
	}

	@Override
	public void setCanDelete(boolean canDelete) {
		selectionToolbar.setCanDelete(canDelete);
	}
}
