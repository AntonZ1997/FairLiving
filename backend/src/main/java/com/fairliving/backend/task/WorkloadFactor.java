package com.fairliving.backend.task;

public enum WorkloadFactor {

    COMPLETED_ON_TIME(1.1),
    COMPLETED_LATE(0.9),
    OPEN(1.0);

    private final double factor;

    WorkloadFactor(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
