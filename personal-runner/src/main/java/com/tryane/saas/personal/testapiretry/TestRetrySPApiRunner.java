package com.tryane.saas.personal.testapiretry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tryane.saas.connector.o365.utils.exception.O365APIRuntimeException;
import com.tryane.saas.connector.o365.utils.exception.O365ConnectionException;
import com.tryane.saas.connector.o365.utils.exception.O365HttpErrorException;
import com.tryane.saas.personal.AbstractSpringRunner;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.config.PersonalDatabaseConfig;

public class TestRetrySPApiRunner extends AbstractSpringRunner {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(TestRetrySPApiRunner.class);

	private final static int	NB_MAX_ATTEMPT	= 5;

	private static final int	RETRY_DELAY		= 30;

	private Boolean				disableRetry	= false;

	@Override
	protected void testImplementation() {
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		for (int i = 0; i < 5; i++) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					try {
						testImplem();
					} catch (O365HttpErrorException | O365ConnectionException e) {
						LOGGER.error("", e);
					}
				}
			});
		}

		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public static void main(String[] args) {
		new TestRetrySPApiRunner().runTest("dev", PersonalAppConfig.class, PersonalDatabaseConfig.class);
	}

	private void testImplem() throws O365HttpErrorException, O365ConnectionException {
		int nbAttempt = 0;
		while (true) {
			// oui je sais un while true ca fait peur, mais c'est comme ca. Pas de probleme, on sort forcÃ©ment de la boucle a un moment !
			nbAttempt++;
			try {// call method
				Response response = new OutboundJaxrsResponse(Status.UNAUTHORIZED, new OutboundMessageContext()) {
					@Override
					public <T> T readEntity(final Class<T> entityType) throws ProcessingException, IllegalStateException {
						String toReturn = "429 TOO MANY REQUESTS";
						return (T) toReturn;
					}
				};
				throw new WebApplicationException("429 TOO MANY REQUESTS", response) {
				};
			} catch (Exception e) {
				// Error handling
				if (ProcessingException.class.isAssignableFrom(e.getClass())) {
					// connection exception, retry at least NB_ATTEMPT times
					if (nbAttempt >= NB_MAX_ATTEMPT) {
						throw new O365ConnectionException((ProcessingException) e, "url", nbAttempt);
					} else {
						LOGGER.debug("Connection Exception while calling {}, attempt #{}, retrying", "url", nbAttempt);
					}

				} else if (WebApplicationException.class.isAssignableFrom(e.getClass())) {
					// http error exception, retry only if we tried NB_ATTEMPT time or final http error code
					O365HttpErrorException o365HttpErrorException = new O365HttpErrorException((WebApplicationException) e, "url", nbAttempt);
					if (nbAttempt >= NB_MAX_ATTEMPT || isFinalHTTPError(o365HttpErrorException)) {
						throw o365HttpErrorException;
					} else {
						LOGGER.info("HTTP {} error  ({}) while calling {}, attempt #{}, retrying", ((WebApplicationException) e).getResponse().getStatus(), ((WebApplicationException) e).getResponse().readEntity(String.class), "url", nbAttempt);
						waitForRetry(((WebApplicationException) e).getResponse(), nbAttempt);
					}
				} else {
					throw new O365APIRuntimeException(e);
				}
			}
		}
	}

	public static void waitForRetry(Response response, int retryAttempt) {
		try {
			int status = response.getStatus();
			String message = response.readEntity(String.class);
			if (status == 429 || status == 503 || (status == 401 && O365HttpErrorException.TOO_MANY_REQUEST_MESSAGE.equals(message))) {
				// https://docs.microsoft.com/en-us/sharepoint/dev/general-development/how-to-avoid-getting-throttled-or-blocked-in-sharepoint-online
				int secondsToSleep = RETRY_DELAY * (int) Math.pow(2, retryAttempt - 1);
				LOGGER.info("waiting for {} seconds until retry", secondsToSleep);
				Thread.sleep(secondsToSleep * 1000);
			}

		} catch (Exception e) {
			// nothing to do
			LOGGER.debug("", e);
		}
	}

	/* return true if we don't need to retry to connect */
	public boolean isFinalHTTPError(O365HttpErrorException o365HttpErrorException) {
		if (disableRetry) {
			return true;
		}

		// pas de retry si dépassement limite
		if (o365HttpErrorException.isSPQueryThrottledException()) {
			return true;
		}

		// renvoie true si on ne re tente pas la connexion
		// implementation: on ne retente pas si c'est une erreur client (code entre 400 et 500), sauf 429 qui est le too many requests
		int status = o365HttpErrorException.getHttpCode();
		// 429 too many request
		if (status == 429 || (status == 401 && O365HttpErrorException.TOO_MANY_REQUEST_MESSAGE.equals(o365HttpErrorException.getHttpMessage()))) {
			// avec o365, les erreur 429 ont pour code 401, avec comme message 409 too many requests
			return false;
		}

		return (status != 429 && status >= 400 && status < 500);
	}
}
