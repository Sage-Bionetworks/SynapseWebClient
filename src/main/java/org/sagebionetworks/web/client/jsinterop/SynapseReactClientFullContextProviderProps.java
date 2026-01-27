package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapseReactClientFullContextProviderProps
  extends ReactComponentProps {

  public SynapseContextJsObject synapseContext;
  public QueryClient queryClient;
  public IsEditingStore isEditingStore;
  public ApplicationSessionContextJsObject applicationSessionContext;

  @JsOverlay
  public static SynapseReactClientFullContextProviderProps create(
    SynapseContextJsObject synapseContext,
    QueryClient queryClient,
    IsEditingStore isEditingStore,
    ApplicationSessionContextJsObject applicationSessionContext
  ) {
    SynapseReactClientFullContextProviderProps props =
      new SynapseReactClientFullContextProviderProps();
    props.synapseContext = synapseContext;
    props.queryClient = queryClient;
    props.isEditingStore = isEditingStore;
    props.applicationSessionContext = applicationSessionContext;
    return props;
  }
}
