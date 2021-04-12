package blockchain.model;

import lombok.Data;

import java.util.List;

@Data
public class Transaction {

    private String id;
    private List<TxIn> txIns;
    private List<TxOut> txOuts;

    public Transaction(String id, List<TxIn> txIns, List<TxOut> txOuts) {
        this.id = id;
        this.txIns = txIns;
        this.txOuts = txOuts;
    }
}
