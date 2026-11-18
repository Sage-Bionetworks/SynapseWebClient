package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URL;
import java.util.Set;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.LogServlet;

/**
 * Guards the link between LogServlet.CLIENT_LOG_NAME and log4j2.xml: if either side is renamed, client logs
 * would silently stop reaching portal.log (and CloudWatch).
 */
public class Log4jConfigTest {

  @Test
  public void clientLogRoutesToPortalThroughAsyncAppender() throws Exception {
    URL url = LogServlet.class.getResource("/log4j2.xml");
    ConfigurationSource source = new ConfigurationSource(url.openStream(), url);
    LoggerContext ctx = new LoggerContext("log4j2-xml-test");
    try {
      Configuration config = new XmlConfiguration(ctx, source);
      config.initialize();
      LoggerConfig lc = config.getLoggerConfig(LogServlet.CLIENT_LOG_NAME);

      assertEquals(LogServlet.CLIENT_LOG_NAME, lc.getName()); // exact entry, not the parent
      assertFalse(lc.isAdditive());
      assertEquals(Set.of("ClientLogAsync"), lc.getAppenders().keySet());

      // ClientLogAsync writes to portal
      AsyncAppender async = config.getAppender("ClientLogAsync");
      assertArrayEquals(
        new String[] { "portal" },
        async.getAppenderRefStrings()
      );
    } finally {
      ctx.stop();
    }
  }
}
