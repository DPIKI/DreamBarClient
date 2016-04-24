package dpiki.dreamclient;

/**
 * Created by prog1 on 24.04.2016.
 */
public class OrderEntry {
    public int id;
    public int count;
    public int numTable;

    public OrderEntry(int id, int count, int numTable){
        this.id = id;
        this.count = count;
        this.numTable = numTable;
    }

    public OrderEntry(){
        this.id = 0;
        this.count = 0;
        this.numTable = -1;
    }
}
