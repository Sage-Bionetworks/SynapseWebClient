package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class React {

  public static native <
    P extends ReactComponentProps
  > ReactElement createElement(ReactComponentType<P> component, P props);

  public static native <
    P extends ReactComponentProps
  > ReactElement createElement(
    ReactComponentType<P> component,
    P props,
    ReactElement... children
  );

  public static native <T> T createRef();

  /**
   * Similar to {@link #createElementWithSynapseContext} but only includes the theme. Any components rendered will NOT get the full Synapse context, including
   * access token + auth state, experimental mode status, and time display settings.
   */
  @JsOverlay
  public static <
    P extends ReactComponentProps
  > ReactElement createElementWithThemeContext(
    ReactComponentType<P> component,
    P props
  ) {
    SynapseReactClientFullContextProviderProps emptyContext =
      SynapseReactClientFullContextProviderProps.create(
        SynapseContextJsObject.create(null, false, false),
        null
      );
    return createElementWithSynapseContext(component, props, emptyContext);
  }

  /**
   * Wraps a component in SynapseContextProvider. Nearly all Synapse React Client components must be wrapped in this context, so this utility
   * simplifies creating the wrapper.
   *
   * For setting props, use {@link SynapseReactClientFullContextPropsProvider}
   * @param component
   * @param props
   * @param wrapperProps
   * @param <P>
   * @return
   */
  @JsOverlay
  public static <
    P extends ReactComponentProps
  > ReactElement createElementWithSynapseContext(
    ReactComponentType<P> component,
    P props,
    SynapseReactClientFullContextProviderProps wrapperProps
  ) {
    ReactElement componentElement = createElement(component, props);
    return createElement(
      SRC.SynapseContext.FullContextProvider,
      wrapperProps,
      componentElement
    );
  }

  public static native ReactElement cloneElement(ReactElement element);

  public static native ReactElement cloneElement(
    ReactElement element,
    ReactComponentProps props
  );

  public static native ReactElement cloneElement(
    ReactElement element,
    ReactComponentProps props,
    ReactElement... children
  );
}
