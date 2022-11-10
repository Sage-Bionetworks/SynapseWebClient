package org.sagebionetworks.web.shared;

import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * AccessTokenWrapper
 *
 * Holds an access token used for short-term authentication with Synapse
 *
 */
public class AccessTokenWrapper implements JSONEntity {

  // maintaining the "sessionToken" key to ease migration pain (production portals do not all need to update in order to use the init session servlet)
  private static final String _KEY_TOKEN = "sessionToken";
  private String token;

  public AccessTokenWrapper() {}

  public AccessTokenWrapper(JSONObjectAdapter adapter)
    throws JSONObjectAdapterException {
    super();
    if (adapter == null) {
      throw new IllegalArgumentException(
        ObjectSchema.OBJECT_ADAPTER_CANNOT_BE_NULL
      );
    }
    initializeFromJSONObject(adapter);
  }

  public String getToken() {
    return token;
  }

  public AccessTokenWrapper setToken(String token) {
    this.token = token;
    return this;
  }

  @Override
  public JSONObjectAdapter initializeFromJSONObject(JSONObjectAdapter adapter)
    throws JSONObjectAdapterException {
    if (adapter == null) {
      throw new IllegalArgumentException(
        ObjectSchema.OBJECT_ADAPTER_CANNOT_BE_NULL
      );
    }
    if (!adapter.isNull(_KEY_TOKEN)) {
      token = adapter.getString(_KEY_TOKEN);
    } else {
      token = null;
    }
    return adapter;
  }

  @Override
  public JSONObjectAdapter writeToJSONObject(JSONObjectAdapter adapter)
    throws JSONObjectAdapterException {
    if (adapter == null) {
      throw new IllegalArgumentException(
        ObjectSchema.OBJECT_ADAPTER_CANNOT_BE_NULL
      );
    }
    if (token != null) {
      adapter.put(_KEY_TOKEN, token);
    }
    return adapter;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = ((prime * result) + ((token == null) ? 0 : token.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    AccessTokenWrapper other = ((AccessTokenWrapper) obj);
    if (token == null) {
      if (other.token != null) {
        return false;
      }
    } else {
      if (!token.equals(other.token)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder result;
    result = new StringBuilder();
    result.append("");
    result.append("org.sagebionetworks.web.shared.AccessTokenWrapper");
    result.append(" [");
    result.append("sessionToken=");
    result.append(token);
    result.append(" ");
    result.append("]");
    return result.toString();
  }
}
