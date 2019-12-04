package org.sagebionetworks.web.client.widget.biodalliance13;

import java.util.List;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtvisualizationwrappers.client.biodalliance13.Biodalliance013dev;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceConfigInterface;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceWidgetViewImpl implements BiodallianceWidgetView {

	private Presenter presenter;

	@UiField
	SimplePanel container;
	@UiField
	Div synAlertContainer;

	Widget widget;
	SynapseJSNIUtils jsniUtils;

	interface BiodallianceWidgetViewImplUiBinder extends UiBinder<Widget, BiodallianceWidgetViewImpl> {
	}

	private static BiodallianceWidgetViewImplUiBinder uiBinder = GWT.create(BiodallianceWidgetViewImplUiBinder.class);

	@Inject
	public BiodallianceWidgetViewImpl(SynapseJSNIUtils jsniUtils) {
		this.jsniUtils = jsniUtils;
		widget = uiBinder.createAndBindUi(this);
		container.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					presenter.viewAttached();
				} ;
			}
		});
	}

	@Override
	public void setContainerId(String id) {
		container.getElement().setId(id);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void showBiodallianceBrowser(final String urlPrefix, final String containerId, final String initChr, final int initViewStart, final int initViewEnd, final BiodallianceConfigInterface currentConfig, final List<BiodallianceSource> sources) {
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				new Biodalliance013dev().show(urlPrefix, containerId, initChr, initViewStart, initViewEnd, currentConfig, sources);
			}

			@Override
			public void onFailure(Throwable reason) {
				jsniUtils.consoleError(reason.getMessage());
			}
		});

	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.add(w);
	}
}
