package com.baeldung.kbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Predicts the next-bar trend using candlestick pattern signals and
 * a simple moving-average (SMA) momentum filter.
 *
 * Confidence is computed from two sources:
 *   - Pattern votes: each detected bullish/bearish pattern contributes a weight.
 *   - SMA momentum: whether the recent close is above or below the SMA.
 */
public class TrendPredictor {

    private static final int DEFAULT_SMA_PERIOD = 5;

    private final KBarAnalyzer analyzer;
    private final int smaPeriod;

    public TrendPredictor() {
        this(DEFAULT_SMA_PERIOD);
    }

    public TrendPredictor(int smaPeriod) {
        if (smaPeriod < 1) throw new IllegalArgumentException("smaPeriod must be >= 1");
        this.smaPeriod = smaPeriod;
        this.analyzer = new KBarAnalyzer();
    }

    /**
     * Predicts the directional bias for the bar following the last entry in {@code bars}.
     *
     * @param bars historical KBar data (at least 1 bar required)
     * @return a TrendPrediction with direction and confidence [0..1]
     */
    public TrendPrediction predict(List<KBar> bars) {
        if (bars == null || bars.isEmpty()) {
            throw new IllegalArgumentException("bars list must not be null or empty");
        }

        List<KBarPattern> patterns = analyzer.analyze(bars);

        double patternScore = scorePatterns(patterns);
        double smaScore     = scoreSma(bars);

        // Weighted blend: patterns 60%, SMA momentum 40%
        double combinedScore = 0.6 * patternScore + 0.4 * smaScore;

        TrendDirection direction;
        double confidence;

        if (combinedScore > 0.0) {
            direction  = TrendDirection.BULLISH;
            confidence = Math.min(1.0, combinedScore);
        } else if (combinedScore < 0.0) {
            direction  = TrendDirection.BEARISH;
            confidence = Math.min(1.0, -combinedScore);
        } else {
            direction  = TrendDirection.NEUTRAL;
            confidence = 0.0;
        }

        return new TrendPrediction(direction, confidence, patterns);
    }

    // -------------------------------------------------------------------------
    // Pattern scoring: returns value in [-1, 1]
    // -------------------------------------------------------------------------

    private double scorePatterns(List<KBarPattern> patterns) {
        double bullish = 0;
        double bearish = 0;

        for (KBarPattern p : patterns) {
            double weight = patternWeight(p);
            switch (p.getSignal()) {
                case BULLISH: bullish += weight; break;
                case BEARISH: bearish += weight; break;
                default: break;
            }
        }

        double total = bullish + bearish;
        if (total == 0) return 0.0;
        return (bullish - bearish) / total;
    }

    private double patternWeight(KBarPattern p) {
        switch (p) {
            // High-reliability 3-bar patterns
            case MORNING_STAR:
            case EVENING_STAR:
            case THREE_WHITE_SOLDIERS:
            case THREE_BLACK_CROWS:
                return 1.0;
            // Solid 2-bar patterns
            case BULLISH_ENGULFING:
            case BEARISH_ENGULFING:
            case THREE_INSIDE_UP:
            case THREE_INSIDE_DOWN:
                return 0.8;
            // Medium-reliability patterns
            case PIERCING_LINE:
            case DARK_CLOUD_COVER:
            case TWEEZER_BOTTOM:
            case TWEEZER_TOP:
                return 0.6;
            // Single-bar patterns
            case MARUBOZU_BULLISH:
            case MARUBOZU_BEARISH:
                return 0.7;
            case HAMMER:
            case INVERTED_HAMMER:
            case SHOOTING_STAR:
            case HANGING_MAN:
                return 0.5;
            case DOJI:
            default:
                return 0.2;
        }
    }

    // -------------------------------------------------------------------------
    // SMA momentum scoring: returns value in [-1, 1]
    // -------------------------------------------------------------------------

    private double scoreSma(List<KBar> bars) {
        if (bars.size() < 2) return 0.0;

        int available = Math.min(smaPeriod, bars.size());
        double sma = 0;
        for (int i = bars.size() - available; i < bars.size(); i++) {
            sma += bars.get(i).getClose();
        }
        sma /= available;

        double lastClose = bars.get(bars.size() - 1).getClose();
        double deviation = (lastClose - sma) / sma;

        // Clamp deviation to [-1, 1] with a sensitivity factor
        double sensitivity = 10.0;
        return Math.max(-1.0, Math.min(1.0, deviation * sensitivity));
    }

    // -------------------------------------------------------------------------
    // Utility: simple moving average over close prices
    // -------------------------------------------------------------------------

    public double computeSma(List<KBar> bars, int period) {
        if (bars.size() < period) throw new IllegalArgumentException("Not enough bars for SMA period " + period);
        double sum = 0;
        for (int i = bars.size() - period; i < bars.size(); i++) {
            sum += bars.get(i).getClose();
        }
        return sum / period;
    }

    /**
     * Computes a list of SMA values over the entire bar series.
     * The first (period-1) entries in the result are null (insufficient data).
     */
    public List<Double> computeSmaList(List<KBar> bars, int period) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < period - 1) {
                result.add(null);
            } else {
                double sum = 0;
                for (int j = i - period + 1; j <= i; j++) {
                    sum += bars.get(j).getClose();
                }
                result.add(sum / period);
            }
        }
        return result;
    }
}
