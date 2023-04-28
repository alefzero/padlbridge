package com.alefzero.padlbridge;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;

class AppTest {

	protected static final Logger logger = LogManager.getLogger();

	@Test
	void appHasAGreeting() {
		App classUnderTest = new App();
		assertNotNull(classUnderTest.getClass().getCanonicalName(), "not null");
	}

	@Test
	void loadLDAP() {
		try {
			InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=alefzero,dc=com");
			config.addAdditionalBindCredentials("cn=admin", "password1");
			config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("lsn", 10389));
			try (
					// Create the directory server instance, populate it with data from the
					// "test-data.ldif" file, and start listening for client connections.
					InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config)) {
				ds.importFromLDIF(true, "ldap-test-source-data.ldif");
				ds.startListening();

				// Get a client connection to the server and use it to perform various
				// operations.
				LDAPConnection conn = ds.getConnection();
				SearchResultEntry entry = conn.getEntry("dc=alefzero,dc=com");

				System.out.println("Entry data; {}" + entry);
				logger.info("Entry data; {}", entry);

				assertNotNull(entry);

				Thread.sleep(1);

				// Disconnect from the server and cause the server to shut down.
				conn.close();
				ds.shutDown(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (LDAPException e) {
			e.printStackTrace();
		}
	}
}
