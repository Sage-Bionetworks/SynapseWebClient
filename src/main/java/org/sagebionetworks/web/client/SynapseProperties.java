package org.sagebionetworks.web.client;

import com.google.common.util.concurrent.FluentFuture;
import java.util.HashMap;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

public interface SynapseProperties {
  String getSynapseProperty(String key);

  PublicPrincipalIds getPublicPrincipalIds();

  FluentFuture<HashMap<String, String>> getInitSynapsePropertiesFuture();
}
