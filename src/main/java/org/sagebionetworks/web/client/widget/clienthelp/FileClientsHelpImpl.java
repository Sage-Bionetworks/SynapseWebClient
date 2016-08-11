package org.sagebionetworks.web.client.widget.clienthelp;

import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileClientsHelpImpl implements FileClientsHelp {
	@UiField
	SpanElement id1;
	@UiField
	SpanElement id2;
	@UiField
	SpanElement id3;
	@UiField
	SpanElement id4;
	@UiField
	SpanElement id5;
	@UiField
	SpanElement id6;
	@UiField
	SpanElement id7;
	@UiField
	SpanElement id8;
	Widget widget;
	SynapseJSNIUtils synapseJsniUtils;
	String entityId = null;
	public interface Binder extends UiBinder<Widget, FileClientsHelpImpl> {}

	@Inject
	public FileClientsHelpImpl(Binder binder, SynapseJSNIUtils synapseJsniUtils) {
		this.widget = binder.createAndBindUi(this);
		this.synapseJsniUtils = synapseJsniUtils;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void configure(String entityId) {
		this.entityId = entityId;
		id1.setInnerHTML(entityId);
		id2.setInnerHTML(entityId);
		id3.setInnerHTML(entityId);
		id4.setInnerHTML(entityId);
		id5.setInnerHTML(entityId);
		id6.setInnerHTML(entityId);
		id7.setInnerHTML(entityId);
		id8.setInnerHTML(entityId);
		synapseJsniUtils.highlightCodeBlocks();
	}
}
