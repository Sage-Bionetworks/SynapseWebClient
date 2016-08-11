package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListWidgetViewImpl extends FlowPanel implements EntityListWidgetView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityListRenderer renderer;
	private PortalGinInjector ginInjector;
	
	@Inject
	public EntityListWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void configure(boolean showDescription) {
		this.clear();
		renderer = new EntityListRenderer(iconsImageBundle, synapseJSNIUtils, ginInjector, false, showDescription);
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
