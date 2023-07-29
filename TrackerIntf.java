import java.rmi.Remote; 
import java.rmi.RemoteException;  
import java.util.*;

public interface TrackerIntf extends Remote {
	final public static int N = 8;
	final public static int m = 3;
	public int create(int node_no, String ip_addr, int port_no) throws RemoteException;
	public NodeInfo get_available_node()throws RemoteException;
	public ArrayList<Integer> get_peers()throws RemoteException;
	public void node_failure_update(int n) throws RemoteException;
}