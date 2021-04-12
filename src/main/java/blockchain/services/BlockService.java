package blockchain.services;

import blockchain.model.Block;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class BlockService {

    private List<Block> blockchain;
    private Integer DIFFICULTY_ADJUSTMENT_INTERVAL = 10;
    private Integer BLOCK_GENERATION_INTERVAL = 10;

    public BlockService() {
        //Initialize
        try {
            File blocksfile = new File("/Users/green/Git/7200Project/src/main/resources/blockchain.txt");
            Scanner scanner = new Scanner(blocksfile);
            String jsonString = "";
            while(scanner.hasNextLine()) {
                jsonString += scanner.nextLine();
            }
            blockchain = new Gson().fromJson(jsonString, new TypeToken<List<Block>>(){}.getType());
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Block generateNextBlock(String blockData) {

        Integer difficulty = getDifficulty();
        Block previousBlock = getLastBlock();
        Integer nextIndex = previousBlock.getIndex().intValue() + 1;
        System.out.println("The index of current block: " + nextIndex);
        System.out.println("The difficulty of current block: " + difficulty);
        Long nextTimestamp = new Date().getTime() / 1000;
        Integer nonce = findNonce(nextIndex, nextTimestamp, blockData, previousBlock.getHash(), difficulty);
        String nextHash = calculateHash(nextIndex, nextTimestamp, blockData, previousBlock.getHash(), difficulty, nonce);
        System.out.println("The hash of new block is:");
        System.out.println(byteToHex(nextHash.getBytes()));
        Block newBlock = new Block(nextIndex, nextHash, previousBlock.getHash(), nextTimestamp, blockData, difficulty, nonce);
        blockchain.add(newBlock);
        System.out.println("The new BLOCK has been added: ");
        writeDown();
        return newBlock;
    }

    public String addNewBlockByBroadcast(String newBlockJson) {

        Block newBlock = new Gson().fromJson(newBlockJson, Block.class);
        String ret;
        if(isValidNewBlock(newBlock)) {
            blockchain.add(newBlock);
            ret = "New block is VALID";
        } else {
            ret = "New block is NOT VALID";
        }
        System.out.println(ret);
        return ret;
    }

    private Boolean isValidNewBlock(Block newBlock) {

        Block prevBlock = getLastBlock();
        if (prevBlock.getIndex() + 1 != newBlock.getIndex()) return false;
        if (prevBlock.getHash() != newBlock.getPreviousHash()) return false;
        if (hashBlock(newBlock) != newBlock.getHash()) return false;
        return true;
    }

    private Block getLastBlock() {

        return blockchain.get(blockchain.size()-1);
    }

    public String getLastBlockJson() {

        Block lastBlock = blockchain.get(blockchain.size()-1);
        return new Gson().toJson(lastBlock);
    }

    private String hashBlock(Block block) {

        Integer index = block.getIndex();
        Long timestamp = block.getTimestamp();
        String blockData = block.getData();
        String previousHash = block.getPreviousHash();
        Integer difficulty = block.getDifficulty();
        Integer nonce = block.getNonce();
        return calculateHash(index, timestamp, blockData, previousHash, difficulty, nonce);
    }

    private String calculateHash(Integer index, Long timestamp, String blockData, String previousHash, Integer difficulty, Integer nonce) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String text = index + previousHash + timestamp + blockData + difficulty + nonce;
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return byteToHex(hash);
        } catch(Exception e) {
            return e.getMessage();
        }

    }

    private Integer getDifficulty() {

        Block lastBlock = getLastBlock();
        if(lastBlock.getIndex() % DIFFICULTY_ADJUSTMENT_INTERVAL == 0 && lastBlock.getIndex() != 0) {
            return getAdjustedDifficulty();
        } else {
            return lastBlock.getDifficulty();
        }
    }

    private Integer findNonce(Integer index, Long timestamp, String blockData, String previousHash, Integer difficulty) {

        Integer nonce = 0;
        while(true) {
            String hash = calculateHash(index, timestamp, blockData, previousHash, difficulty, nonce);
            if(hashMatchesDifficulty(hash, difficulty)) {
                System.out.println("Congratulations!");
                System.out.println("The nonce is: " + nonce);
                writeDown();
                return nonce;
            }
            nonce++;
        }
    }

    private Boolean hashMatchesDifficulty(String hash, Integer difficulty) {

        byte[] hashInBinary = hash.getBytes();
        Integer numLeadZeros = 0;
        for(byte b : hashInBinary) {
            int val = b;
            for(int i = 0; i < 8; i++) {
                if((val & 128) == 0) {
                    numLeadZeros ++;
                } else {
                    if (numLeadZeros >= difficulty) {
                        return true;
                    } else {
                        return false;
                    }
                }
                val <<= 1;
            }
        }
        return false;
    }

    private Integer getAdjustedDifficulty() {

        Block prevAdjustmentBlock = blockchain.get(blockchain.size() - DIFFICULTY_ADJUSTMENT_INTERVAL);
        Block lastBlock = getLastBlock();
        Integer timeExpected = BLOCK_GENERATION_INTERVAL * DIFFICULTY_ADJUSTMENT_INTERVAL;
        Long timeTaken = lastBlock.getTimestamp() - prevAdjustmentBlock.getTimestamp();
        if(timeTaken > timeExpected * 2) {
            return lastBlock.getDifficulty() - 1;
        } else if(timeTaken < timeExpected / 2) {
            return  lastBlock.getDifficulty() + 1;
        }
        return lastBlock.getDifficulty();
    }

    private String byteToHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    private void writeDown() {

        try {
            FileWriter writer = new FileWriter("/Users/green/Git/7200Project/src/main/resources/blockchain.txt");
            String jsonString = new Gson().toJson(blockchain);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        BlockService a = new BlockService();
//        System.out.println(a.generateNextBlock("The Second One"));
    }
}
