package com.jarhax.tcgatagacttagtga;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

public class Team implements Comparable<Team> {

    private final String id;
    private final String name;
    private final long points;
    private final long workUnits;
    private final List<User> members;

    public Team (String[] parts) {

        this(parts[0], parts[1], parts[2], parts[3]);
    }

    public Team (String id, String name, String points, String workUnits) {

        this.id = id;
        this.name = name;
        this.points = NumberUtils.isNumber(points) ? Long.parseLong(points) : 0;
        this.workUnits = NumberUtils.isNumber(workUnits) ? Long.parseLong(workUnits) : 0;
        this.members = new ArrayList<>();
    }

    public String getId () {

        return this.id;
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

    public List<User> getMembers () {

        return this.members;
    }

    public void addMember (User user) {

        this.members.add(user);
    }

    @Override
    public int compareTo (Team other) {

        return Long.compare(this.points, other.getPoints());
    }
}