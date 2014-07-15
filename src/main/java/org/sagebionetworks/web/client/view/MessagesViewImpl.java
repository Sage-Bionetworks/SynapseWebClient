package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.ChangeUsernameView.Presenter;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.footer.Footer;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MessagesViewImpl extends Composite implements MessagesView {
	
	public interface MessagesViewImplUiBinder extends UiBinder<Widget, MessagesViewImpl> {}
	// TODO: What is this for? Do I need this?
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	// TODO: More fields related to Messages View
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public MessagesViewImpl(MessagesViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget) {
		initWidget(binder.createAndBindUi(this));
		
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		// TODO: Set up UI.
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
		// TODO Auto-generated method stub
		// IMPLEMENT!!
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		// IMPLEMENT!!
	}
}
