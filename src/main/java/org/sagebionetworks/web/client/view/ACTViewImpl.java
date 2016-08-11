package org.sagebionetworks.web.client.view;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTViewImpl implements ACTView {

	public interface ACTViewImplUiBinder extends UiBinder<Widget, ACTViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	@UiField
	DropDownMenu stateDropdownMenu;
	@UiField
	Div userSelectContainer;
	@UiField
	Div synAlertContainer;
	
	@UiField
	Div tableData;
	@UiField
	Button clearStateFilter;
	@UiField
	Button clearUserFilter;
	@UiField
	Div paginationContainer;
	@UiField
	Span currentState;
	@UiField
	Div currentUserContainer;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	Widget widget;
	@Inject
	public ACTViewImpl(ACTViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget
			) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		clearStateFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearStateFilter();
			}
		});
		clearUserFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearUserFilter();
			}
		});


	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {		
	}
	@Override
	public void setSelectedStateText(String state) {
		currentState.setText(state);
	}
	@Override
	public void setSelectedUserBadge(Widget w) {
		currentUserContainer.add(w);
	}
	@Override
	public void setSelectedUserBadgeVisible(boolean visible) {
		currentUserContainer.setVisible(visible);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setStates(List<String> states) {
		stateDropdownMenu.clear();
		for (final String state : states) {
			AnchorListItem item = new AnchorListItem(state);
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onStateSelected(state);
				}
			});
			stateDropdownMenu.add(item);
		}
	}
	
	@Override
	public void setUserPickerWidget(Widget w) {
		userSelectContainer.clear();
		userSelectContainer.add(w);
	}
	@Override
	public void addRow(Widget w) {
		tableData.add(w);
	}
	@Override
	public void clearRows() {
		tableData.clear();
	}
	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void updatePagination(List<PaginationEntry> entries) {
		paginationContainer.clear();
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.setStyleName("pagination pagination-lg");
		
		if(entries != null) {
			for(PaginationEntry pe : entries) {
				if(pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()), "active");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), pe.getStart()));
			}
		}
		
		if (entries.size() > 1) {
			paginationContainer.add(ul);
		}
	}
	
	private Anchor createPaginationAnchor(String anchorName, final int newStart) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.loadData((long)newStart);
			}
		});
		return a;
	}
}
