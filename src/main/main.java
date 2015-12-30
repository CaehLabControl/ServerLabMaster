package main;

import control.connection.socket.server;
import control.trayIcon;
import static control.trayIcon.trayIcon;
import interfaces.login;
import java.awt.TrayIcon;

/**
*  @web http://www.jc-mouse.net/
 * @author Mouse
 */
public class main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        server server = new server();
        server.start();
        new trayIcon(new login());
        trayIcon.displayMessage("Estado", "Servidor en linea", TrayIcon.MessageType.INFO);
    }
}
