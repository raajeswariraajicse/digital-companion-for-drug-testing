package com.finora.tracerx.core;

import java.util.List;

/** Converts a measured RGB color to CIELAB and finds the closest reference color (Delta-E CIE76). */
public final class ColorMatcher {

    /** A reference color from the kit datasheet. SAMPLE values below must be replaced with the real kit chart. */
    public record Reference(String substanceClass, int r, int g, int b) {}

    public record Match(String substanceClass, double deltaE, int confidence) {}

    private ColorMatcher() {}

    /** SAMPLE data only. Replace with the colors printed on the kit manufacturer's chart. */
    public static List<Reference> sampleReferences() {
        return List.of(
            new Reference("Opioid-class (sample)", 181, 85, 59),
            new Reference("Stimulant-class (sample)", 120, 60, 140),
            new Reference("Cannabis-class (sample)", 70, 120, 150),
            new Reference("Negative (sample)", 240, 235, 225)
        );
    }

    public static double[] rgbToLab(int r, int g, int b) {
        double R = srgbToLinear(r / 255.0), G = srgbToLinear(g / 255.0), B = srgbToLinear(b / 255.0);
        // sRGB (D65) to XYZ
        double x = 0.4124564 * R + 0.3575761 * G + 0.1804375 * B;
        double y = 0.2126729 * R + 0.7151522 * G + 0.0721750 * B;
        double z = 0.0193339 * R + 0.1191920 * G + 0.9503041 * B;
        // D65 reference white
        double fx = f(x / 0.95047), fy = f(y / 1.0), fz = f(z / 1.08883);
        return new double[] { 116 * fy - 16, 500 * (fx - fy), 200 * (fy - fz) };
    }

    public static double deltaE76(double[] a, double[] b) {
        double dl = a[0] - b[0], da = a[1] - b[1], db = a[2] - b[2];
        return Math.sqrt(dl * dl + da * da + db * db);
    }

    public static Match match(int r, int g, int b, List<Reference> refs) {
        double[] measured = rgbToLab(r, g, b);
        Reference best = null;
        double bestDe = Double.MAX_VALUE;
        for (Reference ref : refs) {
            double de = deltaE76(measured, rgbToLab(ref.r(), ref.g(), ref.b()));
            if (de < bestDe) { bestDe = de; best = ref; }
        }
        // Heuristic: Delta-E 0 -> 100%, every extra unit lowers confidence by 3 points.
        int confidence = (int) Math.max(0, Math.min(100, Math.round(100 - bestDe * 3)));
        return new Match(best.substanceClass(), bestDe, confidence);
    }

    private static double srgbToLinear(double c) {
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double f(double t) {
        return t > 0.008856 ? Math.cbrt(t) : (7.787 * t) + 16.0 / 116.0;
    }
}
