package com.jarhax.tcgatagacttagtga;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.*;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.*;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static com.jarhax.tcgatagacttagtga.db.Tables.*;

public class NewMain {
    
    private static final Logger LOG = LogManager.getLogger("Stats");
    
    private static final Pattern TAB_SEPERATION_PATTERN = Pattern.compile("\\t");
    private static final File DIR_INPUT = new File("input");
    
    private static final File BZ2_DAILY_TEAMS = new File(DIR_INPUT, "daily_team_summary.txt.bz2");
    private static final File BZ2_DAILY_USERS = new File(DIR_INPUT, "daily_user_summary.txt.bz2");
    
    private static final File FILE_DAILY_TEAMS = new File(DIR_INPUT, "daily_team_summary.txt");
    private static final File FILE_DAILY_USERS = new File(DIR_INPUT, "daily_user_summary.txt");
    
    public static final Map<String, Team> TEAM_ENTRIES = new HashMap<>();
    public static final List<User> USER_ENTRIES = new ArrayList<>();
    public static final Map<String, User> MERGED_USERS = new HashMap<>();
    
    public static Connection connection;
    
    public static DSLContext dslContext;
    
    private static String username;
    private static String password;
    public static final ZoneId ZONE_ID = ZoneId.of("America/Los_Angeles");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static Timestamp timestamp;
    private static PreparedStatement psEqual;
    
