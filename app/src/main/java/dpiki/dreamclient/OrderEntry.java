package dpiki.dreamclient;

/**
 * Created by prog1 on 24.04.2016.
 */
public class OrderEntry {
    public int id;
    public String name;
    public int count;
    public int numTable;
    public String note;

    public OrderEntry(int id,String name, int count, int numTable, String note){
        this.id = id;
        this.name = name;
        this.count = count;
        this.numTable = numTable;
        this.note = note;
    }

    public OrderEntry(){
        this.id = 0;
        this.name = "";
        this.count = 0;
        this.numTable = 0;
        this.note = "";
    }
}
