package com.finora.tracerx.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Warns when a declared medicine or food may react with a kit. SAMPLE rules: replace with the kit datasheet's cross-reactivity list. */
public final class MedicineChecker {

    private static final Map<String, String> SAMPLE_RULES = Map.of(
        "cough syrup", "Some cough syrups may react with opioid-class kits",
        "codeine", "Codeine may react with opioid-class kits",
        "poppy seed", "Poppy seeds may react with opioid-class kits",
        "pseudoephedrine", "Decongestants may react with stimulant-class kits",
        "ibuprofen", "Some pain relievers are reported to affect cannabis-class kits"
    );

    private MedicineChecker() {}

    public static List<String> warnings(List<String> declared) {
        List<String> out = new ArrayList<>();
        if (declared == null) return out;
        for (String d : declared) {
            String key = d.toLowerCase(Locale.ROOT).trim();
            for (var rule : SAMPLE_RULES.entrySet()) {
                if (key.contains(rule.getKey())) {
                    out.add("Declared '" + d + "': " + rule.getValue() + ". Result is not conclusive.");
                }
            }
        }
        return out;
    }
}
