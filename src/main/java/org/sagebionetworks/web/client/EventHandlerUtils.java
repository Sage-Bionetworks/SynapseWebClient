package org.sagebionetworks.web.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;

public class EventHandlerUtils {

	/**
	 * Add a native event listener to the given element (listening to the given eventType). Note, on
	 * detach (or reconfigure) you should call the returned HandlerRegistration.removeHandler().
	 * 
	 * @param eventType Identifier of the item which will send a broadcast back
	 * @param el element which we are adding the listener to
	 * @param c invoked when the event type is caught by the element. Returns the event object
	 * @return
	 */
	public static HandlerRegistration addEventListener(final String eventType, final Element el, org.sagebionetworks.web.client.utils.JavaScriptCallback c) {
		final JavaScriptObject fn = _wrapCallback(c);
		_addEventListener(eventType, el, fn);
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				_removeEventListener(eventType, el, fn);
			}
		};
	}

	public final static native Element getWnd() /*-{
		return $wnd;
	}-*/;

	/**
	 * Start listening to the given event type on the given element.
	 * 
	 * @param eventType
	 * @param el
	 * @return The js function
	 */
	public final static native void _addEventListener(String eventType, Element el, JavaScriptObject fn) /*-{
		el.addEventListener(eventType, fn, false);
	}-*/;

	/**
	 * Stop listening to the given event type on the given element.
	 * 
	 * @param eventType
	 * @param el
	 * @return The js function
	 */
	public final static native void _removeEventListener(String eventType, Element el, JavaScriptObject fn) /*-{
		el.removeEventListener(eventType, fn, false);
	}-*/;

	/**
	 * Start listening to the given event type on the given element.
	 * 
	 * @param eventType
	 * @param el
	 * @return The js function that wraps the callback
	 */
	public final static native JavaScriptObject _wrapCallback(final org.sagebionetworks.web.client.utils.JavaScriptCallback c) /*-{
		return function(event) {
			c.@org.sagebionetworks.web.client.utils.JavaScriptCallback::invoke(Lcom/google/gwt/core/client/JavaScriptObject;)(event);
		};
	}-*/;
}

