import java.io.IOException;
import java.net.ServerSocket;


public class RemoteClientHandlerListenerThread extends Thread{
	
	private ClientHandler host;
	ServerSocket serverSocket = null;
	
	public RemoteClientHandlerListenerThread(ClientHandler ch, int port){
		this.host = ch;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		System.out.println("Listener thread online!");
		while(true){
			try {
				new RemoteClientHandlerThread(serverSocket.accept(), host).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
