/**
 *
 */
package com.blizzardtec.fixserver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.field.MDReqID;
import quickfix.field.Symbol;
import quickfix.field.MDEntryType;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.SessionID;
import quickfix.Message;

/**
 * @author Barnaby Golden
 *
 */
@SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
public final class UpdateManager
            extends HashSet<DepthRequest> implements Runnable {

    /**
     *
     */
    private static final long serialVersionUID = -5585782800237016436L;
    /**
     * Sleep time between messages.
     */
    private static final int SLEEP_TIME = 1000;
    /**
     * Logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(UpdateManager.class);

    /**
     * Flag to indicate if the manager is runnable.
     */
    private transient boolean runnable = true;
    /**
     * Price feed.
     */
    private final transient PriceFeed priceFeed;

    /**
     * Constructor.
     */
    public UpdateManager() {

        super();
        priceFeed = new PriceFeed();
    }

    /**
     * Cancel a request for updates.
     * @param request cancel request
     */
    public void cancel(final DepthRequest request) {

        final Iterator<DepthRequest> iterator = iterator();

        while (iterator.hasNext()) {
            final DepthRequest dRequest = iterator.next();

            if ((dRequest.getRequestId().equals(request.getRequestId()))
                    && (dRequest.getSymbol().equals(request.getSymbol()))) {

                remove(dRequest);
            }
        }
    }

    /**
     * Send a full snapshot for price depth.
     * @param request market depth request
     */
    public void sendMarketDataSnapshot(final DepthRequest request) {

        final List<PriceDepth> depths =
           priceFeed.getDepths(request.getSymbol(), request.getType());

        final Message msg =
            buildSnapshotMessage(request.getRequestId(), depths);

        FixHelper.sendMessage(new SessionID(request.getSessionId()), msg);
    }

    /**
     * Build a market data snapshot message.
     * @param requestId request ID
     * @param depths list of price depth levels
     * @return complete market data snapshot message
     */
    private Message buildSnapshotMessage(
            final String requestId, final List<PriceDepth> depths) {

        final Message msg = new MarketDataSnapshotFullRefresh();

        final PriceDepth pDepth0 = depths.get(0);

        // 262* MD request ID
        msg.setField(new MDReqID(requestId));
        msg.setField(new Symbol(pDepth0.getSymbol()));

        // * MD entries, one for each price depth entry
        for (int i = 0; i < depths.size(); i++) {

            final PriceDepth pDepth = depths.get(i);

            final quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries
            entryGroup =
                new quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries();

            entryGroup.set(
                    new quickfix.field.MDEntryPositionNo(i + 1));
            entryGroup.set(
                    new quickfix.field.MDEntryType(pDepth.getType()));
            entryGroup.set(
                    new quickfix.field.MDEntryPx(pDepth.getMidPrice()));
            entryGroup.set(
                    new quickfix.field.MDEntrySize(pDepth.getOrderSize()));

            msg.addGroup(entryGroup);
        }


        return msg;
    }

    /**
     * Build a market data refresh message.
     * @param request request
     * @param priceDepth price depth information
     * @return complete market data snapshot message
     */
    private Message buildRefreshMessage(
            final DepthRequest request, final PriceDepth priceDepth) {

        final Message msg = new MarketDataIncrementalRefresh();

        // 262* MD request ID
        msg.setField(new MDReqID(request.getRequestId()));
        //msg.setField(new Symbol(symbol));

        // * MD entries, only one at the moment as sending a single instrument
        final quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries
            entryGroup =
                new quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries();

        entryGroup.set(
                new MDEntryType('0'));
        entryGroup.set(
                new quickfix.field.Symbol(request.getSymbol()));
        entryGroup.set(
                new quickfix.field.MDEntryType(request.getType()));
        entryGroup.set(
                new quickfix.field.MDEntryPx(priceDepth.getMidPrice()));
        entryGroup.set(
                new quickfix.field.MDEntrySize(priceDepth.getOrderSize()));
        entryGroup.set(
          new quickfix.field.MDUpdateAction(priceDepth.getUpdateAction()));
        entryGroup.set(
                new quickfix.field.MDEntryPositionNo(priceDepth.getLevel()));


        msg.addGroup(entryGroup);

        return msg;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        while (runnable) {

            try {
                Thread.sleep(SLEEP_TIME);
                final Iterator<DepthRequest> iterator = iterator();
                while (iterator.hasNext()) {

                    final DepthRequest request = iterator.next();

                    final PriceDepth pDepth =
                        priceFeed.getDepthUpdate(request.getSymbol(), '0');

                    final Message msg =
                        buildRefreshMessage(request, pDepth);

                    FixHelper.sendMessage(
                           new SessionID(request.getSessionId()), msg);
                }

            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    /**
     * Setting runnable to false halts the manager thread.
     * @param runnable the runnable to set
     */
    public void setRunnable(final boolean runnable) {
        this.runnable = runnable;
    }
}
