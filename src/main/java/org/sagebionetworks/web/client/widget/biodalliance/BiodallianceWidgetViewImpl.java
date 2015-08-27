package org.sagebionetworks.web.client.widget.biodalliance;

import com.google.gwt.core.client.GWT;
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
	
	Widget widget;
	
	interface BiodallianceWidgetViewImplUiBinder extends UiBinder<Widget, BiodallianceWidgetViewImpl> {
	}

	private static BiodallianceWidgetViewImplUiBinder uiBinder = GWT
			.create(BiodallianceWidgetViewImplUiBinder.class);
	
	@Inject
	public BiodallianceWidgetViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		container.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					presenter.viewAttached();
				};
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
}
