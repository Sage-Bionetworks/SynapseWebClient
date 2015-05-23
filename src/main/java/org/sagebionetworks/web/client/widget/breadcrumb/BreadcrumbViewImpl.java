package org.sagebionetworks.web.client.widget.breadcrumb;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BreadcrumbViewImpl implements BreadcrumbView {
	private static final int MAX_BREADCRUMB_LENGTH = 25;
	
	public interface BreadcrumbViewImplUiBinder extends
			UiBinder<Widget, BreadcrumbViewImpl> {
	}

	FlowPanel panel;
	private Presenter presenter;


	@Inject
	public BreadcrumbViewImpl() {
		panel = new FlowPanel();
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setLinksList(List<LinkData> breadcrumbs) {
		setLinksList(breadcrumbs, null);
	}

	@Override
	public void setLinksList(List<LinkData> breadcrumbs, String current) {
		panel.clear();
		for (int i = 0; i < breadcrumbs.size(); i++) {
			final LinkData data = breadcrumbs.get(i);
			String text = data.getText();
			text = stubString(text);
			Anchor anchor = new Anchor(text);
			anchor.setIcon(data.getIconType());
			anchor.addStyleName("displayInline");
			anchor.addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					presenter.goTo(data.getPlace());
				}
			});
			if (i > 0) {
				panel.add(new InlineHTML(SafeHtmlUtils.fromSafeConstant(ClientProperties.BREADCRUMB_SEP)));
			}
			panel.add(anchor);
		}
		if (current != null) {
			current = stubString(current);
			SafeHtmlBuilder shb = new SafeHtmlBuilder();
			shb.appendHtmlConstant(ClientProperties.BREADCRUMB_SEP);
			shb.appendEscaped(current);
			panel.add(new InlineHTML(shb.toSafeHtml()));
		}
	}

	@Override
	public void showLoading() {
		// don't
	}

	@Override
	public void clear() {
		panel.clear();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message); 
	}

	/*
	 * Private Methods
	 */
	private String stubString(String text) {
		if(text.length() > MAX_BREADCRUMB_LENGTH) {
			text = text.substring(0, MAX_BREADCRUMB_LENGTH-1) + "...";
		}
		return text;
	}


	
}
