import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface FileSharingInterface extends Remote{
    public void receiveQuery(Query q) throws RemoteException;
    public void receiveQueryResponse(QueryResponse qr) throws RemoteException;

}