package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Code;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ClientsHelpImpl implements ClientsHelp {
	@UiField
	Code commandLineCode;
	@UiField
	Code javaCode;
	@UiField
	Code pythonCode;
	@UiField
	Code rCode;

	Widget widget;
	SynapseJSNIUtils synapseJsniUtils;
	public interface Binder extends UiBinder<Widget, ClientsHelpImpl> {}

	@Inject
	public ClientsHelpImpl(Binder binder, SynapseJSNIUtils synapseJsniUtils) {
		this.widget = binder.createAndBindUi(this);
		this.synapseJsniUtils = synapseJsniUtils;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
	@Override
	public void configure(String commandLine, String java, String python, String r) {
		commandLineCode.setText(commandLine);
		javaCode.setText(java);
		pythonCode.setText(python);
		rCode.setText(r);
		synapseJsniUtils.highlightCodeBlocks();
	}
}
