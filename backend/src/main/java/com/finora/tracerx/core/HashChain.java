package com.finora.tracerx.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

/** Tamper-evident log: every block stores the hash of the block before it (SHA-256). */
public final class HashChain {

    public record Block(long index, String payload, String prevHash, String hash) {}

    private static final String GENESIS = "0".repeat(64);
    private final List<Block> blocks = new ArrayList<>();

    public synchronized Block append(String payload) {
        String prev = blocks.isEmpty() ? GENESIS : blocks.get(blocks.size() - 1).hash();
        long index = blocks.size();
        Block block = new Block(index, payload, prev, hash(index, payload, prev));
        blocks.add(block);
        return block;
    }

    public synchronized List<Block> blocks() {
        return Collections.unmodifiableList(new ArrayList<>(blocks));
    }

    public synchronized boolean verify() {
        return verify(blocks);
    }

    /** Returns false if any block was edited, removed or reordered. */
    public static boolean verify(List<Block> chain) {
        String prev = GENESIS;
        for (int i = 0; i < chain.size(); i++) {
            Block b = chain.get(i);
            if (b.index() != i || !b.prevHash().equals(prev)) return false;
            if (!b.hash().equals(hash(b.index(), b.payload(), b.prevHash()))) return false;
            prev = b.hash();
        }
        return true;
    }

    private static String hash(long index, String payload, String prevHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest((index + "|" + prevHash + "|" + payload).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
