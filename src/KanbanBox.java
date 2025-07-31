import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class KanbanBox {
    private String epc;
    private String itemRef;
    private int capacidade;

    public KanbanBox(String epc, String itemRef, int capacidade) {
        this.epc = epc;
        this.itemRef = itemRef;
        this.capacidade = capacidade;
    }

    public String getEpc() {
        return epc;
    }

    public String getItemRef() {
        return itemRef;
    }

    public int getCapacidade() {
        return capacidade;
    }
}
