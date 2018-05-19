package com.jarhax.tcgatagacttagtga;

import org.apache.commons.lang.math.NumberUtils;

public class User extends PointEarner {

    private int containedUsers = 1;

    public User (String name, String[] parts) {

        this(name, parts[0], parts[1]);
    }

    public User (String[] parts) {

        this(parts[0], parts[1], parts[2]);
    }

    public User (String name, String points, String workUnits) {

        super(name, NumberUtils.isNumber(points) ? Long.parseLong(points) : 0, NumberUtils.isNumber(workUnits) ? Long.parseLong(workUnits) : 0);
    }

    public User (String name) {

        super(name, 0, 0);
    }

    public int getContainedUSers () {

        return this.containedUsers;
    }

    public User merge (User toInclude) {

        this.addPoints(toInclude.getTotalPoints());
        this.addWorkUnits(toInclude.getTotalWorkUnits());
        this.containedUsers++;
        return this;
    }
}