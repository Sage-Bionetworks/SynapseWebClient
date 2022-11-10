package org.sagebionetworks.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ServiceEntryPointUtils {

  public static void fixServiceEntryPoint(Object serviceDefTargetObject) {
    if (serviceDefTargetObject instanceof ServiceDefTarget) {
      ServiceDefTarget serviceDefTarget = (ServiceDefTarget) serviceDefTargetObject;
      String oldUrl = serviceDefTarget.getServiceEntryPoint();
      if (oldUrl.startsWith(GWT.getModuleBaseURL())) {
        String serviceEntryPoint =
          GWTWrapperImpl.getRealGWTModuleBaseURL() +
          oldUrl.substring(GWT.getModuleBaseURL().length());
        serviceDefTarget.setServiceEntryPoint(serviceEntryPoint);
      }
    }
  }
}
