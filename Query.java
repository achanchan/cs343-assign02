public class Query{

    private String current_ip; 
    private String original_ip;
    private String filename; 

    public Query(String current_ip, String original_ip, String filename){
        this.current_ip=current_ip; 
        this.original_ip=original_ip; 
        this.filename=filename; 

    }

    public String getCurrentIP(){
        return current_ip; 
    }

    public String getOriginalIP(){
        return original_ip; 
    }

    public String getFilename(){
        return filename; 
    }
}