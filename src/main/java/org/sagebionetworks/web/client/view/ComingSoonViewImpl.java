package org.sagebionetworks.web.client.view;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
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
	private Footer footerWidget;
	ProvenanceWidget provenanceWidget;
	SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public ComingSoonViewImpl(ComingSoonViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle sageImageBundle, SynapseJSNIUtils synapseJSNIUtils, ProvenanceWidget provenanceWidget,
			ActionMenu actionMenu) {		
		initWidget(binder.createAndBindUi(this));

		this.icons = icons;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		
		this.provenanceWidget = provenanceWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());		
		FileEntity fake = new FileEntity();
		fake.setId("syn123");
		fake.setName("fake");
		EntityBundle bundle = new EntityBundle(fake, null, null, null, null, null, null);
		entityView.setWidget(actionMenu.asWidget(bundle, true, true, new Long(1)));
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		//provenanceWidget.setHeight(400);
		((LayoutContainer)provenanceWidget.asWidget()).setAutoHeight(true);
		
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
	public void setEntity(Entity entity) {
		Long version = null;
		if(entity instanceof Versionable) 
			version = ((Versionable)entity).getVersionNumber();			
		Map<String,String> configMap = new HashMap<String,String>();
		String entityList = DisplayUtils.createEntityVersionString(entity.getId(), null) +","+"syn114241";
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, entityList);
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(false));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(1));		
	    provenanceWidget.configure(null, configMap, null);
	    provenanceWidget.setHeight(800);	
	    entityView.setWidget(provenanceWidget.asWidget());
	}

	
	
	
	
	
	
}
