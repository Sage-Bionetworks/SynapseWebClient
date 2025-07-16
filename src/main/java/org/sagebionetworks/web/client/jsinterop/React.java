package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.context.ContextUtils;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class React {

  public static native <
    T extends ReactComponentType<P>, P extends ReactComponentProps
  > ReactElement<T, P> createElement(ReactComponentType<P> componentType);

  public static native <
    T extends ReactComponentType<P>, P extends ReactComponentProps
  > ReactElement<T, P> createElement(
    ReactComponentType<P> componentType,
    P props,
    ReactElement<?, ?>... children
  );

  public static native <T> T createRef();

  /**
   * Wraps a component in SynapseContextProvider. Nearly all Synapse React Client components must be wrapped in this context, so this utility
   * simplifies creating the wrapper.
   *
   * @param <P>
   * @param componentType
   * @return
   */
  @JsOverlay
  public static <
    T extends ReactComponentType<P>, P extends ReactComponentProps
  > ReactElement<?, ?> createElementWithSynapseContext(T componentType) {
    return createElementWithSynapseContext(componentType, null);
  }

  /**
   * Wraps a component in SynapseContextProvider. Nearly all Synapse React Client components must be wrapped in this context, so this utility
   * simplifies creating the wrapper.
   *
   * @param <P>
   * @param componentType
   * @param props
   * @return
   */
  @JsOverlay
  public static <
    T extends ReactComponentType<P>, P extends ReactComponentProps
  > ReactElement<?, ?> createElementWithSynapseContext(
    T componentType,
    P props,
    ReactElement<?, ?>... children
  ) {
    ReactElement<T, P> componentElement = createElement(
      componentType,
      props,
      children
    );
    return createElement(
      ContextUtils.SynapseContextProviderFromStore,
      null,
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

  public static ReactComponentType<EmptyProps> Fragment;
}
