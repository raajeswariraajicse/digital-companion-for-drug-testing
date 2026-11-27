package com.finora.tracerx.demo;

import com.finora.tracerx.core.ColorMatcher;
import com.finora.tracerx.core.HashChain;
import com.finora.tracerx.core.MedicineChecker;

import java.util.ArrayList;
import java.util.List;

/** Plain Java demo of the three core ideas. Run it with no framework: java com.finora.tracerx.demo.CoreDemo */
public class CoreDemo {
    public static void main(String[] args) {
        var refs = ColorMatcher.sampleReferences();

        var m = ColorMatcher.match(175, 90, 62, refs);
        System.out.printf("1) Color match: %s, Delta-E %.1f, confidence %d%%%n", m.substanceClass(), m.deltaE(), m.confidence());

        var warns = MedicineChecker.warnings(List.of("cough syrup"));
        System.out.println("2) Medicine warnings: " + warns);

        HashChain chain = new HashChain();
        chain.append("test=101;class=" + m.substanceClass() + ";conf=" + m.confidence());
        chain.append("test=102;class=Negative (sample);conf=95");
        chain.append("test=103;class=Opioid-class (sample);conf=78");
        System.out.println("3) Chain valid: " + chain.verify());

        List<HashChain.Block> copy = new ArrayList<>(chain.blocks());
        HashChain.Block b = copy.get(1);
        copy.set(1, new HashChain.Block(b.index(), "test=102;class=Opioid-class (sample);conf=95", b.prevHash(), b.hash()));
        System.out.println("   After editing record 102, chain valid: " + HashChain.verify(copy));
    }
}
