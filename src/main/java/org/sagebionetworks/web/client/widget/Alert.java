package org.sagebionetworks.web.client.widget;


import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class Alert extends Composite {

	public static enum AlertType {
		Error("error"),
		Success("success"),
		Info("info"),
		Warn("");
		
		public final String type;
		AlertType(String type) {
			this.type = type;
		}
	}
	
	private FlowPanel panel;
	private String heading;
	private String body;
	private boolean fullHeader = false;
	private boolean blockStyle = false;
	
	public Alert(String alert) {
		init(null, alert);
	}
	
	public Alert(String heading, String alert) {
		init(heading, alert);
	}
	
	private void init(String heading, String body) {
		panel = new FlowPanel();

		panel.setStyleName("alert");
		panel.addStyleName("fade");
		panel.addStyleName("in");
		
		this.heading = heading;
		this.body = body;

		initWidget(panel);
		build();
	}
	
	public void build() {
		
		SafeHtmlBuilder text = new SafeHtmlBuilder();

		text.appendHtmlConstant("<button class=\"close\" data-dismiss=\"alert\">×</button>");
		if (blockStyle || fullHeader) {
			panel.addStyleDependentName("block");
		}

		if (heading != null) {
			text.appendHtmlConstant(fullHeader?"<h4 class=\"alert-heading\">":"<strong>");
			text.appendEscaped(heading);
			text.appendHtmlConstant(fullHeader?"</h4>":"</strong> ");
		}

		text.appendEscaped(body);

		panel.getElement().setInnerHTML(text.toSafeHtml().asString());

	}
	
	public void setBlockStyle(boolean isBlock) {
		panel.setStyleDependentName("block", isBlock);
	}
	
	public void setAlertType(AlertType alertType) {
		for (AlertType alert : AlertType.values()) {
			panel.setStyleDependentName(alert.type, alertType == alert);
		}
	}

	public void setFullHeader(boolean fullHeader) {
		this.fullHeader = fullHeader;
		build();
	}

	public boolean isFullHeader() {
		return fullHeader;
	}

	public void setHeading(String heading) {
		this.heading = heading;
		build();
	}

	public String getHeading() {
		return heading;
	}

	public void setBody(String body) {
		this.body = body;
		build();
	}

	public String getBody() {
		return body;
	}
}
