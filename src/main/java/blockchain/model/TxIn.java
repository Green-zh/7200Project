package blockchain.model;

import lombok.Data;

@Data
public class TxIn {

    private String txOutId;
    private Integer txOutIndex;
    private String signature;

    public TxIn(String txOutId, Integer txOutIndex, String signature) {
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
        this.signature = signature;
    }
}
