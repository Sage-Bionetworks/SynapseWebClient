package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListWidgetViewImpl extends LayoutContainer implements EntityListWidgetView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityListRenderer renderer;

	@Inject
	public EntityListWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void configure() {
		this.removeAll();		
		renderer = new EntityListRenderer(iconsImageBundle, synapseJSNIUtils, false);
		this.add(renderer);
	}	
	
	@Override
	public void setEntityGroupRecordDisplay(final int rowIndex,
			final EntityGroupRecordDisplay entityGroupRecordDisplay, boolean isLoggedIn) {
		renderer.setRow(rowIndex, entityGroupRecordDisplay, isLoggedIn);		
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	

}
