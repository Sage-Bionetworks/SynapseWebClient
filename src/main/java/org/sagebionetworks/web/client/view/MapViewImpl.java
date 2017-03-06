package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MapViewImpl implements MapView {

	public interface MapViewImplUiBinder extends UiBinder<Widget, MapViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	Div teamBadgeContainer;
	@UiField
	Div allUsersTitle;
	
	@UiField
	Div mapPanel;
	Widget widget;
	
	private Header headerWidget;
	private Footer footerWidget;
	
	@Inject
	public MapViewImpl(MapViewImplUiBinder binder, 
			Header headerWidget, 
			Footer footerWidget, 
			SynapseJSNIUtils synapseJSNIUtils) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void setMap(Widget w) {
		mapPanel.clear();
		mapPanel.add(w);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public int getClientHeight() {
		return Window.getClientHeight();
	};
	
	@Override
	public void setAllUsersTitleVisible(boolean visible) {
		allUsersTitle.setVisible(visible);
	}
	
	@Override
	public void setTeamBadge(Widget w) {
		teamBadgeContainer.clear();
		teamBadgeContainer.add(w);
	}
	
	@Override
	public void setTeamBadgeVisible(boolean visible) {
		teamBadgeContainer.setVisible(visible);
	}
}
