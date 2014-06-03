/**
 *
 */
package com.blizzardtec.fixserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.MDEntryType;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.MDReqID;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataRequest.NoMDEntryTypes;
import quickfix.fix44.MarketDataRequest.NoRelatedSym;
import quickfix.fix44.MessageCracker;

/**
 * @author Barnaby Golden
 *
 */
public final class ServerApplication
        extends MessageCracker implements Application {

    /**
     * Logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(ServerApplication.class);

    /**
     * SessionID.
     */
    private transient SessionID sessionId;
    /**
     * Update manager to control any market data update requests.
     */
    private final transient UpdateManager manager;

    /**
     * Constructor.
     */
    public ServerApplication() {
        super();
        manager = new UpdateManager();
        final Thread thread = new Thread(manager);
        thread.start();
    }

    /**
     * Handle a market data request.
     * @param message request
     * @param sessionId id
     */
    public void onMessage(final MarketDataRequest message,
                          final SessionID sessionId) {

        LOG.info("Received MarketDataRequest");

        // first we unpack the request
        final SubscriptionRequestType reqType = new SubscriptionRequestType();
        final MDReqID mdReqId = new MDReqID();

        try {
            message.get(reqType);
            message.get(mdReqId);

            final int entryCount =
                message.get(new quickfix.field.NoMDEntryTypes()).getValue();

            for (int i = 1; i < (entryCount + 1); i++) {

                final NoMDEntryTypes entries = new NoMDEntryTypes();
                message.getGroup(i, entries);

                final NoRelatedSym symbols = new NoRelatedSym();
                message.getGroup(i, symbols);

                final Symbol symbol = symbols.getSymbol();
                final MDEntryType entryType = new MDEntryType();
                entries.get(entryType);
                final char type = (char) entryType.getObject();

                String typeStr = "Bid";

                if (type == OrderType.OFFER) {
                    typeStr = "Offer";
                }

                final DepthRequest request = new DepthRequest();
                request.setRequestId((String) mdReqId.getObject());
                request.setSessionId(sessionId.toString());
                request.setSymbol((String) symbol.getObject());
                request.setType(type);

                // check to see if it is a snapshot request
                // or if it requires updates or if it is a cancel request
                final char mode = reqType.getValue();
                if (mode == SubscriptionRequestType.SNAPSHOT) {
                    LOG.info("Sending "
                            + typeStr
                            + " MarketDataSnapshotFullRefresh for "
                            + symbol.getObject());
                    manager.sendMarketDataSnapshot(request);
                } else if (mode
                        == SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES) {
                    LOG.info("Sending "
                          + typeStr
                          + " MarketDataSnapshotFullRefresh for "
                          + symbol.getObject() + " and adding to update list");
                    manager.sendMarketDataSnapshot(request);
                    manager.add(request);
                } else if (mode
     == SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST) {
                    LOG.info("Cancelling "
                            + typeStr
                            + " updates for " + symbol.getObject());
                    manager.cancel(request);
                }
            }

        } catch (FieldNotFound e) {
            LOG.error(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see
     * quickfix.Application#fromAdmin(quickfix.Message, quickfix.SessionID)
     *
     */
    @Override
    public void fromAdmin(final Message arg0, final SessionID arg1)
         throws FieldNotFound,
                IncorrectDataFormat,
                IncorrectTagValue,
                RejectLogon {

        // TODO empty
    }

    /* (non-Javadoc)
     * @see quickfix.Application#fromApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void fromApp(final Message message, final SessionID sessionId)
        throws FieldNotFound, IncorrectDataFormat,
        IncorrectTagValue, UnsupportedMessageType {

        crack(message, sessionId);
    }

    /* (non-Javadoc)
     * @see quickfix.Application#onCreate(quickfix.SessionID)
     */
    @Override
    public void onCreate(final SessionID arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see quickfix.Application#onLogon(quickfix.SessionID)
     */
    @Override
    public void onLogon(final SessionID arg0) {

        this.sessionId = arg0;
    }

    /* (non-Javadoc)
     * @see quickfix.Application#onLogout(quickfix.SessionID)
     */
    @Override
    public void onLogout(final SessionID arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see quickfix.Application#toAdmin(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toAdmin(final Message arg0, final SessionID arg1) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see quickfix.Application#toApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toApp(final Message arg0, final SessionID arg1)
        throws DoNotSend {

        // TODO blah
    }

    /**
     * @return the sessionId
     */
    public SessionID getSessionId() {
        return sessionId;
    }

    /**
     * Halt the server.
     */
    public void halt() {
        manager.setRunnable(false);
    }
}
