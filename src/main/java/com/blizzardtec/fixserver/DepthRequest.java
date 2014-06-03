/**
 *
 */
package com.blizzardtec.fixserver;

/**
 * @author Barnaby Golden
 *
 */
public final class DepthRequest {

    /**
     * ID for specific market depth request.
     */
    private String requestId;
    /**
     * ID of the session making the request.
     */
    private String sessionId;
    /**
     * Symbol for which the request is made.
     */
    private String symbol;
    /**
     * Market order type.
     * 0 = Bid
     * 1 = Offer
     */
    private char type;

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }
    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }
    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }
    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }
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
}
