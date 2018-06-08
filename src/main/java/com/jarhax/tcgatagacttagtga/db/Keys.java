/*
 * This file is generated by jOOQ.
*/
package com.jarhax.tcgatagacttagtga.db;


import com.jarhax.tcgatagacttagtga.db.tables.GlobalData;
import com.jarhax.tcgatagacttagtga.db.tables.TeamData;
import com.jarhax.tcgatagacttagtga.db.tables.TeamMembers;
import com.jarhax.tcgatagacttagtga.db.tables.Teams;
import com.jarhax.tcgatagacttagtga.db.tables.UserData;
import com.jarhax.tcgatagacttagtga.db.tables.Users;
import com.jarhax.tcgatagacttagtga.db.tables.UsersFldc;
import com.jarhax.tcgatagacttagtga.db.tables.records.GlobalDataRecord;
import com.jarhax.tcgatagacttagtga.db.tables.records.TeamDataRecord;
import com.jarhax.tcgatagacttagtga.db.tables.records.TeamMembersRecord;
import com.jarhax.tcgatagacttagtga.db.tables.records.TeamsRecord;
import com.jarhax.tcgatagacttagtga.db.tables.records.UserDataRecord;
import com.jarhax.tcgatagacttagtga.db.tables.records.UsersFldcRecord;
import com.jarhax.tcgatagacttagtga.db.tables.records.UsersRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>folding</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<GlobalDataRecord> KEY_GLOBAL_DATA_PRIMARY = UniqueKeys0.KEY_GLOBAL_DATA_PRIMARY;
    public static final UniqueKey<TeamsRecord> KEY_TEAMS_PRIMARY = UniqueKeys0.KEY_TEAMS_PRIMARY;
    public static final UniqueKey<TeamDataRecord> KEY_TEAM_DATA_PRIMARY = UniqueKeys0.KEY_TEAM_DATA_PRIMARY;
    public static final UniqueKey<TeamMembersRecord> KEY_TEAM_MEMBERS_PRIMARY = UniqueKeys0.KEY_TEAM_MEMBERS_PRIMARY;
    public static final UniqueKey<UsersRecord> KEY_USERS_PRIMARY = UniqueKeys0.KEY_USERS_PRIMARY;
    public static final UniqueKey<UsersFldcRecord> KEY_USERS_FLDC_PRIMARY = UniqueKeys0.KEY_USERS_FLDC_PRIMARY;
    public static final UniqueKey<UserDataRecord> KEY_USER_DATA_PRIMARY = UniqueKeys0.KEY_USER_DATA_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<TeamDataRecord, TeamsRecord> FK_87 = ForeignKeys0.FK_87;
    public static final ForeignKey<TeamMembersRecord, TeamsRecord> FK_67 = ForeignKeys0.FK_67;
    public static final ForeignKey<TeamMembersRecord, UsersRecord> FK_95 = ForeignKeys0.FK_95;
    public static final ForeignKey<UsersFldcRecord, UsersRecord> USER_ID = ForeignKeys0.USER_ID;
    public static final ForeignKey<UserDataRecord, UsersRecord> FK_79 = ForeignKeys0.FK_79;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 {
        public static final UniqueKey<GlobalDataRecord> KEY_GLOBAL_DATA_PRIMARY = Internal.createUniqueKey(GlobalData.GLOBAL_DATA, "KEY_global_data_PRIMARY", GlobalData.GLOBAL_DATA.DATE);
        public static final UniqueKey<TeamsRecord> KEY_TEAMS_PRIMARY = Internal.createUniqueKey(Teams.TEAMS, "KEY_teams_PRIMARY", Teams.TEAMS.TEAM_ID);
        public static final UniqueKey<TeamDataRecord> KEY_TEAM_DATA_PRIMARY = Internal.createUniqueKey(TeamData.TEAM_DATA, "KEY_team_data_PRIMARY", TeamData.TEAM_DATA.DATE, TeamData.TEAM_DATA.TEAM_ID);
        public static final UniqueKey<TeamMembersRecord> KEY_TEAM_MEMBERS_PRIMARY = Internal.createUniqueKey(TeamMembers.TEAM_MEMBERS, "KEY_team_members_PRIMARY", TeamMembers.TEAM_MEMBERS.TEAM_MEMBER_ID);
        public static final UniqueKey<UsersRecord> KEY_USERS_PRIMARY = Internal.createUniqueKey(Users.USERS, "KEY_users_PRIMARY", Users.USERS.USER_ID);
        public static final UniqueKey<UsersFldcRecord> KEY_USERS_FLDC_PRIMARY = Internal.createUniqueKey(UsersFldc.USERS_FLDC, "KEY_users_fldc_PRIMARY", UsersFldc.USERS_FLDC.USER_ID);
        public static final UniqueKey<UserDataRecord> KEY_USER_DATA_PRIMARY = Internal.createUniqueKey(UserData.USER_DATA, "KEY_user_data_PRIMARY", UserData.USER_DATA.DATE, UserData.USER_DATA.USER_ID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<TeamDataRecord, TeamsRecord> FK_87 = Internal.createForeignKey(com.jarhax.tcgatagacttagtga.db.Keys.KEY_TEAMS_PRIMARY, TeamData.TEAM_DATA, "FK_87", TeamData.TEAM_DATA.TEAM_ID);
        public static final ForeignKey<TeamMembersRecord, TeamsRecord> FK_67 = Internal.createForeignKey(com.jarhax.tcgatagacttagtga.db.Keys.KEY_TEAMS_PRIMARY, TeamMembers.TEAM_MEMBERS, "FK_67", TeamMembers.TEAM_MEMBERS.TEAM_ID);
        public static final ForeignKey<TeamMembersRecord, UsersRecord> FK_95 = Internal.createForeignKey(com.jarhax.tcgatagacttagtga.db.Keys.KEY_USERS_PRIMARY, TeamMembers.TEAM_MEMBERS, "FK_95", TeamMembers.TEAM_MEMBERS.USER_ID);
        public static final ForeignKey<UsersFldcRecord, UsersRecord> USER_ID = Internal.createForeignKey(com.jarhax.tcgatagacttagtga.db.Keys.KEY_USERS_PRIMARY, UsersFldc.USERS_FLDC, "USER_ID", UsersFldc.USERS_FLDC.USER_ID);
        public static final ForeignKey<UserDataRecord, UsersRecord> FK_79 = Internal.createForeignKey(com.jarhax.tcgatagacttagtga.db.Keys.KEY_USERS_PRIMARY, UserData.USER_DATA, "FK_79", UserData.USER_DATA.USER_ID);
    }
}
