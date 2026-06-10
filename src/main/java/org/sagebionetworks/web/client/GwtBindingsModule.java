package org.sagebionetworks.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.gwt.client.schema.adapter.JSONArrayGwt;
import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;

/**
 * Dagger module for the handful of injected types that have no @Inject
 * constructor and are not created locally by a single owner: framework
 * classes, dependency classes, and GWT generator interfaces (ClientBundles,
 * the place-history mapper).
 */
@Module
public abstract class GwtBindingsModule {

  @Provides
  @Singleton
  static SimpleEventBus provideSimpleEventBus() {
    return new SimpleEventBus();
  }

  @Provides
  @Singleton
  static IconsImageBundle provideIconsImageBundle() {
    return GWT.create(IconsImageBundle.class);
  }

  @Provides
  @Singleton
  static SageImageBundle provideSageImageBundle() {
    return GWT.create(SageImageBundle.class);
  }

  @Provides
  static JSONObjectGwt provideJSONObjectGwt() {
    return new JSONObjectGwt();
  }

  @Provides
  static JSONArrayGwt provideJSONArrayGwt() {
    return new JSONArrayGwt();
  }

  @Provides
  static GwtAdapterFactory provideGwtAdapterFactory() {
    return new GwtAdapterFactory();
  }

  @Provides
  static BiodallianceSource provideBiodallianceSource() {
    return new BiodallianceSource();
  }

  @Provides
  @Singleton
  static AppPlaceHistoryMapper provideAppPlaceHistoryMapper() {
    return GWT.create(AppPlaceHistoryMapper.class);
  }
}
