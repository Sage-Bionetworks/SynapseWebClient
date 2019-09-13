package org.sagebionetworks.web.client;

import com.google.inject.Inject;

public class SynapseStatusDetector {

	public static final int INTERVAL_MS = 1000*60; //check once every minute
	PopupUtilsView popupUtils;
	GWTWrapper gwt;
	public static final String STATUS_PAGE_IO_PAGE = "kh896k90gyvg";
	@Inject
	public SynapseStatusDetector(
			GWTWrapper gwt,
			PopupUtilsView popupUtils) {
		this.gwt = gwt;
		this.popupUtils = popupUtils;
	}
	
	public void start() {
		gwt.scheduleFixedDelay(() -> {
			_getCurrentStatus(this);
			_getActiveScheduledMaintenance(this);
		}, INTERVAL_MS);
	}
	
	public void showScheduledMaintenance(String moreInfo) {
		String moreInfoString = moreInfo != null ? ": " + moreInfo : "";
		popupUtils.showInfo("<a href=\"http://status.synapse.org/\" target=\"_blank\" class=\"color-white\">Under Maintenance" + moreInfoString + "</a>", INTERVAL_MS - 1200);
	}
	
	public void showOutage(String info) {
		popupUtils.showError("<a href=\"http://status.synapse.org/\" target=\"_blank\" class=\"color-white\">" + info + "</a>", INTERVAL_MS - 1200);
	}
	
	private static native void _getCurrentStatus(SynapseStatusDetector x) /*-{
		var sp = new $wnd.StatusPage.page({
			page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
		});
		sp.status({
			success : function(data) {
				if (data.status.indicator !== 'none') {
					// Houston, we have a problem...
					var description = data.status.description;
					x.@org.sagebionetworks.web.client.SynapseStatusDetector::showOutage(Ljava/lang/String;)(description);
				}
			}
		});
	}-*/;
	
	private static native void _getActiveScheduledMaintenance(SynapseStatusDetector x) /*-{
		var sp = new $wnd.StatusPage.page({
			page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
		});
		sp.scheduled_maintenances({
			filter : 'active',
			success : function(data) {
				if (data.scheduled_maintenances[0]) {
					var scheduledMaintenance = data.scheduled_maintenances[0];
					// there's an active scheduled maintenance!  invoke alert with info.
					if (scheduledMaintenance.incident_updates && scheduledMaintenance.incident_updates[0]) {
						// we have more information, invoke with more information
						var info = scheduledMaintenance.incident_updates[0].body;
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showScheduledMaintenance(Ljava/lang/String;)(info);
					} else {
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showScheduledMaintenance(Ljava/lang/String;)(null);
					}
				}
			}
		});
	}-*/;

}
