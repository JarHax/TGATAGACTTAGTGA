package com.jarhax.tcgatagacttagtga;

public class PointEarner implements IPointEarner, Comparable<PointEarner> {

    private final String name;
    private long points;
    private long workUnits;

    public PointEarner (String name, long points, long workUnits) {

        this.name = name;
        this.points = points;
        this.workUnits = workUnits;
    }

    @Override
    public String getName () {

        return this.name;
    }

    @Override
    public long getTotalPoints () {

        return this.points;
    }

    public void addPoints (long points) {

        this.points += points;
    }

    @Override
    public long getTotalWorkUnits () {

        return this.workUnits;
    }

    public void addWorkUnits (long workUnits) {

        this.workUnits += workUnits;
    }

    @Override
    public int compareTo (PointEarner o) {

        return Long.compare(this.getTotalPoints(), o.getTotalPoints());
    }
}