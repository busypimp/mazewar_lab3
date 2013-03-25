import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles client
 * @author tyagirac
 *
 */
public class ClientHandler extends Thread{
	
	private Socket sock;
	private String name;
	private Client hostClient;
	private int mySeqNum;
	private Maze maze;

	private Socket seqSock;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ClientPacket packetFromClient;
	private Map nameToClientMap;
	private Map nameToHandlerMap;
	
	private RemoteClientHandler rch;
	
	private HashMap seqEventQueue;
	
	public ClientHandler(String address, int port, String name, int portMe){
		
		try {
			this.rch = new RemoteClientHandler(portMe, this);
			this.sock = new Socket(address, port);
			this.out = new ObjectOutputStream(sock.getOutputStream());
			this.in = new ObjectInputStream(sock.getInputStream());
			
			this.nameToClientMap = new HashMap();
			this.nameToHandlerMap = new HashMap();
			this.name = name;
			this.mySeqNum = 0;
			seqEventQueue = new HashMap() ;
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
				switch (packetFromClient.type) {
				case ClientPacket.SERVER_ACKNOWLEDGE:
					System.out.println("ACK'ed");
					break;
				case ClientPacket.SERVER_RESPOND_PLAYERS:
					//processPlayerList();
					break;
				case ClientPacket.SERVER_EVENT_BROADCAST: // we wil only use this when a new player joins
					processSeq(packetFromClient);
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
	
	private void broadcastClients() throws IOException {
		ClientPacket packet = packetFromClient;
		packet.type = ClientPacket.SERVER_EVENT_BROADCAST;
		RemoteClientHandlerThread rch;
		Object[] keys = nameToHandlerMap.keySet().toArray();
		for(int i = 0; i < keys.length; i++){
			rch = (RemoteClientHandlerThread) nameToHandlerMap.get(keys[i]);
			rch.sendPacket(packet);
		}
	}
	
	public void processSeq(ClientPacket packet) {
		
		if(packet.sequence == (this.mySeqNum+1)) {
			processEvent(packet);
			this.mySeqNum++;
			while(!seqEventQueue.containsKey(this.mySeqNum)){
				processEvent((ClientPacket) seqEventQueue.get(this.mySeqNum));
				seqEventQueue.remove(this.mySeqNum);
				this.mySeqNum++;
			}
		} else {
			//This is when the seq number we got was too big. so put in the queue!
			seqEventQueue.put(packet.sequence, packet);
		}
	}
	
	private void processEvent(ClientPacket fromClient) {
		int event = fromClient.event;
		String name = fromClient.clientName;
		Client client = (Client) nameToClientMap.get(name);

		if (event == ClientEvent.FIRE) {
			assert (client != null);
			client.fire();
		} else if (event == ClientEvent.JOINED) {
			System.out.println(name + " joins the game");
			// addNewClient(packetFromClient.clientName, packetFromClient.info);
		} else if (event == ClientEvent.MOVE_BACKWARD) {
			assert (client != null);
			client.backup();
		} else if (event == ClientEvent.MOVE_FORWARD) {
			assert (client != null);
			client.forward();
		} else if (event == ClientEvent.QUIT && name.equals(this.name)) {
			Mazewar.quit();
		} else if (event == ClientEvent.TURN_LEFT) {
			assert (client != null);
			client.turnLeft();
		} else if (event == ClientEvent.TURN_RIGHT) {
			assert (client != null);
			client.turnRight();
		}

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
	
	public synchronized boolean addClientToMap(String name, RemoteClientHandlerThread handlerThread){ //TODO add the handler thread to map
//		return false;
		boolean  retValue = true;
		// client Map
		if(this.nameToClientMap.containsKey(name) ){
			retValue =  false;
		}else{
			this.nameToClientMap.put(name,new RemoteClient(name));
			System.out.println("Added Client with name <" + name + "> to the map");
			retValue =  true;
		}
		
		// HandlerMap
		if(this.nameToHandlerMap.containsKey(name)){
			retValue = false;
		}else{
			this.nameToHandlerMap.put(name, handlerThread);
			retValue = true;
		}
		return retValue;
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
			connectToRemotePlayers(packetFromServer);
		}
	}
	
	private void connectToRemotePlayers(ClientPacket packetFromServer) throws UnknownHostException, IOException {

		ServerDataBean bean = packetFromServer.serverData;
		if(bean == null){
			return;
		}
		ConnectionDetails conn;
		HashMap nameToConnMap = packetFromServer.serverData.nameToConnMap;
		if(nameToConnMap == null)
			return;
		Object[] names = nameToConnMap.keySet().toArray();
		
		for(int i = 0; i < names.length; i++){
			
			System.out.println("Name <" + names[i] + ">");
//			if(names[i].equals(this.name)){
//				continue;
//			}
			
			conn = (ConnectionDetails) nameToConnMap.get(names[i]);
			assert(conn != null);
			assert(rch != null);
			System.out.println("Connecting to port <" + conn.getPortNumber() + "> and host <" + conn.getHostName() + ">");
			rch.connectToRemoteClient(conn.getPortNumber(), conn.getHostName());
			
		}
		
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
		
		try {
			sendToServer(packetToServer);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public synchronized void sendToServer(ClientPacket packetToServer)throws IOException{
		assert (out != null);
		out.writeObject(packetToServer);
	}
	
	public String getClientName(){
		return this.name;
	}

	public ClientInformation getClientOrientation() {
		ClientInformation info = new ClientInformation();
		info.direction = this.hostClient.getOrientation();
		info.position = this.hostClient.getPoint();
		info.name = this.hostClient.getName();
		return info;
	}
	
	public void spawn(ClientInformation info){
		if(nameToClientMap.containsKey(info.name)){
			Client client = (Client) nameToClientMap.get(info.name);
			this.maze.addClient(client, info.position, info.direction);
		}else{
			System.out.println("INVALID NAME");
		}
		
	}

	public void registerClient(Client client) {
		// TODO Auto-generated method stub
		this.hostClient = client;
	}
}
