package org.sagebionetworks.web.client.widget.entity;


import org.gwtbootstrap3.client.ui.html.Italic;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
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
	
	@Inject
	public MarkdownWidgetViewImpl(final Binder uiBinder,
			SynapseJSNIUtils jsniUtils) {
		widget = uiBinder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
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
		jsniUtils.highlightCodeBlocks();
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
	public void clearMarkdown() {
		contentPanel.clear();
		setMarkdown("");
	}
	
}
