package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidgetViewImpl extends IsWidget implements DownloadListWidgetView {
	@UiField
	Div synAlertContainer;
	Presenter presenter;
	Widget w;
	interface DownloadListWidgetViewImplUiBinder extends UiBinder<Widget, DownloadListWidgetViewImpl> {}
	
	private static DownloadListWidgetViewImplUiBinder uiBinder = GWT
			.create(DownloadListWidgetViewImplUiBinder.class);
	@Inject
	public DownloadListWidgetViewImpl() {
		w  = uiBinder.createAndBindUi(this);
	}
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	@Override
	public Widget asWidget() {
		return w;
	}
	@Override
	public void clear() {
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
