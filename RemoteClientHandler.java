import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class RemoteClientHandler {
	
	private ClientHandler host;
	
	public RemoteClientHandler(int port, ClientHandler ch){
		System.out.println("Starting RemoteClientHandler");
		this.host = ch;
		new RemoteClientHandlerListenerThread(host, port).start(); // for incoming requests
		System.out.println("Remote Client Handler Online!");
	}
	
	public void connectToRemoteClient(int port, String hostname) throws UnknownHostException, IOException{
		System.out.println("remote client handler thread starting for port <" + port + "> and host <"+ hostname+ ">");
		new RemoteClientHandlerThread(new Socket(hostname, port), host, false).start();
		System.out.println("Thread started!");
	}
}
