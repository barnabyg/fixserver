package com.blizzardtec.fixserver;

/**
 * Fix server main class.
 *
 */
public final class App {

    /**
     * Private constructor.
     */
    private App() {
        // private con
    }

    /**
     * Main app start.
     *
     * @param args
     *            command line args
     */
    public static void main(final String[] args) {

        final FixServer fixServer = new FixServer();
        fixServer.run();
    }
}
