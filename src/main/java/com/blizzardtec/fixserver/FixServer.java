/**
 * 
 */
package com.blizzardtec.fixserver;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.fix44.MessageFactory;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;

/**
 * @author Barnaby Golden
 *
 */
public final class FixServer {

    /**
     * Sleep time.
     */
    private static final int SLEEP_TIME = 4000;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FixServer.class);

    /**
     * Client configuration file.
     */
    private static final String CONFIG =
      "c:\\docs\\workspace\\fixserver\\src\\main\\resources\\fixconfig";

    /**
     * Acceptor.
     */
    private transient SocketAcceptor acceptor;

    /**
     * Session mappings.
     */
    private final transient Map<InetSocketAddress, List<TemplateMapping>>
        dynSessionMap =
                new HashMap<InetSocketAddress, List<TemplateMapping>>();

    /**
     * Run the FIX server.
     */
    public void run() {

        final ServerApplication application = new ServerApplication();

        SessionSettings settings;
        try {
            settings = new SessionSettings(new FileInputStream(CONFIG));

            final MessageStoreFactory storeFactory = new FileStoreFactory(
                    settings);

            final LogFactory logFactory = new ScreenLogFactory(true, true,
                    true, true);

            final MessageFactory messageFactory = new MessageFactory();
            acceptor = new SocketAcceptor(application,
                    storeFactory, settings, logFactory, messageFactory);

            configureDynamicSessions(settings, application, storeFactory,
                    logFactory, messageFactory);

            LOG.info("STARTING SERVER...");

            acceptor.start();

            // short sleep to allow initialisation to complete
            Thread.sleep(SLEEP_TIME);

            System.out.println("press <enter> to quit");

            System.in.read();

            application.halt();

            // another short sleep to allow shutdown of update thread
            Thread.sleep(SLEEP_TIME);

            acceptor.stop();

        } catch (ConfigError e) {
            LOG.error(e.getMessage());
        } catch (FieldConvertError e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } catch (InterruptedException ie) {
            LOG.error(ie.getMessage());
        }
    }

    /**
     * Configure dynamic sessions.
     * @param settings settings
     * @param application app
     * @param msgStoreFactory store
     * @param logFactory log factory
     * @param msgFactory message factory
     * @throws ConfigError thrown
     * @throws FieldConvertError thrown
     */
    private void configureDynamicSessions(
            final SessionSettings settings,
            final Application application,
            final MessageStoreFactory msgStoreFactory,
            final LogFactory logFactory,
            final MessageFactory msgFactory)
            throws ConfigError, FieldConvertError {

        final Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            final SessionID sessionID = sectionIterator.next();
            if (isSessionTemplate(settings, sessionID)) {
                final InetSocketAddress address =
                    getAcceptorSocketAddress(settings, sessionID);
                getMappings(address).add(
                        new TemplateMapping(sessionID, sessionID));
            }
        }

        for (Map.Entry<InetSocketAddress,
                List<TemplateMapping>> entry : dynSessionMap.entrySet()) {

            acceptor.setSessionProvider(
                    entry.getKey(),
                    new DynamicAcceptorSessionProvider(settings, entry
                            .getValue(), application, msgStoreFactory,
                            logFactory, msgFactory));
        }
    }

    /**
     * Get mappings.
     * @param address address to use
     * @return list of mappings
     */
    private List<TemplateMapping> getMappings(final InetSocketAddress address) {
        List<TemplateMapping> mappings = dynSessionMap.get(address);
        if (mappings == null) {
            mappings = new ArrayList<TemplateMapping>();
            dynSessionMap.put(address, mappings);
        }
        return mappings;
    }

    /**
     * Get socket address.
     * @param settings settings
     * @param sessionID id
     * @return socket address
     * @throws ConfigError thrown
     * @throws FieldConvertError thrown
     */
    private InetSocketAddress getAcceptorSocketAddress(
            final SessionSettings settings,
            final SessionID sessionID) throws ConfigError,
            FieldConvertError {

        final String acceptorHost = settings.getString(sessionID,
                                        SETTING_SOCKET_ACCEPT_ADDRESS);

        final int acceptorPort = (int) settings.getLong(sessionID,
                SETTING_SOCKET_ACCEPT_PORT);

        final InetSocketAddress address =
            new InetSocketAddress(acceptorHost, acceptorPort);

        return address;
    }

    /**
     * Check if it is a session template.
     * @param settings settings
     * @param sessionID id
     * @return true if session template
     * @throws ConfigError thrown
     * @throws FieldConvertError thrown
     */
    private boolean isSessionTemplate(
            final SessionSettings settings,
            final SessionID sessionID)
                throws ConfigError, FieldConvertError {

        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }
}
