package ru.calypso.ogar.server.util.listeners;

import java.io.IOException;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import ru.calypso.ogar.server.OgarServer;

public class ConsoleListener implements Runnable {

    private final OgarServer server;

    public ConsoleListener(OgarServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            ConsoleReader console = new ConsoleReader();
            console.setPrompt("> ");
            String line = null;
            while ((line = console.readLine()) != null) {
                server.handleCommand(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                TerminalFactory.get().restore();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
