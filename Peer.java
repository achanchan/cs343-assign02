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

    public void receiveQueryResponse(QueryResponse qr) throws RemoteException{
        String nextpeer_ip = qr.getNextPeer();
        String owner_ip = qr.getOwner();
        String filename = qr.getFilename();

        if (request_record.containsKey(filename)){
            String prev = request_record.get(filename); 
            QueryResponse newQR = new QueryResponse(nextpeer_ip, owner_ip, filename);
            FileSharingInterface nextNeighbor = neighbors.get(prev);
                        try{
                            sendQueryResponse(newQR, nextNeighbor, prev); 
                        } catch (Exception e){
                            System.err.println("Client exception: " + e.toString());
                            e.printStackTrace();
                        }
            

        }
    }

    public void sendQueryResponse(QueryResponse qr, FileSharingInterface neighbor, String neighbor_ip) 
    throws RemoteException, MalformedURLException{
        try{
            Registry registry = LocateRegistry.getRegistry(neighbor_ip);
            neighbor = (FileSharingInterface) registry.lookup(neighbor_ip);
            neighbor.receiveQueryResponse(qr);;
    
            } catch (Exception e){
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
            }
    

    }

    public static void main(String[] args){

    }
}