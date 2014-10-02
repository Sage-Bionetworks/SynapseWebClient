package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.view.TeamSearchViewImpl.TeamSearchViewImplUiBinder;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.MemberListWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PeopleSearchViewImpl extends Composite implements PeopleSearchView {
	public interface PeopleSearchViewImplUiBinder extends UiBinder<Widget, PeopleSearchViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel searchBoxPanel;
	@UiField
	FlowPanel mainContainer;
	@UiField
	SimplePanel paginationPanel;
	
	private Header headerWidget;
	private Footer footerWidget;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJsniUtils;
	private MemberListWidget memberListWidget;
	
	
	@Inject
	public PeopleSearchViewImpl(PeopleSearchViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget, 
			SageImageBundle sageImageBundle,
			SynapseJSNIUtils synapseJsniUtils,
			MemberListWidget memberListWidget) {
		initWidget(binder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJsniUtils = synapseJsniUtils;
		this.memberListWidget = memberListWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
//	@Override
//	public Widget asWidget() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void showLoading() {
		mainContainer.clear();
		mainContainer.add(DisplayUtils.getLoadingWidget(sageImageBundle));
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
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		
	}

}
