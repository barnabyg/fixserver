/**
 * 
 */
package com.blizzardtec.fixserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Barnaby Golden
 *
 */
public final class PriceFeed {

    /**
     * 
     */
    private static final double DELETE_FACTOR = 0.05;
    /**
     * 
     */
    private static final char NEW = '0';
    /**
     * 
     */
    private static final double UPDATE_FACTOR = 0.3;
    /**
     * 
     */
    private static final char DELETE = '2';
    /**
     * 
     */
    private static final char CHANGE = '1';
    /**
     * Depth count.
     */
    private static final int DEPTH_COUNT = 3;
    /**
     * Zero offset.
     */
    private static final double ZERO_OFFSET = 1;
    /**
     * Operator to tidy up order size.
     */
    private static final int OPERATOR = 100;
    /**
     * Size multiplier to scale up size.
     */
    private static final int SIZE_MULT = 1000;
    /**
     * Multiplier for random order size.
     */
    private static final double MULTIPLIER = 10000.0;

    /**
     * Get the next price.
     * @param symbol the instrument to get a price for
     * @param type the type (e.g. bid/offer/etc.)
     * @return price depth
     */
    public List<PriceDepth> getDepths(final String symbol, final char type) {

        final List<PriceDepth> depths = new ArrayList<PriceDepth>();

        double price = 1.0 + Math.random();
        // tidy up price length
        final int pInt = (int) (price * MULTIPLIER); // scale it
        price = ((double) pInt / MULTIPLIER); // cut off the end part

        for (int i = 0; i < DEPTH_COUNT; i++) {

            int size = (int) ((Math.random() * OPERATOR) + ZERO_OFFSET);
            size = size * SIZE_MULT;

            final PriceDepth priceDepth = new PriceDepth();

            priceDepth.setSymbol(symbol);
            priceDepth.setType(type);
            priceDepth.setMidPrice(price - (i * (1.0 / SIZE_MULT)));
            priceDepth.setOrderSize(size);
            priceDepth.setLevel(i + 1);

            depths.add(priceDepth);
        }

        return depths;
    }

    /**
     * Get a single random price depth update.
     * @param symbol instrument symbol
     * @param type order type (BID/OFFER)
     * @return price depth
     */
    public PriceDepth getDepthUpdate(final String symbol, final char type) {

        double price = 1.0 + Math.random();
        // tidy up price length
        final int pInt = (int) (price * MULTIPLIER); // scale it
        price = ((double) pInt / MULTIPLIER); // cut off the end part

        int size = (int) ((Math.random() * OPERATOR) + ZERO_OFFSET);
        size = size * SIZE_MULT;

        final PriceDepth priceDepth = new PriceDepth();

        final int position =
            1 + (int) (Math.random() * ((DEPTH_COUNT) - 1) + 1);

        final double updateFactor = Math.random();

        if (updateFactor > UPDATE_FACTOR) {
            priceDepth.setUpdateAction(CHANGE);            
        } else if (updateFactor > DELETE_FACTOR
                        && updateFactor <= UPDATE_FACTOR) {
            priceDepth.setUpdateAction(DELETE);
        } else {
            priceDepth.setUpdateAction(NEW);
        }

        priceDepth.setSymbol(symbol);
        priceDepth.setType(type);
        priceDepth.setMidPrice(price);
        priceDepth.setOrderSize(size);
        priceDepth.setLevel(position);

        return priceDepth;
    }
}
