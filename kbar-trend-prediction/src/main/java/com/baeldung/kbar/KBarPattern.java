package com.baeldung.kbar;

public enum KBarPattern {

    // Single-bar patterns
    DOJI(TrendDirection.NEUTRAL, 1),
    HAMMER(TrendDirection.BULLISH, 1),
    INVERTED_HAMMER(TrendDirection.BULLISH, 1),
    SHOOTING_STAR(TrendDirection.BEARISH, 1),
    HANGING_MAN(TrendDirection.BEARISH, 1),
    MARUBOZU_BULLISH(TrendDirection.BULLISH, 1),
    MARUBOZU_BEARISH(TrendDirection.BEARISH, 1),

    // Two-bar patterns
    BULLISH_ENGULFING(TrendDirection.BULLISH, 2),
    BEARISH_ENGULFING(TrendDirection.BEARISH, 2),
    PIERCING_LINE(TrendDirection.BULLISH, 2),
    DARK_CLOUD_COVER(TrendDirection.BEARISH, 2),
    TWEEZER_BOTTOM(TrendDirection.BULLISH, 2),
    TWEEZER_TOP(TrendDirection.BEARISH, 2),

    // Three-bar patterns
    MORNING_STAR(TrendDirection.BULLISH, 3),
    EVENING_STAR(TrendDirection.BEARISH, 3),
    THREE_WHITE_SOLDIERS(TrendDirection.BULLISH, 3),
    THREE_BLACK_CROWS(TrendDirection.BEARISH, 3),
    THREE_INSIDE_UP(TrendDirection.BULLISH, 3),
    THREE_INSIDE_DOWN(TrendDirection.BEARISH, 3);

    private final TrendDirection signal;
    private final int barsRequired;

    KBarPattern(TrendDirection signal, int barsRequired) {
        this.signal = signal;
        this.barsRequired = barsRequired;
    }

    public TrendDirection getSignal() { return signal; }
    public int getBarsRequired()      { return barsRequired; }
}
