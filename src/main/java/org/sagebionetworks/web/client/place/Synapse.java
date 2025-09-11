package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import java.util.LinkedList;
import org.sagebionetworks.web.client.StringUtils;

public class Synapse extends Place {

  public static final String DOT_REGEX = "\\.";
  public static final String DELIMITER = "/";
  public static final String SYNAPSE_ENTITY_PREFIX = "Synapse:";
  public static final String VERSION = "version";
  public static final String DRAFT = "draft";

  private String synapsePlaceToken;
  private String entityId, areaToken;
  private Long versionNumber;
  private Synapse.EntityArea area;
  private boolean isDraftRequested;

  public Synapse(String token) {
    // SWC-6646: strip any trailing ',' or '.'
    this.synapsePlaceToken = token.replaceAll("[,\\.]+$", "");
    area = null;
    areaToken = null;
    String[] tokensArray = synapsePlaceToken.split(DELIMITER);
    LinkedList<String> tokens = new LinkedList<String>();
    for (int i = 0; i < tokensArray.length; i++) {
      tokens.add(tokensArray[i]);
    }

    // first token should be the entity id
    entityId = tokens.poll();

    // look for dot version or draft syntax in this token
    String[] entityIdTokens = entityId.split(DOT_REGEX);
    if (entityIdTokens.length > 1) {
      entityId = entityIdTokens[0];
      if (DRAFT.equals(entityIdTokens[1])) {
        isDraftRequested = true;
      } else {
        try {
          versionNumber = Long.parseLong(entityIdTokens[1]);
        } catch (NumberFormatException e) {
          // invalid version, ignore
        }
      }
    }

    // set the next token
    String nextToken = tokens.poll();

    if (nextToken != null && VERSION.equals(nextToken.toLowerCase())) {
      nextToken = null;
      if (!tokens.isEmpty()) {
        try {
          versionNumber = Long.parseLong(tokens.removeFirst());
        } catch (NumberFormatException e) {
          // invalid version, ignore
        }
        nextToken = tokens.poll();
      }
    }

    if (nextToken != null) {
      try {
        area = EntityArea.valueOf(nextToken.toUpperCase());
      } catch (Exception e) {
        // invalid entity area, ignore
      }
    }

    // remaining tokens are recognized is the area token
    if (tokens.size() > 0) {
      areaToken = "";
    }
    while (tokens.size() > 0) {
      areaToken += tokens.poll();
      if (tokens.size() > 0) {
        areaToken += "/";
      }
    }
  }

  public static String getDelimiter(Synapse.EntityArea tab) {
    return "/" + tab.toString().toLowerCase() + "/";
  }

  /**
   * Constructor for creating draft Synapse places.
   *
   * @param isDraftRequested true to generate URLs like "syn123.draft/datasets/", false for normal URLs
   */
  public Synapse(
    String entityId,
    Long versionNumber,
    Synapse.EntityArea area,
    String areaToken,
    boolean isDraftRequested
  ) {
    this.entityId = entityId;
    this.versionNumber = versionNumber;
    this.area = area;
    this.areaToken = areaToken;
    this.isDraftRequested = isDraftRequested;
    calculateToken(entityId, versionNumber, area, areaToken, isDraftRequested);
  }

  /**
   * Constructor for creating non-draft Synapse places.
   */
  public Synapse(
    String entityId,
    Long versionNumber,
    Synapse.EntityArea area,
    String areaToken
  ) {
    this.entityId = entityId;
    this.versionNumber = versionNumber;
    this.area = area;
    this.areaToken = areaToken;
    calculateToken(entityId, versionNumber, area, areaToken, false);
  }

  /**
   * Convenience overload for calculateToken that defaults draft support to false.
   * Calls the main calculateToken method with isDraftRequested set to false (non-draft version).
   */
  private void calculateToken(
    String entityId,
    Long versionNumber,
    Synapse.EntityArea area,
    String areaToken
  ) {
    calculateToken(entityId, versionNumber, area, areaToken, false);
  }

  /**
   * Main token calculation method that generates the URL token string.
   * Handles version numbers (syn123.1) and draft tokens (syn123.draft) for datasets.
   */
  private void calculateToken(
    String entityId,
    Long versionNumber,
    Synapse.EntityArea area,
    String areaToken,
    boolean isDraftRequested
  ) {
    this.synapsePlaceToken = entityId;
    if (versionNumber != null) {
      this.synapsePlaceToken += "." + versionNumber;
    } else if (isDraftRequested) {
      this.synapsePlaceToken += "." + DRAFT;
    }
    if (area != null) {
      this.synapsePlaceToken += getDelimiter(area);
      if (areaToken != null) {
        this.synapsePlaceToken += areaToken;
      }
    }
  }

  public String toToken() {
    return synapsePlaceToken;
  }

  public String getEntityId() {
    return entityId;
  }

  public Long getVersionNumber() {
    return versionNumber;
  }

  public boolean isDraftRequested() {
    return isDraftRequested;
  }

  public Synapse.EntityArea getArea() {
    return area;
  }

  public String getAreaToken() {
    return areaToken;
  }

  public void setArea(Synapse.EntityArea area) {
    this.area = area;
    calculateToken(entityId, versionNumber, area, areaToken, isDraftRequested);
  }

  public void setAreaToken(String areaToken) {
    this.areaToken = areaToken;
    calculateToken(entityId, versionNumber, area, areaToken, isDraftRequested);
  }

  @Prefix("Synapse")
  public static class Tokenizer implements PlaceTokenizer<Synapse> {

    @Override
    public String getToken(Synapse place) {
      return place.toToken();
    }

    @Override
    public Synapse getPlace(String token) {
      return new Synapse(token);
    }
  }

  public static enum EntityArea {
    WIKI,
    FILES,
    DATASETS,
    TABLES,
    CHALLENGE,
    DISCUSSION,
    DOCKER,
  }

  public static enum ProfileArea {
    PROFILE,
    FAVORITES,
    PROJECTS,
    CHALLENGES,
    TEAMS,
    /* The Settings area now automatically redirects to OneSage */
    SETTINGS,
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result +
      ((synapsePlaceToken == null) ? 0 : synapsePlaceToken.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Synapse other = (Synapse) obj;
    if (synapsePlaceToken == null) {
      if (other.synapsePlaceToken != null) return false;
    } else if (!synapsePlaceToken.equals(other.synapsePlaceToken)) return false;
    return true;
  }

  /**
   * Given a string where the version number is delimited with a dot (.) convert to a valid token.
   *
   * @param dotNotation
   * @return
   */
  public static String getHrefForDotVersion(String dotNotation) {
    dotNotation = StringUtils.emptyAsNull(dotNotation);
    if (dotNotation == null) {
      return null;
    }
    return "/" + SYNAPSE_ENTITY_PREFIX + dotNotation.toLowerCase();
  }
}
