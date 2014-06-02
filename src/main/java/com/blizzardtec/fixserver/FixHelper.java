/**
 * 
 */
package com.blizzardtec.fixserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.DataDictionaryProvider;
import quickfix.FixVersions;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.ApplVerID;

/**
 * @author Barnaby Golden
 *
 */
public final class FixHelper {

    /**
     * Logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(FixHelper.class);

    /**
     * Private constructor for utility method.
     */
    private FixHelper() {
        // private constructor
    }

    /**
     * Send a message.
     * @param sessionID id
     * @param message message to send
     */
    public static void sendMessage(
            final SessionID sessionID, final Message message) {
        try {
            final Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }
            
            final DataDictionaryProvider dictProvider =
                                    session.getDataDictionaryProvider();

            if (dictProvider != null) {
                try {
                    dictProvider.getApplicationDataDictionary(
                            getApplVerID(session)).validate(message, true);
                } catch (Exception e) {
                    LOG.error("Outgoing message failed validation: "
                                                        + e.getMessage());
                    return;
                }
            }
            
            session.send(message);
        } catch (SessionNotFound e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Get app version id.
     * @param session session
     * @return id
     */
    private static ApplVerID getApplVerID(final Session session) {

        ApplVerID verId;

        final String beginString = session.getSessionID().getBeginString();

        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            verId = new ApplVerID(ApplVerID.FIX50);
        } else {
            verId = MessageUtils.toApplVerID(beginString);
        }

        return verId;
    }


}
