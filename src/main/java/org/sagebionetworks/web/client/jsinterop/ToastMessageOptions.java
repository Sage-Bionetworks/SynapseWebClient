package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.user.client.Window;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ToastMessageOptions extends ReactComponentProps {

    @JsOverlay
    public static final int DEFAULT_TOAST_TIMEOUT_MS = 1000 * 15;

    @JsFunction
    public interface Callback {
        void run();
    }

    @JsNullable
    public String title;
    @JsNullable
    public int autoCloseInMs;
    @JsNullable
    public String primaryButtonText;
    @JsNullable
    Callback onPrimaryButtonClick;
    @JsNullable
    String secondaryButtonText;
    @JsNullable
    String secondaryButtonHref;

    @JsOverlay
    public static ToastMessageOptions create(String title, Integer autoCloseInMs, String primaryButtonText, Callback onPrimaryButtonClick, String secondaryButtonText, String secondaryButtonHref) {
        if (autoCloseInMs == null) {
            autoCloseInMs = DEFAULT_TOAST_TIMEOUT_MS;
        }

        ToastMessageOptions options = new ToastMessageOptions();
        options.title = title;
        options.autoCloseInMs = autoCloseInMs;
        options.primaryButtonText = primaryButtonText;
        options.onPrimaryButtonClick = onPrimaryButtonClick;
        options.secondaryButtonText = secondaryButtonText;
        options.secondaryButtonHref = secondaryButtonHref;
        return options;
    }

    @JsOverlay
    public static ToastMessageOptions create(String title, Integer autoCloseInMs, String primaryButtonText, Callback onPrimaryButtonClick) {
        if (autoCloseInMs == null) {
            autoCloseInMs = DEFAULT_TOAST_TIMEOUT_MS;
        }

        ToastMessageOptions options = new ToastMessageOptions();
        options.title = title;
        options.autoCloseInMs = autoCloseInMs;
        options.primaryButtonText = primaryButtonText;
        options.onPrimaryButtonClick = onPrimaryButtonClick;
        return options;
    }

    public static class Builder {
        private String title;
        private Integer autoCloseInMs;
        private String primaryButtonText;
        private Callback onPrimaryButtonClick;
        private String secondaryButtonText;
        private String secondaryButtonHref;

        public Builder() {
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setAutoCloseInMs(Integer autoCloseInMs) {
            this.autoCloseInMs = autoCloseInMs;
            return this;
        }

        public Builder setPrimaryButton(String text, Callback onClick) {
            this.primaryButtonText = text;
            this.onPrimaryButtonClick = onClick;
            return this;
        }

        /**
         * Clicking the primary button opens the provided link in the current window.
         * @param text
         * @param href
         * @return
         */
        public Builder setPrimaryButton(String text, String href) {
            this.primaryButtonText = text;
            this.onPrimaryButtonClick = () -> Window.Location.assign(href);
            return this;
        }

        public Builder setSecondaryButton(String text, String href) {
            this.secondaryButtonText = text;
            this.secondaryButtonHref = href;
            return this;
        }

        public ToastMessageOptions build() {
            return ToastMessageOptions.create(title, autoCloseInMs, primaryButtonText, onPrimaryButtonClick, secondaryButtonText, secondaryButtonHref);
        }
    }


}
