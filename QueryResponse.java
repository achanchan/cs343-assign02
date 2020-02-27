public class QueryResponse{

    private String nextpeer_ip; 
    private String owner_ip;
    private String filename; 

    public QueryResponse(String nextpeer_ip, String owner_ip, String filename){
        this.nextpeer_ip=nextpeer_ip; 
        this.owner_ip=owner_ip; 
        this.filename=filename; 

    }

    public String getNextPeer(){
        return nextpeer_ip; 
    }

    public String getOwner(){
        return owner_ip; 
    }

    public String getFilename(){
        return filename; 
    }
}