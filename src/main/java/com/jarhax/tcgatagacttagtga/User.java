package com.jarhax.tcgatagacttagtga;

import org.apache.commons.lang.math.NumberUtils;

import java.util.*;

public class User extends PointEarner {
    
    private int containedUsers = 1;
    
    private int teamId;
    
    private Set<User> containedUsersList = new HashSet<>();
    
    public User(String name, String[] parts) {
        
        this(name, parts[0], parts[1]);
        
    }
    
    public User(String[] parts) {
        
        this(parts[0], parts[1], parts[2]);
    }
    
    public User(String name, String points, String workUnits) {
        
        super(name, NumberUtils.isNumber(points) ? Long.parseLong(points) : 0, NumberUtils.isNumber(workUnits) ? Long.parseLong(workUnits) : 0);
        containedUsersList.add(this);
    }
    
    public User(String name) {
        
        super(name, 0, 0);
        containedUsersList.add(this);
    }
    
    public int getContainedUsers() {
        
        return this.containedUsers;
    }
    
    public int getTeamId() {
        return teamId;
    }
    
    
    public Set<User> getContainedUsersList() {
        return containedUsersList;
    }
    
    
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
    
    public User merge(User toInclude) {
        
        this.addPoints(toInclude.getTotalPoints());
        this.addWorkUnits(toInclude.getTotalWorkUnits());
        this.containedUsers++;
        this.getContainedUsersList().addAll(toInclude.containedUsersList);
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return getContainedUsers() == user.getContainedUsers() && getTeamId() == user.getTeamId();
    }
    
    @Override
    public int hashCode() {
        
        return Objects.hash(getContainedUsers(), getTeamId());
    }
}