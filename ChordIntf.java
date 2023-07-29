import java.rmi.Remote; 
import java.rmi.RemoteException;  
import java.util.*;
public interface ChordIntf extends Remote {
	final public static int N = 8;
	final public static int m = 3;
	public NodeInfo find_successor(int n) throws RemoteException;
	public NodeInfo get_predecessor() throws RemoteException;
	public void notify(NodeInfo n_dash) throws RemoteException;
	public NodeInfo lookup(int peer) throws RemoteException;
	public void printMsg(int node_no) throws RemoteException;
	public void  start_server(int listening_port) throws RemoteException;
}
