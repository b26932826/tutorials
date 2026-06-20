package com.baeldung.kbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Identifies candlestick patterns from a sequence of KBar data.
 * Patterns are detected on the last N bars of the provided list.
 */
public class KBarAnalyzer {

    private static final double DOJI_RATIO       = 0.05;
    private static final double WICK_RATIO        = 2.0;
    private static final double MARUBOZU_RATIO    = 0.05;
    private static final double ENGULF_TOLERANCE  = 0.001;

    public List<KBarPattern> analyze(List<KBar> bars) {
        if (bars == null || bars.isEmpty()) return Collections.emptyList();

        List<KBarPattern> found = new ArrayList<>();
        int n = bars.size();
        KBar last  = bars.get(n - 1);
        KBar prev  = n >= 2 ? bars.get(n - 2) : null;
        KBar prev2 = n >= 3 ? bars.get(n - 3) : null;

        // ---- single-bar ----
        checkSingleBar(last, found);

        // ---- two-bar ----
        if (prev != null) {
            checkTwoBar(prev, last, found);
        }

        // ---- three-bar ----
        if (prev != null && prev2 != null) {
            checkThreeBar(prev2, prev, last, found);
        }

        return found;
    }

    // -------------------------------------------------------------------------
    // Single-bar helpers
    // -------------------------------------------------------------------------

    private void checkSingleBar(KBar c, List<KBarPattern> out) {
        double range = c.totalRange();
        if (range == 0) return;

        double bodyRatio = c.bodySize() / range;

        // Doji: body is very small relative to the total range
        if (bodyRatio <= DOJI_RATIO) {
            out.add(KBarPattern.DOJI);
            return;
        }

        // Marubozu: almost no wicks
        if (c.upperWick() / range <= MARUBOZU_RATIO && c.lowerWick() / range <= MARUBOZU_RATIO) {
            out.add(c.isBullish() ? KBarPattern.MARUBOZU_BULLISH : KBarPattern.MARUBOZU_BEARISH);
            return;
        }

        // Hammer / Hanging Man: long lower wick, small upper wick, small body
        if (c.lowerWick() >= WICK_RATIO * c.bodySize() && c.upperWick() <= c.bodySize()) {
            // Hammer after a downtrend, Hanging Man after an uptrend (context-free detection here)
            out.add(KBarPattern.HAMMER);
        }

        // Inverted Hammer / Shooting Star: long upper wick, small lower wick
        if (c.upperWick() >= WICK_RATIO * c.bodySize() && c.lowerWick() <= c.bodySize()) {
            out.add(c.isBullish() ? KBarPattern.INVERTED_HAMMER : KBarPattern.SHOOTING_STAR);
        }
    }

    // -------------------------------------------------------------------------
    // Two-bar helpers
    // -------------------------------------------------------------------------

    private void checkTwoBar(KBar p, KBar c, List<KBarPattern> out) {
        // Bullish Engulfing: previous bearish, current bullish body engulfs previous body
        if (p.isBearish() && c.isBullish()
                && c.getOpen() <= p.getClose() - ENGULF_TOLERANCE
                && c.getClose() >= p.getOpen() + ENGULF_TOLERANCE) {
            out.add(KBarPattern.BULLISH_ENGULFING);
        }

        // Bearish Engulfing
        if (p.isBullish() && c.isBearish()
                && c.getOpen() >= p.getClose() + ENGULF_TOLERANCE
                && c.getClose() <= p.getOpen() - ENGULF_TOLERANCE) {
            out.add(KBarPattern.BEARISH_ENGULFING);
        }

        // Piercing Line: bearish prev, bullish current opens below prev low,
        // closes above midpoint of prev body
        if (p.isBearish() && c.isBullish()
                && c.getOpen() < p.getLow()
                && c.getClose() > (p.getOpen() + p.getClose()) / 2.0
                && c.getClose() < p.getOpen()) {
            out.add(KBarPattern.PIERCING_LINE);
        }

        // Dark Cloud Cover: bullish prev, bearish current opens above prev high,
        // closes below midpoint of prev body
        if (p.isBullish() && c.isBearish()
                && c.getOpen() > p.getHigh()
                && c.getClose() < (p.getOpen() + p.getClose()) / 2.0
                && c.getClose() > p.getOpen()) {
            out.add(KBarPattern.DARK_CLOUD_COVER);
        }

        // Tweezer Bottom: lows are equal (within tolerance)
        double lowTol = p.totalRange() * ENGULF_TOLERANCE;
        if (Math.abs(p.getLow() - c.getLow()) <= lowTol && p.isBearish() && c.isBullish()) {
            out.add(KBarPattern.TWEEZER_BOTTOM);
        }

        // Tweezer Top: highs are equal
        if (Math.abs(p.getHigh() - c.getHigh()) <= lowTol && p.isBullish() && c.isBearish()) {
            out.add(KBarPattern.TWEEZER_TOP);
        }
    }

    // -------------------------------------------------------------------------
    // Three-bar helpers
    // -------------------------------------------------------------------------

    private void checkThreeBar(KBar b1, KBar b2, KBar b3, List<KBarPattern> out) {
        // Morning Star: b1 bearish big, b2 small body (star), b3 bullish big
        if (b1.isBearish() && b1.bodySize() > b1.totalRange() * 0.5
                && b2.bodySize() < b2.totalRange() * 0.3
                && b2.getClose() < b1.getClose()
                && b3.isBullish() && b3.getClose() > (b1.getOpen() + b1.getClose()) / 2.0) {
            out.add(KBarPattern.MORNING_STAR);
        }

        // Evening Star: b1 bullish big, b2 small body (star), b3 bearish big
        if (b1.isBullish() && b1.bodySize() > b1.totalRange() * 0.5
                && b2.bodySize() < b2.totalRange() * 0.3
                && b2.getClose() > b1.getClose()
                && b3.isBearish() && b3.getClose() < (b1.getOpen() + b1.getClose()) / 2.0) {
            out.add(KBarPattern.EVENING_STAR);
        }

        // Three White Soldiers: three consecutive bullish bars, each closing higher
        if (b1.isBullish() && b2.isBullish() && b3.isBullish()
                && b2.getClose() > b1.getClose()
                && b3.getClose() > b2.getClose()
                && b2.getOpen() > b1.getOpen()
                && b3.getOpen() > b2.getOpen()) {
            out.add(KBarPattern.THREE_WHITE_SOLDIERS);
        }

        // Three Black Crows: three consecutive bearish bars, each closing lower
        if (b1.isBearish() && b2.isBearish() && b3.isBearish()
                && b2.getClose() < b1.getClose()
                && b3.getClose() < b2.getClose()
                && b2.getOpen() < b1.getOpen()
                && b3.getOpen() < b2.getOpen()) {
            out.add(KBarPattern.THREE_BLACK_CROWS);
        }

        // Three Inside Up: b1 bearish, b2 bullish engulfs b1 partially, b3 bullish closes above b1 open
        if (b1.isBearish() && b2.isBullish()
                && b2.getOpen() >= b1.getClose() && b2.getClose() <= b1.getOpen()
                && b3.isBullish() && b3.getClose() > b1.getOpen()) {
            out.add(KBarPattern.THREE_INSIDE_UP);
        }

        // Three Inside Down: b1 bullish, b2 bearish inside b1, b3 bearish closes below b1 open
        if (b1.isBullish() && b2.isBearish()
                && b2.getOpen() <= b1.getClose() && b2.getClose() >= b1.getOpen()
                && b3.isBearish() && b3.getClose() < b1.getOpen()) {
            out.add(KBarPattern.THREE_INSIDE_DOWN);
        }
    }
}
