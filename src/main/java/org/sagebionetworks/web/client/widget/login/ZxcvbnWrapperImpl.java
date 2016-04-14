package org.sagebionetworks.web.client.widget.login;

public class ZxcvbnWrapperImpl implements ZxcvbnWrapper {

	private int score;
	private String feedback;
	
	@Override
	public void scorePassword(String password) {
		_scorePassword(this, password);
	}

	@Override
	public String getFeedback() {
		return feedback;
	}

	@Override
	public int getScore() {
		return score;
	}

	/**
	 * 
	 * @param password
	 * @return
	 */
	private static native void _scorePassword(ZxcvbnWrapperImpl x, String password) /*-{
		// Write instance field on x
		var result = $wnd.zxcvbn(password);
		x.@org.sagebionetworks.web.client.widget.login.ZxcvbnWrapperImpl::score = result.score;
		if (result.score <= 2) {
			x.@org.sagebionetworks.web.client.widget.login.ZxcvbnWrapperImpl::feedback = result.feedback.warning;
		}
	}-*/;
}
