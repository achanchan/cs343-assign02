import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import javafx.util.Pair;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.*; 

public class Peer implements FileSharingInterface{
    Hashtable<String, FileSharingInterface> neighbors;
    Vector<String> filenames;
    String my_ip;
    Hashtable<String, String> request_record;

    public Peer(){

    }

    public void receiveQuery(Query q){
        // check if has file
        // check to see if already in map
        // if yes, do nothing
        // if no, add to map and propagate to neighbors

        synchronized(this){
            String current = q.getCurrentIP();
            String prev = q.getOriginalIP();
            String filename = q.getFilename(); 
            
            if (filenames.contains(filename)){
                // send query response
            }
            if (!request_record.containsKey(filename)){
                // add to map and propagate to neighbors
                request_record.put(filename, prev);
                Query newQuery = new Query(my_ip, current, filename);
                for (String n: neighbors.keySet()){
                    if (!n.equals(current)){
                        FileSharingInterface nextNeighbor = neighbors.get(n);
                        try{
                        sendQuery(newQuery, nextNeighbor, n); 
                        } catch (Exception e){
                            System.err.println("Client exception: " + e.toString());
                            e.printStackTrace();
                        }

                    }
                }

            }
        }    
    }

    public void sendQuery(Query q, FileSharingInterface neighbor, String neighbor_ip) throws RemoteException, MalformedURLException{
        try{
        Registry registry = LocateRegistry.getRegistry(neighbor_ip);
        neighbor = (FileSharingInterface) registry.lookup(neighbor_ip);
        neighbor.receiveQuery(q);

        } catch (Exception e){
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }


    }

    public void receiveQueryResponse(String next, String owner, String filename){
        String x = "1";
    }

    public static void main(String[] args){

    }
}