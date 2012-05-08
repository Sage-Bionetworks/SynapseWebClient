package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.TempPropertyPresenter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.table.QueryServiceTableResourceProvider;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GovernanceViewImpl extends Composite implements GovernanceView {

	public interface GovernanceViewImplUiBinder extends UiBinder<Widget, GovernanceViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
		
	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;
	
	private TempPropertyPresenter props;

	
	@Inject
	public GovernanceViewImpl(GovernanceViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle,
			QueryServiceTableResourceProvider queryServiceTableResourceProvider,
			TempPropertyPresenter props) {		
		initWidget(binder.createAndBindUi(this));

		this.icons = icons;
		this.headerWidget = headerWidget;
		
		this.props = props;
		this.props.initializeWithTestData();
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
				
	}



	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
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

}
