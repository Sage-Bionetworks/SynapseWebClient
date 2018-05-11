package org.sagebionetworks.web.client;

public class MomentImpl implements Moment {

	@Override
	public String getRelativeTime(String s) {
		return _getRelativeTime(s);
	}

	@Override
	public String getCalendarTime(String s) {
		return _getCalendarTime(s);
	}

	private static native String _getRelativeTime(String s) /*-{
		return $wnd.moment(s).fromNow();
	}-*/;
	private static native String _getCalendarTime(String s) /*-{
		return $wnd.moment(s).calendar();
	}-*/;
}
