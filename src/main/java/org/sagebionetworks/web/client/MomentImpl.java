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
		try {
			return $wnd.moment(s).fromNow();
		} catch (err) {
			console.error(err);
			return s;
		}
	}-*/;

	private static native String _getCalendarTime(String s) /*-{
		try {
			return $wnd.moment(s).calendar();
		} catch (err) {
			console.error(err);
			return s;
		}
	}-*/;
}
