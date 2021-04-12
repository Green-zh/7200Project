package blockchain.services;

import blockchain.common.ECDSAUtils;
import blockchain.model.Transaction;
import blockchain.model.TxIn;
import blockchain.model.TxOut;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransactionService {

    private List<Transaction> txs;
    private Double COINBASE_AMOUNT = 50.0;

    public TransactionService() {
        //Initialize
        try {
            File transactionsFile = new File("/Users/green/Git/7200Project/src/main/resources/transactions.txt");
            Scanner scanner = new Scanner(transactionsFile);
            String jsonString = "";
            while(scanner.hasNextLine()) {
                jsonString += scanner.nextLine();
            }
            txs = new Gson().fromJson(jsonString, new TypeToken<List<Transaction>>(){}.getType());
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Transaction createTransaction(Double amount, Boolean isCoinbaseTx) {

        List<TxOut> txOuts = new ArrayList<>();
        List<TxIn> txIns = new ArrayList<>();
        String signature = "";
        String address = "";
        try {
            KeyPair keyPair = ECDSAUtils.getKeyPair();
            address = DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded());
//            signature = ECDSAUtils.signECDSA(keyPair.getPrivate(), String.valueOf(tx.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(isCoinbaseTx == true) {
            TxOut txOut = new TxOut(address, COINBASE_AMOUNT);
            txOuts.add(txOut);
        } else {
//            for(Transaction tx : txs) {
//                for(TxOut txOut : tx.getTxOuts()) {
//                    try {
//                        KeyPair keyPair = ECDSAUtils.getKeyPair();
//                        address = DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded());
//                        signature = ECDSAUtils.signECDSA(keyPair.getPrivate(), String.valueOf(tx.getId()));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    TxIn newTxIn = new TxIn(tx.getId(), tx.getTxOuts().indexOf(txOut)+1, signature);
//                    txIns.add(newTxIn);
//                    if(txOut.getAmount() >= amount) {
//                        TxOut newTxOut = new TxOut(address, amount);
//                        txOuts.add(newTxOut);
//                        TxOut newTxOut2 = new TxOut("localhost", txOut.getAmount() - amount);//todo
//                        txOuts.add(newTxOut2);
//                        amount = 0.0;
//                    } else {
//                        TxOut newTxOut = new TxOut(address, txOut.getAmount());
//                        txOuts.add(newTxOut);
//                        amount -= txOut.getAmount();
//                    }
//                    tx.getTxOuts().remove(txOut);
//                }
//                if(txs.size() == 0) txs.remove(tx);
//                if(amount == 0) break;
//            }
        }
        if(amount > 0) return null;
        String txId = generateTxId(txOuts, txIns);
        Transaction newTransaction = new Transaction(txId, txIns, txOuts);
        txs = new ArrayList<>();
        txs.add(newTransaction);
        System.out.println("New Transaction created.");
        writeDown();
        return newTransaction;
    }

    private String generateTxId(List<TxOut> txOuts, List<TxIn> txIns) {

        String txOutContent = "";
        for(TxOut txOut : txOuts) {
            txOutContent += txOut.getAddress() + txOut.getAmount().toString();
        }
        String txInContent = "";
        for(TxIn txIn : txIns) {
            txInContent += txIn.getTxOutId() + txIn.getTxOutIndex().toString();
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String text = txInContent + txOutContent;
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return byteToHex(hash);
        } catch(Exception e) {
            return e.getMessage();
        }
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
            FileWriter writer = new FileWriter("/Users/green/Git/7200Project/src/main/resources/transactions.txt");
            String jsonString = new Gson().toJson(txs);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {


    }
}
