package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PublicPrivateBadgeViewImpl extends SimplePanel implements PublicPrivateBadgeView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	@Inject
	public PublicPrivateBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.addStyleName("inline-block");
	}
	
	@Override
	public void configure(boolean isPublic) {
		this.clear();
		this.add(DisplayUtils.getShareSettingsDisplay(isPublic, synapseJSNIUtils));

	}	
	
	@Override
	public Widget asWidget() {
		return this;
	}
	

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	@Override
	public void clear() {
		super.clear();
	}
	@Override
	public void showLoading() {
	}
}
