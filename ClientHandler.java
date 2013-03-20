import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Handles client
 * @author tyagirac
 *
 */
public class ClientHandler extends Thread{
	
	private Socket sock;
	private Maze maze;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ClientPacket packetFromClient;
	
	public ClientHandler(String address, int port, String name){
		
		try {
			
			this.sock = new Socket(address, port);
			this.out = new ObjectOutputStream(sock.getOutputStream());
			this.in = new ObjectInputStream(sock.getInputStream());

			registerClient(name);
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void addMaze(Maze mazeInput){
		this.maze = mazeInput;
	}
	
	public void run(){
//		try{
//			while((packetFromClient = (ClientPacket) in.readObject()) != null){
			while(true){
			/*
				 * do something don't just stand there
				 */
			}
//		}catch(ClassNotFoundException e){
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public void registerClient(String name){
//		GUIClient client = new GUIClient(name);
		/*
		 * put code here to register the client
		 */
//		return client;
	}
}
