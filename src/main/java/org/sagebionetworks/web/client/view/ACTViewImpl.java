package org.sagebionetworks.web.client.view;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.ACTPresenter;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ListBox;
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
	ListBox stateComboBox;
	@UiField
	Div userSelectContainer;
	@UiField
	Div synAlertContainer;
	
	@UiField
	Div tableData;
	@UiField
	Button applyStateFilter;
	@UiField
	Button clearStateFilter;
	@UiField
	Button applyUserFilter;
	@UiField
	Button clearUserFilter;
	@UiField
	Div paginationContainer;
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
		
		applyStateFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onApplyStateFilter();
			}
		});
		clearStateFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearStateFilter();
			}
		});
		applyUserFilter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onApplyUserFilter();
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
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setStates(List<String> states) {
		stateComboBox.clear();
		for (String state : states) {
			stateComboBox.addItem(state);
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
	
	@Override
	public String getSelectedState() {
		return stateComboBox.getSelectedValue();
	}
	@Override
	public void setSelectedState(int index) {
		stateComboBox.setSelectedIndex(index);
	}
}
