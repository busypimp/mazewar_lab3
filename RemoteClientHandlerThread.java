import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteClientHandlerThread extends Thread {

	private Socket sock;
	private ClientHandler host;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ClientPacket packetFromClient;
//	private boolean isClientRegistered = false;

	public RemoteClientHandlerThread(Socket incomingSocket,
			ClientHandler hostHandler) {
			this.host = hostHandler;
			this.sock = incomingSocket;
	}

	public void run() {
		try {
			
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());

			
			introduceYourself();
			while ((packetFromClient = (ClientPacket) in.readObject()) != null) {
				System.out.println("Got a packet from remoteClient");
				switch(packetFromClient.type){
				case ClientPacket.CLIENT_REGISTER:
					registerClient();
					requestOrientation();
					break;
				case ClientPacket.CLIENT_RESPOND_ORIENTATION:
					spawnClient();
					break;
				case ClientPacket.CLIENT_REQUEST_ORIENTATION:
					sendCurrentOrientation();
					break;
				case ClientPacket.SERVER_EVENT_BROADCAST:
					processEvent();
					break;
				default:
					System.err.println("ERROR!! Invalid packet type");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void processEvent() {
		// TODO Auto-generated method stub
		this.host.processSeq(packetFromClient);
		
	}

	private void sendCurrentOrientation() throws IOException {
		ClientPacket packet = new ClientPacket();
		packet.type = ClientPacket.CLIENT_RESPOND_ORIENTATION;
		packet.clientName = host.getClientName();
		packet.info = host.getClientOrientation();
		
		sendPacket(packet);
		
	}

	private void spawnClient() {
		this.host.spawn(packetFromClient.info);
		
	}

	private void requestOrientation() throws IOException {
		ClientPacket packet = new ClientPacket();
		packet.type = ClientPacket.CLIENT_REQUEST_ORIENTATION;
		
		sendPacket(packet);
	}

	private void introduceYourself() throws IOException {
		System.out.println("Sending intro packet");
		ClientPacket packet = new ClientPacket();
		packet.type = ClientPacket.CLIENT_REGISTER;
		packet.clientName = host.getClientName();
		
		sendPacket(packet);
	}

	public synchronized void sendPacket(ClientPacket packetToOthers) throws IOException {
		out.writeObject(packetToOthers);
	}

	private boolean registerClient() {
		assert (packetFromClient.clientName != null);
//		System.out.println);
		return host.addClientToMap(packetFromClient.clientName, this);
	}
}
