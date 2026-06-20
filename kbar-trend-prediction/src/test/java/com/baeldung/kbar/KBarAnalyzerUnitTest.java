package com.baeldung.kbar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class KBarAnalyzerUnitTest {

    private KBarAnalyzer analyzer;

    @Before
    public void setUp() {
        analyzer = new KBarAnalyzer();
    }

    private KBar bar(double open, double high, double low, double close) {
        return new KBar(LocalDateTime.now(), open, high, low, close, 1000L);
    }

    // -------------------------------------------------------------------------
    // Single-bar patterns
    // -------------------------------------------------------------------------

    @Test
    public void givenDojiCandle_whenAnalyze_thenDetectDoji() {
        // open and close are almost equal, long wicks on both sides
        KBar doji = bar(100.0, 105.0, 95.0, 100.1);
        List<KBarPattern> patterns = analyzer.analyze(Collections.singletonList(doji));
        assertTrue(patterns.contains(KBarPattern.DOJI));
    }

    @Test
    public void givenBullishMarubozu_whenAnalyze_thenDetectMarubozu() {
        // open == low, close == high — no wicks
        KBar marubozu = bar(100.0, 110.0, 100.0, 110.0);
        List<KBarPattern> patterns = analyzer.analyze(Collections.singletonList(marubozu));
        assertTrue(patterns.contains(KBarPattern.MARUBOZU_BULLISH));
    }

    @Test
    public void givenHammerCandle_whenAnalyze_thenDetectHammer() {
        // Long lower wick (~12), small body (~2), tiny upper wick (~1) — clearly not a doji
        KBar hammer = bar(100.0, 103.0, 88.0, 102.0);
        List<KBarPattern> patterns = analyzer.analyze(Collections.singletonList(hammer));
        assertTrue(patterns.contains(KBarPattern.HAMMER));
    }

    @Test
    public void givenShootingStarCandle_whenAnalyze_thenDetectShootingStar() {
        // Bearish bar: long upper wick (~12), small body (~1.5), tiny lower wick (~0.5)
        KBar shootingStar = bar(100.0, 112.0, 98.0, 98.5);
        List<KBarPattern> patterns = analyzer.analyze(Collections.singletonList(shootingStar));
        assertTrue(patterns.contains(KBarPattern.SHOOTING_STAR));
    }

    // -------------------------------------------------------------------------
    // Two-bar patterns
    // -------------------------------------------------------------------------

    @Test
    public void givenBullishEngulfing_whenAnalyze_thenDetect() {
        KBar bearish = bar(105.0, 106.0, 98.0, 99.0);
        KBar bullish = bar(97.0, 112.0, 97.0, 111.0); // engulfs bearish body
        List<KBarPattern> patterns = analyzer.analyze(Arrays.asList(bearish, bullish));
        assertTrue(patterns.contains(KBarPattern.BULLISH_ENGULFING));
    }

    @Test
    public void givenBearishEngulfing_whenAnalyze_thenDetect() {
        KBar bullish = bar(95.0, 106.0, 95.0, 105.0);
        KBar bearish = bar(107.0, 108.0, 90.0, 91.0); // engulfs bullish body
        List<KBarPattern> patterns = analyzer.analyze(Arrays.asList(bullish, bearish));
        assertTrue(patterns.contains(KBarPattern.BEARISH_ENGULFING));
    }

    @Test
    public void givenPiercingLine_whenAnalyze_thenDetect() {
        KBar bearish = bar(110.0, 111.0, 98.0, 100.0); // open=110, close=100
        // Bullish: opens below prev low (97 < 98), closes above midpoint of prev body (105 > 105.0 = (110+100)/2)
        KBar bullish = bar(97.0, 108.0, 97.0, 106.0);
        List<KBarPattern> patterns = analyzer.analyze(Arrays.asList(bearish, bullish));
        assertTrue(patterns.contains(KBarPattern.PIERCING_LINE));
    }

    // -------------------------------------------------------------------------
    // Three-bar patterns
    // -------------------------------------------------------------------------

    @Test
    public void givenThreeWhiteSoldiers_whenAnalyze_thenDetect() {
        KBar b1 = bar(100.0, 104.0, 99.0, 103.0);
        KBar b2 = bar(104.0, 108.0, 103.0, 107.0);
        KBar b3 = bar(108.0, 113.0, 107.0, 112.0);
        List<KBarPattern> patterns = analyzer.analyze(Arrays.asList(b1, b2, b3));
        assertTrue(patterns.contains(KBarPattern.THREE_WHITE_SOLDIERS));
    }

    @Test
    public void givenThreeBlackCrows_whenAnalyze_thenDetect() {
        KBar b1 = bar(112.0, 113.0, 108.0, 109.0);
        KBar b2 = bar(108.0, 109.0, 104.0, 105.0);
        KBar b3 = bar(104.0, 105.0, 100.0, 101.0);
        List<KBarPattern> patterns = analyzer.analyze(Arrays.asList(b1, b2, b3));
        assertTrue(patterns.contains(KBarPattern.THREE_BLACK_CROWS));
    }

    @Test
    public void givenMorningStar_whenAnalyze_thenDetect() {
        KBar bigBearish = bar(120.0, 121.0, 104.0, 105.0); // large bearish body
        KBar star       = bar(103.0, 105.0, 101.0, 104.0); // small body star
        KBar bigBullish = bar(105.0, 120.0, 104.0, 118.0); // large bullish closes above midpoint of b1
        List<KBarPattern> patterns = analyzer.analyze(Arrays.asList(bigBearish, star, bigBullish));
        assertTrue(patterns.contains(KBarPattern.MORNING_STAR));
    }

    @Test
    public void givenEmptyBars_whenAnalyze_thenReturnEmpty() {
        List<KBarPattern> patterns = analyzer.analyze(Collections.emptyList());
        assertTrue(patterns.isEmpty());
    }
}
