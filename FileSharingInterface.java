import java.rmi.Remote; 
import java.rmi.RemoteException; 

public interface FileSharingInterface extends Remote{
    public void receiveQuery(Query q);
    public void receiveQueryResponse(String next, String owner, String filename);

}