    public static void main(String... main) {
        username = main[0];
        password = main[1];
        LOG.info("Processing has been started.");
        final long startTime = System.currentTimeMillis();
        LocalDateTime date = LocalDateTime.now(ZONE_ID);
        timestamp = Timestamp.valueOf(date);
        try {
            createDatabaseConnection();
            startFileDownload(BZ2_DAILY_USERS, FILE_DAILY_USERS, "http://fah-web.stanford.edu/daily_user_summary.txt.bz2");
            startFileDownload(BZ2_DAILY_TEAMS, FILE_DAILY_TEAMS, "http://fah-web.stanford.edu/daily_team_summary.txt.bz2");
            startTeamInsertion();
            startUserInsertion();
            startTeamDataInsertion();
            
            //            startTeamMemberInsertion();
            //                        startUserInsertion();
            //            startTeamMemberInsertion();
        } catch(Exception e) {
            e.printStackTrace();
            
        } finally {
            try {
                connection.close();
            } catch(SQLException e1) {
                e1.printStackTrace();
            }
        }
        
        LOG.info("Processing has ended. Total time took {}ms.", toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    
    private static void createDatabaseConnection() throws ClassNotFoundException, SQLException {
        LOG.info("Creating database connection");
        final long startTime = System.currentTimeMillis();
        Class.forName("com.mysql.jdbc.Driver");
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("rewriteBatchedStatements", "true");
        connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/folding", properties);
        dslContext = DSL.using(connection, SQLDialect.MYSQL);
        dslContext.execute("SET GLOBAL max_allowed_packet=1073741824;");
        //TODO if the schema doesn't exist, create it.
        
        LOG.info("Database connection has finished. Took {}ms.", toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    
    private static void startFileDownload(File zipped, File output, String url) throws IOException {
        LOG.info("Starting file download for: {} from: {}", output.getName(), url);
        final long startTime = System.currentTimeMillis();
        downloadBZip2(zipped, output, url);
        LOG.info("Finished file download for {} from {}. Took {}ms.", output.getName(), url, toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    
    private static void startTeamDataInsertion() {
        //TODO so users_fldc works on merged users, teamdata does not, so I'm not sure if the fldc counted should be counted.
        LOG.info("Starting team data insertion");
        final long startTime = System.currentTimeMillis();
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(TEAM_DATA).values((Timestamp) null, null, null, null, null));
        for(Team team : TEAM_ENTRIES.values()) {
            int fldcFolders = 0;
            Record1<Integer> one = dslContext.select(DSL.count(USERS_FLDC.USER_ID)).from(USERS_FLDC.join(TEAM_MEMBERS).on(USERS_FLDC.USER_ID.eq(TEAM_MEMBERS.USER_ID))).where(TEAM_MEMBERS.TEAM_ID.eq(team.getId())).fetchOne();
            if(one != null) {
                fldcFolders = one.value1();
            }
            batch.bind(timestamp, team.getId(), team.getTotalPoints(), team.getTotalWorkUnits(), fldcFolders);
        }
        if(batch.size() > 0) {
            batch.execute();
        }
        LOG.info("Finished team data insertion. Took {}ms. Inserted {} new teamdata entries.", toHumanTime(System.currentTimeMillis() - startTime), batch.size());
    }
    
    private static void startTeamInsertion() {
        LOG.info("Starting team insertion");
        final long startTime = System.currentTimeMillis();
        int errors = 0;
        int skipped = 0;
        try {
            List<String> lines = FileUtils.readLines(FILE_DAILY_TEAMS, StandardCharsets.UTF_8);
            lines.remove(0);
            lines.remove(0);
            
            for(final String line : lines) {
                
                final String[] parts = TAB_SEPERATION_PATTERN.split(line);
                
                // Filters out invalid teams
                if(parts.length == 2 && NumberUtils.isDigits(parts[0])) {
                    
                    errors++;
                    LOG.debug("Invalid Team: {}", line);
                }
                
                // Valid team
                else if(parts.length == 4 && NumberUtils.isNumber(parts[3])) {
                    
                    final long points = Long.parseLong(parts[3]);
                    
                    if(points > 0) {
                        
                        TEAM_ENTRIES.put(parts[0], new Team(parts));
                    } else {
                        
                        LOG.debug("Skipping team {} because they have no points.");
                        skipped++;
                    }
                }
            }
        } catch(final IOException e) {
            LOG.trace("Error reading team data.", e);
        }
        Map<Integer, String> teamMap = dslContext.select(TEAMS.TEAM_ID, TEAMS.NAME).from(TEAMS).fetchMap(TEAMS.TEAM_ID, TEAMS.NAME);
        
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(TEAMS).values((Integer) null, null));
        for(Team team : TEAM_ENTRIES.values()) {
            
            if(teamMap.containsKey(team.getId())) {
                continue;
            }
            String name = team.getName();
            batch.bind(team.getId(), name);
        }
        int[] execute = new int[]{};
        if(batch.size() > 0)
            execute = batch.execute();
        int inserts = execute.length;
        LOG.info("Finished team insertion. Took {}ms. Found {} teams. {} new teams were inserted. {} teams were invalid. {} teams were skipped.", toHumanTime(System.currentTimeMillis() - startTime), TEAM_ENTRIES.size(), inserts, errors, skipped);
    }
    
    
    private static void startUserInsertion() {
        LOG.info("Starting user insertion");
        final long startTime = System.currentTimeMillis();
        int skipped = 0;
        try {
            
            List<String> lines = FileUtils.readLines(FILE_DAILY_USERS, StandardCharsets.UTF_8);
            lines.remove(0);
            lines.remove(0);
            
            for(final String line : lines) {
                
                User user = null;
                String team = "0";
                final String[] parts = TAB_SEPERATION_PATTERN.split(line);
                
                if(parts.length == 3 && NumberUtils.isDigits(parts[0])) {
                    
                    user = new User("anonymous", parts);
                    team = parts[2];
                } else if(parts.length == 4) {
                    
                    user = new User(parts);
                    team = parts[3];
                }
                
                if(user != null) {
                    
                    if(user.getTotalPoints() <= 0) {
                        
                        skipped++;
                        LOG.debug("Skipping {} on team {} for not having points. P: {} WU: {}", user.getName(), team, user.getTotalPoints(), user.getTotalWorkUnits());
                        continue;
                    }
                    
                    final Team teamObj = TEAM_ENTRIES.get(team);
                    
                    if(teamObj != null) {
                        
                        teamObj.addMember(user);
                        user.setTeamId(teamObj.getId());
                    }
                    
                    
                    USER_ENTRIES.add(user);
                }
            }
        } catch(final IOException e) {
            
            LOG.trace("Error reading user data.", e);
        }
        
        mergeUsers();
        
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(USERS).values((Integer) null, null));
        BatchBindStep tmBatch = dslContext.batch(dslContext.insertInto(TEAM_MEMBERS).values((Integer) null, null, null));
        BatchBindStep fldcBatch = dslContext.batch(dslContext.insertInto(USERS_FLDC).values((Integer) null, null, null, null));
        
        Result<Record3<Integer, Integer, Integer>> tmFetch = dslContext.select(TEAM_MEMBERS.TEAM_MEMBER_ID, TEAM_MEMBERS.TEAM_ID, TEAM_MEMBERS.USER_ID).from(TEAM_MEMBERS).fetch();
        
        List<Pair<Integer, Integer>> tmEntries = new ArrayList<>();
        for(Record3<Integer, Integer, Integer> fetch : tmFetch) {
            tmEntries.add(new Pair<>(fetch.value2(), fetch.value3()));
        }
        Result<Record1<String>> fetch = dslContext.select(USERS.NAME).from(USERS).fetch();
        List<String> userNames = new ArrayList<>();
        for(Record1<String> record1 : fetch) {
            userNames.add(record1.value1());
        }
        Record1<Integer> userIdOld = dslContext.select(DSL.max(USERS.USER_ID)).from(USERS).fetchOne();
        int userId = userIdOld.value1() != null ? userIdOld.value1() + 1 : 1;
        Map<String, User> tempMerged = new LinkedHashMap<>(MERGED_USERS);
        for(String name : userNames) {
            tempMerged.remove(name);
        }
        Record1<Integer> tmIdOld = dslContext.select(DSL.max(TEAM_MEMBERS.TEAM_MEMBER_ID)).from(TEAM_MEMBERS).fetchOne();
        int tmId = tmIdOld.value1() != null ? tmIdOld.value1() + 1 : 1;
        //TODO this may mess up... if an old user starts folding on a new team, then they wouldn't be included here...
        for(User user : tempMerged.values()) {
            String name = user.getName();
            boolean alreadyExists = false;
            int tempId = 0;
            if(fetch.size() > 0) { //no need to run this unless there are actually values in the database
                for(Record2<Integer, String> record2 : dslContext.select(USERS.USER_ID, USERS.NAME).from(USERS).where(USERS.NAME.eq(name)).fetch()) {
                    if(sqlEq(record2.value2(), name)) {
                        alreadyExists = true;
                        tempId = record2.value1();
                        break;
                        //                        continue outer;
                    }
                }
            }
            if(!alreadyExists) {
                tempId = userId++;
                batch.bind(tempId, name);
            }
            //TODO somehow, in here, a user is being added again, userId = 66, teamid = 184157, the user is treated as "?" in mysql, so I suspect it is encoding, honestly not sure how to deal with this
            for(User user1 : user.getContainedUsersList()) {
                if(!tmEntries.contains(new Pair<>(user1.getTeamId(), tempId))) {
                    tmBatch.bind(tmId++, user1.getTeamId(), tempId);
                }
            }
            
            //LKU1Q1zVXDf7kKHu5N8VAh1rKFUgQs55VR this passes the regex
            //LKiC1THQK9QKc6ycBUHPYvpqxrKkt3JxZN
            //TODO users_fldc should *maybe* be changed, so it doesn't have the credit, it is storing it twice in theory
            if(user.getName().matches(".*[13][a-km-zA-HJ-NP-Z0-9]{26,33}$")) {
                String address = "";
                long totalPoints = user.getTotalPoints();
                String token = "";
                if(user.getName().matches("^[13][a-km-zA-HJ-NP-Z0-9]{26,33}$")) {
                    address = user.getName();
                    if(address.length() > 34) {
                        System.out.println("1 " + address);
                    }
                    token = "ALL";
                } else if(user.getName().matches("^.*_[13][a-km-zA-HJ-NP-Z0-9]{26,33}$")) {
                    address = user.getName().substring(user.getName().lastIndexOf("_") + 1);
                    if(address.length() > 34) {
                        System.out.println("2 " + address);
                    }
                    if(user.getName().indexOf("_") == user.getName().lastIndexOf("_")) {
                        token = "ALL";
                    } else {
                        String s = user.getName();
                        s = s.substring(0, s.lastIndexOf("_"));
                        s = s.substring(s.lastIndexOf("_") + 1, s.length());
                        token = s;
                    }
                }
                
                if(!address.isEmpty() && !token.isEmpty())
                    fldcBatch.bind(tempId, address.trim(), totalPoints, token.trim());
            }
        }
        
        if(batch.size() > 0)
            batch.execute();
        if(tmBatch.size() > 0)
            tmBatch.execute();
        if(fldcBatch.size() > 0)
            fldcBatch.execute();
        
        LOG.info("Skipped {} user entries that had 0 points.", skipped);
        LOG.info("Finished user insertion. Took {}ms. {} new users inserted. {} team members inserted. {} fldc users inserted. Found {} user entries. Merged {} user entries.", toHumanTime(System.currentTimeMillis() - startTime), batch.size(), tmBatch.size(), fldcBatch.size(), USER_ENTRIES.size(), MERGED_USERS.size());
    }
    
    public static boolean sqlEq(String one, String two) {
        try {
            if(psEqual == null)
                psEqual = connection.prepareStatement("SELECT ? = ?");
            psEqual.clearBatch();
            psEqual.setString(1, one);
            psEqual.setString(2, two);
            
            ResultSet set = psEqual.executeQuery();
            set.next();
            return set.getBoolean(1);
        } catch(SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private static void mergeUsers() {
        
        LOG.info("Starting user merge process.");
        
        final long startTime = System.currentTimeMillis();
        final int startingUserCount = USER_ENTRIES.size();
        
        for(final User user : USER_ENTRIES) {
            
            MERGED_USERS.merge(user.getName(), user, User::merge);
        }
        
        LOG.info("Users with same name have been merged. Took {}ms. Merged {} users, {} remaining.", toHumanTime(System.currentTimeMillis() - startTime), startingUserCount - MERGED_USERS.size(), MERGED_USERS.size());
    }
    
    private static void downloadBZip2(File input, File output, String url) {
        
        downloadFile(input, url);
        decompress(input, output);
    }
    
    private static void decompress(File archive, File output) {
        
        final long startTime = System.currentTimeMillis();
        LOG.info("Decompressing {}.", archive.getName());
        
        try(BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(archive)); FileOutputStream out = new FileOutputStream(output)) {
            
            IOUtils.copyLarge(bzIn, out);
        } catch(final IOException e1) {
            e1.printStackTrace();
        }
        
        LOG.info("Decompressed {} to {} in {}ms.", archive.getName(), output.getName(), toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    private static void downloadFile(File downloadLocation, String url) {
        
        final long startTime = System.currentTimeMillis();
        LOG.info("Starting a download from {}.", url);
        
        try {
            
            FileUtils.copyURLToFile(new URL(url), downloadLocation);
        } catch(final IOException e) {
            
            LOG.catching(e);
        }
        
        LOG.info("Completed download of {} in {}ms from {}.", FileUtils.byteCountToDisplaySize(downloadLocation.length()), toHumanTime(System.currentTimeMillis() - startTime), url);
    }
    
    public static String toHumanTime(long millis) {
        
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        
        return String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
    }
}