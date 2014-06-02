/**
 * 
 */
package com.blizzardtec.fixserver;

/**
 * @author Barnaby Golden
 *
 */
public final class PriceDepth {

    /**
     * Instrument symbol.
     */
    private String symbol;
    /**
     * Mid price.
     */
    private double midPrice;
    /**
     * Price type.
     */
    private char type;
    /**
     * Size of the order.
     */
    private int orderSize;
    /**
     * Entry position.
     */
    private int level;
    /**
     * Update action.
     */
    private char updateAction;

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }
    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }
    /**
     * @return the midPrice
     */
    public double getMidPrice() {
        return midPrice;
    }
    /**
     * @param midPrice the midPrice to set
     */
    public void setMidPrice(final double midPrice) {
        this.midPrice = midPrice;
    }
    /**
     * @return the type
     */
    public char getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final char type) {
        this.type = type;
    }
    /**
     * @return the orderSize
     */
    public int getOrderSize() {
        return orderSize;
    }
    /**
     * @param orderSize the orderSize to set
     */
    public void setOrderSize(final int orderSize) {
        this.orderSize = orderSize;
    }
    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }
    /**
     * @param level the level to set
     */
    public void setLevel(final int level) {
        this.level = level;
    }
    /**
     * @return the updateAction
     */
    public char getUpdateAction() {
        return updateAction;
    }
    /**
     * @param updateAction the updateAction to set
     */
    public void setUpdateAction(final char updateAction) {
        this.updateAction = updateAction;
    }
}
