package com.jarhax.tcgatagacttagtga;

import org.apache.commons.lang.math.NumberUtils;

public class User {

    private final String name;
    private long points;
    private long workUnits;
    private int containedUsers = 1;

    public User (String name, String[] parts) {

        this(name, parts[0], parts[1]);
    }

    public User (String[] parts) {

        this(parts[0], parts[1], parts[2]);
    }

    public User (String name, String points, String workUnits) {

        this(name, NumberUtils.isNumber(points) ? Long.parseLong(points) : 0, NumberUtils.isNumber(workUnits) ? Long.parseLong(workUnits) : 0);
    }

    public User (String name) {

        this(name, 0, 0);
    }

    public User (String name, long points, long workUnits) {

        this.name = name;
        this.points = points;
        this.workUnits = workUnits;
    }

    public String getName () {

        return this.name;
    }

    public long getPoints () {

        return this.points;
    }

    public long getWorkUnits () {

        return this.workUnits;
    }

    public int getContainedUSers () {

        return this.containedUsers;
    }

    public User merge (User toInclude) {

        this.points += toInclude.getPoints();
        this.workUnits += toInclude.getWorkUnits();
        this.containedUsers++;
        return this;
    }
}