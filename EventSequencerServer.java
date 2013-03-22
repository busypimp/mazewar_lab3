import java.net.*;
import java.io.*;

public class EventSequencerServer {
	public static int seqID = 0; 
	
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;
//        ServerDataBean serverData = new ServerDataBean();
//        MazewarBroadcast bbc = new MazewarBroadcast(serverData);
        
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
        	new EventSequencerServerHandlerThread(serverSocket.accept(), incrementSeq());//, serverData).start();
        }

        serverSocket.close();
    }
    
    public synchronized static int incrementSeq(){
    	return seqID++;
    }
	
	
}