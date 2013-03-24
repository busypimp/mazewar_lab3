import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	private Map nameToClientMap;
	
	public ClientHandler(String address, int port, String name){
		
		try {
			
			this.sock = new Socket(address, port);
			this.out = new ObjectOutputStream(sock.getOutputStream());
			this.in = new ObjectInputStream(sock.getInputStream());
			this.nameToClientMap = new HashMap();

			registerClient(name, port, address);
			
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
	
	public void registerClient(String name, int port, String address){
		// first thing  I am going to do is add myself
		
		this.nameToClientMap.put(name, new GUIClient(name));
		
		ClientPacket pack = new ClientPacket();
		pack.type = ClientPacket.CLIENT_REGISTER;
		pack.clientName = name;
		pack.connDetails = new ConnectionDetails(port, address);
		
		try {
			out.writeObject(pack);
			// since the listening thread is still not running 
			//we wait manually for the client to reply back
			ClientPacket packetFromServer = (ClientPacket) in.readObject();
			if(packetFromServer.type != ClientPacket.SERVER_ACKNOWLEDGE){
				System.err.println("There was a problem registering the client!!");
			}else{
				// Server has ack'ed that we are online, now request other players
				requestPlayers();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void requestPlayers() throws IOException, ClassNotFoundException {
		
		ClientPacket packetToServer = new ClientPacket();
		packetToServer.type = ClientPacket.CLIENT_REQUEST_PLAYERS;
		
		out.writeObject(packetToServer);
		// Again, since the listening part is still not online yet, we wait manually
		ClientPacket packetFromServer = new ClientPacket();
		packetFromServer = (ClientPacket) in.readObject();
		
		if(packetFromServer.type != ClientPacket.SERVER_RESPOND_PLAYERS){
			System.err.println("There was a problem getting list of existing players!");
		}else{ 
			System.out.println("Ok so the server returned a list of playaaaaas!!");//TODO put some real code here
			// WORK IN PROGRESS 
			ServerDataBean bean = packetFromServer.serverData;
			HashMap nameToConnMap = bean.nameToConnMap;
			Set names = nameToConnMap.keySet();
			for(int i = 0; i < names.size(); i++){
				System.out.println("Name <" + names.toArray()[i] + ">");
				//TODO Things to do at this point
				/*
				 * Start a thread with each new client
				 * first thing, ask each client for update on thier position and orientation
				 * once you get it back, add them to the list and to the GUI
				 * 
				 */
			}
		}
		
	}
}
