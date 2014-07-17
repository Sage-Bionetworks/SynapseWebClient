package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.footer.Footer;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class TrashViewImpl extends Composite implements TrashView {
	
	public interface TrashViewImplUiBinder extends UiBinder<Widget, TrashViewImpl> {}
	
	private static final String HEADER_CHECKBOX = " ";
	private static final String HEADER_NAME = "Name";
	private static final String HEADER_LOCATION = "Location";
	private static final String HEADER_RESTORE = " ";
	
	private static final int HEADER_CHECKBOX_IDX = 0;
	private static final int HEADER_NAME_IDX = 1;
	private static final int HEADER_LOCATION_IDX = 2;
	private static final int HEADER_RESTORE_IDX = 3;

	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	Button deleteAllButton;
	@UiField
	SimplePanel trashListPanel;	// TODO: BootstrapTable??
	// TODO: More fields related to Messages View
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	BootstrapTable trashList;
	
	@Inject
	public TrashViewImpl(TrashViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget) {
		initWidget(binder.createAndBindUi(this));
		
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		// TODO: Set up UI.
		
		// set up delete all button
		deleteAllButton.setText("Delete All");
		deleteAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteAll();
			}
		});
		
		// add trash list
		trashList = initTable();
		trashListPanel.add(trashList);

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
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		trashList = initTable();
		trashListPanel.clear();
		trashListPanel.add(trashList);
	}
	
	@Override
	public void displayIndividualRow(TrashedEntity trashedEntity) {
		int row = trashList.getRowCount();
		
//		trashList.addClickHandler(new ClickHandler() { 
//            @Override 
//            public void onClick(ClickEvent event) {  
//                 int rowIndex = s.getCellForEvent(event).getRowIndex();
//            } 
//        }); 
		
		// Make checkbox.
		CheckBox cb = new CheckBox();
		
		// Make button
		
		
		trashList.setWidget(row, 0, cb);
		trashList.setText(row, 1, trashedEntity.getEntityName());
		trashList.setText(row, 2, trashedEntity.getOriginalParentId());
	}
	
	/*
	 * Private Methods
	 */
	private BootstrapTable initTable() {

		trashList = new BootstrapTable();
		trashList.addStyleName("trashList-striped trashList-bordered trashList-condensed");
		List<String> headerRow = new ArrayList<String>();
		headerRow.add(HEADER_CHECKBOX_IDX, HEADER_CHECKBOX);
		headerRow.add(HEADER_NAME_IDX, HEADER_NAME);
		headerRow.add(HEADER_LOCATION_IDX, HEADER_LOCATION);
		headerRow.add(HEADER_RESTORE_IDX, HEADER_RESTORE);

		List<List<String>> trashListHeaderRows = new ArrayList<List<String>>();
		trashListHeaderRows.add(headerRow);
		trashList.setHeaders(trashListHeaderRows);			

		trashList.setWidth("100%");		
		
		trashList.getColumnFormatter().setWidth(0, "10%");
		trashList.getColumnFormatter().setWidth(1, "43%");
		trashList.getColumnFormatter().setWidth(2, "33%");
		trashList.getColumnFormatter().setWidth(3, "14%");

		return trashList;
	}
}
