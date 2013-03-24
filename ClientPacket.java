import java.io.Serializable;

/**
 * 
 * @author rachittyagi
 *
 */
public class ClientPacket implements Serializable {

	public static final int CLIENT_REGISTER = 100;
	public static final int CLIENT_REQUEST_PLAYERS = 101;
	public static final int CLIENT_REQUEST_QUEUE = 102;
	public static final int CLIENT_EVENT_REQUEST = 103;
	public static final int CLIENT_ACK = 104;

	public static final int SERVER_RESPOND_PLAYERS = 105;
	public static final int SERVER_RESPOND_QUEUE = 106;
	public static final int SERVER_ACKNOWLEDGE = 107;
	public static final int SERVER_EVENT_BROADCAST = 108;

	public static final int GENERAL_NULL = 109;
	public static final int GENERAL_BYE = 110;

	//TODO add error codes

	public int type = GENERAL_NULL;
	public int errorCode = -1;
	public String message;
	public ClientEvent event;
	public ServerDataBean serverData;
	public String clientName = null;
	public ConnectionDetails connDetails;
}