package com.baeldung.kbar;

import java.util.Collections;
import java.util.List;

public class TrendPrediction {

    private final TrendDirection direction;
    private final double confidence;
    private final List<KBarPattern> triggeringPatterns;

    public TrendPrediction(TrendDirection direction, double confidence, List<KBarPattern> triggeringPatterns) {
        this.direction = direction;
        this.confidence = confidence;
        this.triggeringPatterns = Collections.unmodifiableList(triggeringPatterns);
    }

    public TrendDirection getDirection()               { return direction; }
    public double getConfidence()                      { return confidence; }
    public List<KBarPattern> getTriggeringPatterns()   { return triggeringPatterns; }

    @Override
    public String toString() {
        return String.format("TrendPrediction[direction=%s, confidence=%.2f, patterns=%s]",
            direction, confidence, triggeringPatterns);
    }
}
