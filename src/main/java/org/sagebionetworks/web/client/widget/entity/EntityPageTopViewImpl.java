package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}
	
	@UiField
	Row projectMetaContainer;
	
	@UiField
	Div tabsUI;
	
	private Presenter presenter;
	
	//project level info
	@UiField
	SimplePanel projectMetadataContainer;
	@UiField
	SimplePanel projectDescriptionContainer;
	@UiField
	SimplePanel projectActionMenuContainer;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SynapseJSNIUtils synapseJSNIUtils,
			CookieProvider cookies) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	

	
	@Override
	public void setPageTitle(String title) {
		synapseJSNIUtils.setPageTitle(title);
	}
	@Override
	public void setActionMenu(Widget w) {
		projectActionMenuContainer.add(w);
	}
	@Override
	public void setProjectMetadata(Widget w) {
		projectMetadataContainer.setWidget(w);
	}
	
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}
	
	@Override
	public void setTabs(Widget w) {
		tabsUI.clear();
		tabsUI.add(w);
	}
}
