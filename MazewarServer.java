import java.net.*;
import java.io.*;

public class MazewarServer {
	private static int clientID = 0;
	
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;
        ServerDataBean serverData = new ServerDataBean();
//        new MazewarBroadcast(serverData).start();
        
        try {
        	if(args.length == 1) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

        while (listening) {
        	new MazewarServerHandlerThread(serverSocket.accept(), getClientID(), serverData).start();
//        	new MazewarServerHandlerThread(serverSocket.accept(), getClientID()).start();
        }

        serverSocket.close();
    }
    
    public synchronized static int getClientID(){
    	return clientID++;
    }
}