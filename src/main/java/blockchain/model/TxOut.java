package blockchain.model;

import lombok.Data;

@Data
public class TxOut {

    private String address;
    private Double amount;

    public TxOut(String address, Double amount) {
        this.address = address;
        this.amount = amount;
    }
}
