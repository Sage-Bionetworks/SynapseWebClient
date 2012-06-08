package org.sagebionetworks.web.client.widget.breadcrumb;

import java.util.List;

import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BreadcrumbViewImpl extends LayoutContainer implements BreadcrumbView {
	private static final int MAX_BREADCRUMB_LENGTH = 25;
	
	public interface BreadcrumbViewImplUiBinder extends
			UiBinder<Widget, BreadcrumbViewImpl> {
	}

	HorizontalPanel panel;
	private Presenter presenter;


	@Inject
	public BreadcrumbViewImpl() {
		panel = new HorizontalPanel();
		this.add(panel);
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
	public void setLinksList(List<LinkData> breadcrumbs) {
		setLinksList(breadcrumbs, null);
	}

	@Override
	public void setLinksList(List<LinkData> breadcrumbs, String current) {
		panel.removeAll();
		panel.layout();
		for (int i = 0; i < breadcrumbs.size(); i++) {
			final LinkData data = breadcrumbs.get(i);
			String text = data.getText();
			text = stubString(text);
			Anchor anchor = new Anchor(text);
			anchor.addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					presenter.goTo(data.getPlace());
				}
			});
			if (i > 0) {
				panel.add(new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.BREADCRUMB_SEP)));
			}
			panel.add(anchor);
		}
		if (current != null) {
			current = stubString(current);
			SafeHtmlBuilder shb = new SafeHtmlBuilder();
			shb.appendHtmlConstant(DisplayUtils.BREADCRUMB_SEP);
			shb.appendEscaped(current);
			panel.add(new HTML(shb.toSafeHtml()));
		}
		panel.layout(true);
		this.layout(true);
	}

	@Override
	public void showLoading() {
		// don't
	}

	@Override
	public void clear() {
		panel.removeAll();
		panel.layout();
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
