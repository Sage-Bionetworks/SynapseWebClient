package org.sagebionetworks.web.client.widget;


import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;

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
	private Button closeButton;
	private InlineHTML headingEl;
	private InlineLabel bodyEl;

	private String heading;

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

		// The use of setStyleName throughout this class is INTENTIONAL
		// It allows us to get rid of any classes that GWT
		// might decide to helpfully add by default
		panel.setStyleName("alert");
		panel.addStyleName("fade");
		panel.addStyleName("in");
		
		this.closeButton = new Button("x");
		closeButton.setStyleName("close");
		closeButton.getElement().setAttribute("data-dismiss", "alert");
		
		this.heading = heading;
		this.headingEl = new InlineHTML();
		this.headingEl.setStyleName("heading");
		this.headingEl.setText(this.heading);
		
		this.bodyEl = new InlineLabel(body);
		this.bodyEl.setStyleName("");
		
		panel.add(closeButton);
		panel.add(this.headingEl);
		panel.add(this.bodyEl);
		
		initWidget(panel);
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
		if (fullHeader) {
			SafeHtmlBuilder builder = new SafeHtmlBuilder();
			builder.appendHtmlConstant("<h4 class=\"alert-heading\">");
			builder.appendEscaped(this.heading);
			builder.appendHtmlConstant("</h4>");
			this.headingEl.setHTML(builder.toSafeHtml());
		} else {
			this.headingEl.setHTML("");
			this.headingEl.setText(this.heading + " ");
		}
	}
	
	public boolean isFullHeader() {
		return fullHeader;
	}

	public void setHeading(String heading) {
		this.heading = heading;
		this.headingEl.setText(this.heading + " ");
	}

	public String getHeading() {
		return this.heading;
	}

	public void setBody(String body) {
		this.bodyEl.setText(body);
	}

	public String getBody() {
		return bodyEl.getText();
	}
	
	public void setTimeout(int delay) {
		final Button button = this.closeButton;
		
		Timer timer = new Timer() {

			@Override
			public void run() {
				button.click();
			}
		};
		timer.schedule(delay);
	}
}
