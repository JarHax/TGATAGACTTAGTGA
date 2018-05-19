package com.jarhax.tcgatagacttagtga;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

public class Team extends PointEarner {

    private final String id;
    private final List<User> members;

    public Team (String[] parts) {

        this(parts[0], parts[1], parts[2], parts[3]);
    }

    public Team (String id, String name, String points, String workUnits) {

        super(name, NumberUtils.isNumber(points) ? Long.parseLong(points) : 0, NumberUtils.isNumber(workUnits) ? Long.parseLong(workUnits) : 0);

        this.id = id;
        this.members = new ArrayList<>();
    }

    public String getId () {

        return this.id;
    }

    public List<User> getMembers () {

        return this.members;
    }

    public void addMember (User user) {

        this.members.add(user);
    }
}