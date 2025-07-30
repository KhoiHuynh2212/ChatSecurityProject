package sendfile.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Enhanced server thread that works with your existing MainForm
 * Simply replace your existing ServerThread.java with this file
 */
public class ServerThread implements Runnable {

    ServerSocket server;
    MainForm main;  // Uses your existing MainForm
    boolean keepGoing = true;

    public ServerThread(int port, MainForm main) {
        this.main = main;
        main.appendMessage("[Secure Server]: Initializing on port " + port);

        try {
            server = new ServerSocket(port);
            main.appendMessage("[Secure Server]: Ready for encrypted connections!");
            main.appendMessage("[Security]: Supporting both legacy and encrypted clients");
        }
        catch (IOException e) {
            main.appendMessage("[IOException]: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (keepGoing) {
                Socket clientSocket = server.accept();

                // Log new connection
                String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                main.appendMessage("[New Connection]: " + clientInfo);

                /** Create secure socket thread for each client **/
                new Thread(new SocketThread(clientSocket, main)).start();
            }
        } catch (IOException e) {
            if (keepGoing) {
                main.appendMessage("[ServerThread IOException]: " + e.getMessage());
            } else {
                main.appendMessage("[Server]: Shutdown complete");
            }
        }
    }

    /**
     * Stop the server gracefully
     */
    public void stop() {
        try {
            keepGoing = false;
            if (server != null && !server.isClosed()) {
                server.close();
            }
            main.appendMessage("[Server]: Stopping secure server...");
            System.out.println("Secure server stopped!");
            System.exit(0);

        } catch (IOException e) {
            main.appendMessage("[Stop Error]: " + e.getMessage());
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
}