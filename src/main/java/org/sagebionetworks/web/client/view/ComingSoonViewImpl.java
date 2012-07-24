package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.Node;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ComingSoonViewImpl extends Composite implements ComingSoonView {

	public interface ComingSoonViewImplUiBinder extends UiBinder<Widget, ComingSoonViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel entityView;
		
	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;
	
	@Inject
	public ComingSoonViewImpl(ComingSoonViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle) {		
		initWidget(binder.createAndBindUi(this));

		this.icons = icons;
		this.headerWidget = headerWidget;
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());						
	}



	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page

		BootstrapTable table = new BootstrapTable(); 		
		List<String> headerRow = Arrays.asList(new String[] { "Header 1", "Header 2" });
		List<List<String>> tableHeaderRows = new ArrayList<List<String>>();
		tableHeaderRows.add(headerRow);
		table.setHeaders(tableHeaderRows);
		
		table.setHTML(0, 0, "hello");
		table.setHTML(0, 1, "world");
		table.setHTML(1, 0, "hi");
		table.setHTML(1, 1, "dave");
		table.setHTML(2, 0, "hi");
		table.setHTML(2, 1, "tom");
				
		entityView.clear();
		entityView.add(table);
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

}
