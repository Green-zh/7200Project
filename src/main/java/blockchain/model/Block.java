package blockchain.model;

import lombok.Data;

@Data
public class Block {

    private Integer index;
    private String hash;
    private String previousHash;
    private Long timestamp;
    private String data;
    private Integer difficulty;
    private Integer nonce;
    private String merkleRootHash;

    public Block(Integer index, String hash, String previousHash, Long timestamp, String data, Integer difficulty, Integer nonce) {
        this.index = index;
        this.hash = hash;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = nonce;
    }
}
