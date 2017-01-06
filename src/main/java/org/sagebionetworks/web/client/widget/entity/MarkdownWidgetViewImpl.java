package org.sagebionetworks.web.client.widget.entity;


import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.NavPills;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MarkdownWidgetViewImpl implements MarkdownWidgetView {
	public interface Binder extends UiBinder<Widget, MarkdownWidgetViewImpl> {}
	
	Widget widget;
	SynapseJSNIUtils jsniUtils;
	
	@UiField
	HTMLPanel contentPanel;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	Italic emptyPanel;
	@UiField
	NavPills navContainer;
	Presenter presenter;
	List<AnchorListItem> items;
	@Inject
	public MarkdownWidgetViewImpl(final Binder uiBinder,
			SynapseJSNIUtils jsniUtils) {
		widget = uiBinder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		items = new ArrayList<AnchorListItem>();
	}
	
	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void setEmptyVisible(boolean isVisible) {
		emptyPanel.setVisible(isVisible);
	}

	@Override
	public void setMarkdown(final String result) {
		contentPanel.getElement().setInnerHTML(result);
	}
	
	@Override
	public void callbackWhenAttached(final Callback callback) {
		final Timer t = new Timer() {
	      @Override
	      public void run() {
	    	  if (contentPanel.isAttached()) {
	    		  callback.invoke();
	    	  } else {
	    		  schedule(100);
	    	  }
	      }
	    };

	    t.schedule(100);
	}

	@Override
	public ElementWrapper getElementById(String id) {
		Element ele = contentPanel.getElementById(id);
		return ele == null ? null : new ElementWrapper(ele);
	}

	@Override
	public void addWidget(Widget widget, String divID) {
		contentPanel.add(widget, divID);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	
	@Override
	public void addNavItem(String text, final ElementWrapper target, boolean active, final String suffix) {
		final AnchorListItem item = new AnchorListItem(text);
		item.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.hideAllNavPanels(suffix);
				markAllItemsInactive();
				target.setVisible(true);
				item.setActive(true);
			}
		});
		
		item.setActive(active);
		target.setVisible(active);
		items.add(item);
		navContainer.add(item);
	}
	
	private void markAllItemsInactive() {
		for (AnchorListItem item : items) {
			item.setActive(false);
		}
	}
	
	@Override
	public void clearMarkdown() {
		items.clear();
		navContainer.clear();
		contentPanel.clear();
		setMarkdown("");
	}
}
