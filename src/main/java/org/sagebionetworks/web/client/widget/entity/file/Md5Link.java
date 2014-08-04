package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class Md5Link extends SimplePanel {
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public Md5Link(SynapseJSNIUtils synapseJSNIUtils) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		addStyleName("inline-block font-italic");
	}
	
	public void configure(final String md5) {
		this.clear();
		Anchor md5Link = new Anchor("md5");
		md5Link.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showMd5Dialog(md5);
			}
		});
		DisplayUtils.addTooltip(md5Link, md5, Placement.BOTTOM);
		this.add(md5Link);
	}
	
	private void showMd5Dialog(String md5) {
		final Dialog window = new Dialog();
		window.setSize(220, 85);
		window.setPlain(true);
		window.setModal(true);
		window.setHeading("md5");
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span style=\"margin-left: 10px;\">"+md5+"</span>");
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		window.add(htmlPanel);
		
	    window.setButtons(Dialog.OK);
	    window.setButtonAlign(HorizontalAlignment.CENTER);
	    window.setHideOnButtonClick(true);
		window.setResizable(false);
		window.show();
	}
}
