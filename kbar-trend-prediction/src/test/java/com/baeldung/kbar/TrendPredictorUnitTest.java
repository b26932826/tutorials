package com.baeldung.kbar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TrendPredictorUnitTest {

    private TrendPredictor predictor;

    @Before
    public void setUp() {
        predictor = new TrendPredictor(3);
    }

    private KBar bar(double open, double high, double low, double close) {
        return new KBar(LocalDateTime.now(), open, high, low, close, 1000L);
    }

    @Test
    public void givenThreeWhiteSoldiers_whenPredict_thenBullish() {
        KBar b1 = bar(100.0, 104.0, 99.0, 103.0);
        KBar b2 = bar(104.0, 108.0, 103.0, 107.0);
        KBar b3 = bar(108.0, 113.0, 107.0, 112.0);

        TrendPrediction prediction = predictor.predict(Arrays.asList(b1, b2, b3));

        assertEquals(TrendDirection.BULLISH, prediction.getDirection());
        assertTrue(prediction.getConfidence() > 0.0);
        assertTrue(prediction.getTriggeringPatterns().contains(KBarPattern.THREE_WHITE_SOLDIERS));
    }

    @Test
    public void givenThreeBlackCrows_whenPredict_thenBearish() {
        KBar b1 = bar(112.0, 113.0, 108.0, 109.0);
        KBar b2 = bar(108.0, 109.0, 104.0, 105.0);
        KBar b3 = bar(104.0, 105.0, 100.0, 101.0);

        TrendPrediction prediction = predictor.predict(Arrays.asList(b1, b2, b3));

        assertEquals(TrendDirection.BEARISH, prediction.getDirection());
        assertTrue(prediction.getConfidence() > 0.0);
        assertTrue(prediction.getTriggeringPatterns().contains(KBarPattern.THREE_BLACK_CROWS));
    }

    @Test
    public void givenBullishEngulfing_whenPredict_thenBullish() {
        KBar bearish = bar(105.0, 106.0, 98.0, 99.0);
        KBar bullish = bar(97.0, 112.0, 97.0, 111.0);

        TrendPrediction prediction = predictor.predict(Arrays.asList(bearish, bullish));

        assertEquals(TrendDirection.BULLISH, prediction.getDirection());
    }

    @Test
    public void givenBearishEngulfing_whenPredict_thenBearish() {
        KBar bullish = bar(95.0, 106.0, 95.0, 105.0);
        KBar bearish = bar(107.0, 108.0, 90.0, 91.0);

        TrendPrediction prediction = predictor.predict(Arrays.asList(bullish, bearish));

        assertEquals(TrendDirection.BEARISH, prediction.getDirection());
    }

    @Test
    public void givenSingleBar_whenPredict_thenReturnsPrediction() {
        KBar single = bar(100.0, 110.0, 95.0, 108.0);
        TrendPrediction prediction = predictor.predict(Arrays.asList(single));
        assertNotNull(prediction);
        assertNotNull(prediction.getDirection());
        assertTrue(prediction.getConfidence() >= 0.0);
        assertTrue(prediction.getConfidence() <= 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNullBars_whenPredict_thenThrowException() {
        predictor.predict(null);
    }

    @Test
    public void givenRisingCloses_whenComputeSma_thenReturnAverage() {
        List<KBar> bars = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            bars.add(bar(i * 10.0, i * 10.0 + 1, i * 10.0 - 1, i * 10.0));
        }
        // closes: 10, 20, 30, 40, 50 → SMA(5) = 30
        double sma = predictor.computeSma(bars, 5);
        assertEquals(30.0, sma, 0.001);
    }

    @Test
    public void givenBarSeries_whenComputeSmaList_thenCorrectLength() {
        List<KBar> bars = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            bars.add(bar(100.0, 101.0, 99.0, 100.0));
        }
        List<Double> smaList = predictor.computeSmaList(bars, 3);
        assertEquals(7, smaList.size());
        assertNull(smaList.get(0));
        assertNull(smaList.get(1));
        assertNotNull(smaList.get(2));
    }

    @Test
    public void givenConfidence_whenPredict_thenWithinRange() {
        List<KBar> bars = Arrays.asList(
            bar(100.0, 104.0, 99.0, 103.0),
            bar(104.0, 108.0, 103.0, 107.0),
            bar(108.0, 113.0, 107.0, 112.0)
        );
        TrendPrediction prediction = predictor.predict(bars);
        assertTrue("Confidence should be >= 0", prediction.getConfidence() >= 0.0);
        assertTrue("Confidence should be <= 1", prediction.getConfidence() <= 1.0);
    }
}
