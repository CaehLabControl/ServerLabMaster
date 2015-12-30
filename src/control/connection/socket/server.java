package control.connection.socket;

import static control.trayIcon.trayIcon;
import interfaces.login;
import java.awt.TrayIcon;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class server extends WebSocketServer {

    /** The web socket port number */
    private static int PORT = 8887;

    static public Set<WebSocket> serverConnection;
    private Map<WebSocket, String> nickConnections;
    static public JSONArray objClientsConnected = new JSONArray();    
    int existConunt = 0;
    
    /**
     * Creates a new WebSocketServer with the wildcard IP accepting all connections.
     */
    public server() {
        super(new InetSocketAddress(PORT));
        serverConnection = new HashSet<>();
        nickConnections = new HashMap<>();
    }

    /** 
     * Method handler when a new connection has been opened. 
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("[{\"body\":[{\"message\":\"Hi, welcome\"}]}]");
        if(!serverConnection.contains(conn)){
            serverConnection.add(conn);
            nickConnections.put(conn, conn.getRemoteSocketAddress().getAddress().getHostAddress());  
            System.out.println("Show clients connected");
            System.out.println("   ->"+nickConnections.values());
//            System.out.println("New user with ip remote address "+conn.getRemoteSocketAddress().getAddress().getHostAddress()+" add to connection");
            trayIcon.displayMessage("Información", "Un nuevo cliente con dirección "+conn.getLocalSocketAddress().getAddress().getHostAddress()+" se incorporo a la sesión", TrayIcon.MessageType.INFO);
        }else{
            System.out.println("Good");            
        }
        
    }

    /** 
     * Method handler when a connection has been closed.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {   
        System.out.println("Closed connection of " + conn.getRemoteSocketAddress().getAddress().getHostAddress()+" error "+code);
        serverConnection.remove(conn);
        nickConnections.remove(conn);
        removeUser(conn);
    }

    /** 
     * Method handler when a message has been received from the client.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONObject jsonMenssage;
        if (nickConnections.containsKey(conn)) {
            try {
                jsonMenssage = new JSONObject(message);
                switch (jsonMenssage.getString("action")) {
                    case "message":
                        String messageToSend = jsonMenssage.getJSONArray("content").getJSONObject(0).get("body").toString();
                        String rolClient = jsonMenssage.getJSONArray("content").getJSONObject(0).get("rolClient").toString();
                        sendMessage(conn, conn, messageToSend, rolClient);
                        for (WebSocket sock : serverConnection) {
                            if(sock!=conn){
                                sendMessage(sock, conn, messageToSend, rolClient);
                            }                
                        }
                    break;
                    case "addClient":
                        JSONArray jsonContainer = new JSONArray();
                        JSONObject jsonPrincipal = new JSONObject();
                        String ipAddress = conn.getRemoteSocketAddress().getAddress().getHostAddress();
                        for (WebSocket sock : serverConnection) {
                            if(sock!=conn){
                                sendMessage(sock, conn, "New client add", "manager");
                            }                
                        }
                        try {     
                            JSONObject client = new JSONObject();
                            client.put("idConnection", conn);
                            client.put("ipClient", ipAddress);
                            client.put("rolClient", jsonMenssage.getString("rolClient"));
                            if(nickConnections.containsKey(conn)){
                                objClientsConnected.put(client); 
                                jsonPrincipal.put("CliensConnected", objClientsConnected);
                                jsonContainer.put(jsonPrincipal);
                                System.out.println("   ->"+jsonContainer);
                            }     
                        }catch(Exception e){
                            System.err.println(e);
                        }
                    break;
                    default:
                        System.err.println("Case not found");
                    break;
                }
            } catch (JSONException ex) {
                Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /** 
     * Method handler when an error has occured.
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        
        System.err.println("error");
        if(ex.toString().contains("BindException")){
            JOptionPane.showMessageDialog(null,"Otra instancia de la aplicación se está ejecutando...", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }else if(ex.toString().contains("Se ha forzado la interrupción")){            
            trayIcon.displayMessage("Alvertencia", "Se perdio la conexión con el cliente "+conn.getRemoteSocketAddress().getAddress().getHostAddress(), TrayIcon.MessageType.WARNING);
        }     
    }

    private void removeUser(WebSocket conn) {
        String messageToSend = "Client "+ conn.getRemoteSocketAddress().getAddress().getHostAddress() + " was removed";
        for (int i = 0; i < objClientsConnected.length(); i++) {
            try {
                if(objClientsConnected.getJSONObject(i).get("idConnection").equals(conn)){
                    objClientsConnected.remove(i);
                    System.out.println("Was deleted connection -> "+conn);
                }
            } catch (JSONException ex) {
                Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        for (WebSocket sock : serverConnection){
            sendMessage(sock, conn, messageToSend, "manager");
        }
    }
    
    public void sendMessage(WebSocket connDestination, WebSocket connOrigin, String message, String forRolClient){
        JSONArray jsonContainerMessage = new JSONArray();
        JSONObject jsonPrincipalMessage = new JSONObject();
        JSONArray jsonBody = new JSONArray();
        JSONObject jsonBodyContent = new JSONObject();
        try {
            jsonBodyContent.put("message", message);
            jsonBodyContent.put("hostNameDestination", connDestination.getRemoteSocketAddress().getAddress().getHostName());
            jsonBodyContent.put("idConnectionDestination", connDestination);
            jsonBodyContent.put("ipAddresDestination", connDestination.getRemoteSocketAddress().getAddress().getHostAddress());
            jsonBodyContent.put("ipAddresOrigin", connOrigin.getRemoteSocketAddress().getAddress().getHostAddress());
            jsonBodyContent.put("rolClient", forRolClient);
            jsonBody.put(jsonBodyContent);
            jsonPrincipalMessage.put("body", jsonBody);
            jsonContainerMessage.put(jsonPrincipalMessage);
            System.out.println(connDestination.getRemoteSocketAddress().getAddress().getHostAddress()+" envio ->"+jsonContainerMessage);
            connDestination.send(jsonContainerMessage.toString());
        } catch (JSONException ex) {
            Logger.getLogger(server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
