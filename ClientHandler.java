import java.awt.event.KeyEvent;
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
	private String name;
	private int mySeqNum;
	private Maze maze;

	private Socket seqSock;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ClientPacket packetFromClient;
	private Map nameToClientMap;
	
	private Hashtable<Integer, ClientPacket> seqEventQueue;
	public ClientHandler(String address, int port, String name){
		
		try {
			
			this.sock = new Socket(address, port);
			this.out = new ObjectOutputStream(sock.getOutputStream());
			this.in = new ObjectInputStream(sock.getInputStream());
			this.nameToClientMap = new HashMap();

			registerClient(name, port, address);
			this.name = name;
			this.mySeqNum = 1;
			Hashtable<Integer, ClientPacket> seqEventQueue = new Hashtable<Integer, ClientPacket>() ;
			
			//This is saying the event sequencer should be run at the same place as the naming service server and port num is below!
			int portSeq = port+2222;
			this.seqout = new ObjectOutputStream(seqSock.getOutputStream());
			this.seqin = new ObjectInputStream(seqSock.getInputStream());
			
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
		try{
			while((packetFromClient = (ClientPacket) in.readObject()) != null){
//			while(true){
			/*
				 * do something don't just stand there
				 */
				switch (packetFromClient.type) {
				case ClientPacket.SERVER_ACKNOWLEDGE:
					System.out.println("ACK'ed");
					break;
				case ClientPacket.SERVER_RESPOND_PLAYERS:
					//processPlayerList();
					break;
				case ClientPacket.SERVER_EVENT_BROADCAST:
					processSeq();
					break;
				case ClientPacket.SERVER_RESPOND_SEQ:
						broadcastClients();
					break;
				case ClientPacket.GENERAL_NULL:
					break;
				case ClientPacket.GENERAL_BYE:
					System.out.println("Server Bye Functionality not yet implemented!!");
					break;
				default:
					System.err.println("ERROR: Unknown packet!!");
					System.exit(-1);
				}
			}
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void broadcastClients() {
		//Need to know how the client names and such are kept!
		
		
	}
	
	private void processSeq() {
		if(packetFromClient.sequence == (this.mySeqNum+1)) {
			processEvent(packetFromClient);
			this.mySeqNum++;
			while(seqEventQueue.get(this.mySeqNum) != null){
				processEvent(seqEventQueue.get(this.mySeqNum));
				seqEventQueue.remove(this.mySeqNum);
				this.mySeqNum++;
			}
		} else {
			//This is when the seq number we got was too big. so put in the queue!
			seqEventQueue.put(packetFromClient.sequence, packetFromClient);
		}
	}
	
	private void processEvent(ClientPacket fromClient) {
		int event =  fromClient.event;
		String name =  fromClient.clientName;
		Client client = null;
//		if(nameToClientMap.containsKey(name))
//			client = (Client) nameToClientMap.get(name);
		
		//Need to know if this is in order or not.

			if(event == ClientEvent.FIRE){
				assert(client != null);
				client.fire();
			}else if(event == ClientEvent.JOINED){
				System.out.println(name + " joins the game");
	//			addNewClient(packetFromClient.clientName, packetFromClient.info);
			}else if(event == ClientEvent.MOVE_BACKWARD){
				assert(client != null);
				client.backup();
			}else if(event == ClientEvent.MOVE_FORWARD){
				assert(client != null);
				client.forward();
			}else if(event == ClientEvent.QUIT && name.equals(this.name)){
				Mazewar.quit();
			}else if(event == ClientEvent.TURN_LEFT){
				assert(client != null);
				client.turnLeft();
			}else if(event == ClientEvent.TURN_RIGHT){
				assert(client != null);
				client.turnRight();
			}

//		if(client != null)
//			updateLocation(client);
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
		
	public void registerClient(String name){
		GUIClient client = new GUIClient(name);
	}
	
	public void keyPressedInGUI(KeyEvent e){
		//The server its sending to is the EventSequencerServerHandler
		ClientPacket packetToServer = new ClientPacket();
		packetToServer.type = ClientPacket.CLIENT_EVENT_SEQ;
		packetToServer.clientName = this.name;

		// If the user pressed Q, invoke the cleanup code and quit.
		if ((e.getKeyChar() == 'q') || (e.getKeyChar() == 'Q')) {
			packetToServer.event = ClientEvent.QUIT;
			// Up-arrow moves forward.
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			packetToServer.event = ClientEvent.MOVE_FORWARD;
			// Down-arrow moves backward.
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			packetToServer.event = ClientEvent.MOVE_BACKWARD;
			// Left-arrow turns left.
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			packetToServer.event = ClientEvent.TURN_LEFT;
			// Right-arrow turns right.
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			packetToServer.event = ClientEvent.TURN_RIGHT;
			// Spacebar fires.
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			packetToServer.event = ClientEvent.FIRE;
		}
		
		sendToServer(packetToServer);
		
	}
	
	public synchronized void sendToServer(ClientPacket packetToServer){
		
		assert(out != null);
		try {
			seqout.writeObject(packetToServer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
