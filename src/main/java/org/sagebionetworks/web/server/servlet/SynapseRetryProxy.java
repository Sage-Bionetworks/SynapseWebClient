package org.sagebionetworks.web.server.servlet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseTooManyRequestsException;
import org.sagebionetworks.util.RetryException;
import org.sagebionetworks.util.TimeUtils;

/**
 * A Utility for creating a proxy of the Synapse Java Client where certain calls will be retried.
 * 
 * @author Jay
 *
 */
public class SynapseRetryProxy {
	private static Logger log = LogManager.getLogger(SynapseRetryProxy.class);

	/** 429 Too Many Requests (RFC 6585) */
	public static final int SC_TOO_MANY_REQUESTS = 429;

	public static final int MAX_RETRY_COUNT = 10;

	/**
	 * Create a proxy of the Synapse Java that profiles all calls.
	 * 
	 * @param toProxy
	 * @return
	 */
	public static SynapseClient createProxy(SynapseClient toProxy) {
		InvocationHandler handler = new SynapseInvocationHandler(toProxy);
		return (SynapseClient) Proxy.newProxyInstance(SynapseClient.class.getClassLoader(), new Class[] {SynapseClient.class}, handler);
	}

	/**
	 * Handler that will retry the call if it encounters an error corresponding to the TOO MANY REQUESTS
	 * http status
	 */
	private static class SynapseInvocationHandler implements InvocationHandler {

		public SynapseInvocationHandler(SynapseClient wrapped) {
			super();
			this.wrapped = wrapped;
		}

		private SynapseClient wrapped;

		@Override
		public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
			try {
				return TimeUtils.waitForExponentialMaxRetry(MAX_RETRY_COUNT, 1000, new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							Object response = method.invoke(wrapped, args);
							return response;
						} catch (InvocationTargetException ex) {
							Throwable cause = ex.getCause();
							if (cause instanceof SynapseTooManyRequestsException) {
								// if 429, we can retry. send full exception trace to logger.debug to inform developer that we have
								// a problem.
								Calendar now = Calendar.getInstance();
								log.debug("THROTTLED! Attempted to call " + method.getName() + " at " + now.getTime(), cause);
								throw new RetryException(cause);
							}
							throw ex;
						}

					}
				});
			} catch (InvocationTargetException e) {
				log.error(e);
				// We must catch InvocationTargetException to avoid UndeclaredThrowableExceptions
				// see: http://amitstechblog.wordpress.com/2011/07/24/java-proxies-and-undeclaredthrowableexception/
				throw e.getCause();
			}
		}

	}
}
