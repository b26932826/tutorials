package com.baeldung.kbar;

import java.time.LocalDateTime;

public class KBar {

    private final LocalDateTime timestamp;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    public KBar(LocalDateTime timestamp, double open, double high, double low, double close, long volume) {
        if (high < low) throw new IllegalArgumentException("high must be >= low");
        if (high < open || high < close) throw new IllegalArgumentException("high must be the session high");
        if (low > open || low > close) throw new IllegalArgumentException("low must be the session low");
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public double bodySize() {
        return Math.abs(close - open);
    }

    public double upperWick() {
        return high - Math.max(open, close);
    }

    public double lowerWick() {
        return Math.min(open, close) - low;
    }

    public double totalRange() {
        return high - low;
    }

    public boolean isBullish() {
        return close > open;
    }

    public boolean isBearish() {
        return close < open;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public double getOpen()  { return open; }
    public double getHigh()  { return high; }
    public double getLow()   { return low; }
    public double getClose() { return close; }
    public long   getVolume(){ return volume; }

    @Override
    public String toString() {
        return String.format("KBar[%s O=%.2f H=%.2f L=%.2f C=%.2f V=%d]",
            timestamp, open, high, low, close, volume);
    }
}
