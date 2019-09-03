package com.rvr.event.planner.domain;

public enum Place {
    CafeOne(5),
    CafeTwo(1),
    Empty(-1);

    private int priority;

    Place(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
