package org.sagebionetworks.web.client.view;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.client.exceptions.SynapseBadRequestException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
	Button jsExceptionButton;
		
	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;
	private Footer footerWidget;
	ProvenanceWidget provenanceWidget;
	SynapseJSNIUtils synapseJSNIUtils;
	JiraURLHelper jiraErrorHelper;
	SynapseClientAsync synapseClient;
	AuthenticationController authenticationController;
	
	@Inject
	public ComingSoonViewImpl(ComingSoonViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SynapseClientAsync synapseClient,
			SageImageBundle sageImageBundle, SynapseJSNIUtils synapseJSNIUtils, ProvenanceWidget provenanceWidget,
			PortalGinInjector ginInjector,
			JiraURLHelper jiraErrorHelper, AuthenticationController authenticationController ) {		
		initWidget(binder.createAndBindUi(this));

		this.icons = icons;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseClient = synapseClient;
		this.jiraErrorHelper = jiraErrorHelper;
		this.provenanceWidget = provenanceWidget;
		this.authenticationController = authenticationController;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());	
		jsExceptionButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				throwException();
			}
		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		//provenanceWidget.setHeight(400);
		
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}


	private static native void throwException() /*-{
		throw "something bad happened in js";
	}-*/;
	
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
		
	}
	
}
