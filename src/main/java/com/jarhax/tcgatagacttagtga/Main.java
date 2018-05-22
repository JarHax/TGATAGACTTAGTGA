package com.jarhax.tcgatagacttagtga;

import com.jarhax.tcgatagacttagtga.db.tables.records.UsersRecord;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.*;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.util.mysql.MySQLDataType;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.jarhax.tcgatagacttagtga.db.Tables.*;

public class Main {
    
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
    
    public static final Map<String, Long> NAME_TO_ID = new HashMap<>();
    
    public static Connection connection;
    
    public static DSLContext dslContext;
    
    private static String username;
    private static String password;
    public static final ZoneId ZONE_ID = ZoneId.of("America/Los_Angeles");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static void main(String... main) {
        username = main[0];
        password = main[1];
        LOG.info("Processing has been started.");
        
        final long startTime = System.currentTimeMillis();
        try {
            createDatabaseConnection();
            processTeams();
            processUsers();
            mergeUsers();
            mergeGoogle();
            
            
            insertTeams();
            insertUsers();
            insertTeamMembers();
        } catch(SQLException | ClassNotFoundException e) {
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
        connection = DriverManager.getConnection("jdbc:mysql://blamejared.com:3306/folding", properties);
        dslContext = DSL.using(connection, SQLDialect.MYSQL);
        
        
        LOG.info("Database connection has finished. Took {}ms.", toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    
    private static void insertTeams() {
        LOG.info("Starting insertion of Teams.");
        final long startTime = System.currentTimeMillis();
        
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(TEAMS, TEAMS.TEAM_ID, TEAMS.NAME).values((Integer) null, null).onDuplicateKeyIgnore());
        for(Team team : TEAM_ENTRIES.values()) {
            batch.bind(team.getId(), team.getName());
        }
        batch.execute();
        
        LOG.info("Teams have been inserted. Took {}ms.", toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    private static void insertUsers() {
        LOG.info("Starting insertion of Users.");
        final long startTime = System.currentTimeMillis();
        
        BatchBindStep userBatch = dslContext.batch(dslContext.insertInto(USERS, USERS.USER_ID, USERS.NAME).values((Integer) null, null).onDuplicateKeyIgnore());
        BatchBindStep dataBatch = dslContext.batch(dslContext.insertInto(USER_DATA, USER_DATA.DATE, USER_DATA.USER_ID, USER_DATA.POINTS_TOTAL, USER_DATA.WORK_UNITS_TOTAL).values((Date) null, null, null, null).onDuplicateKeyIgnore());
        
        LocalDate now = LocalDate.now(ZONE_ID);
        String format = DATE_FORMAT.format(now);
        for(User user : MERGED_USERS.values()) {
            Long userID = NAME_TO_ID.get(user.getName());
            userBatch.bind(userID, user.getName());
            dataBatch.bind(format, userID, user.getTotalPoints(), user.getTotalWorkUnits());
            //            String address = "";
            //            String token = "";
            //            //just address as name
            //            if(user.getName().matches("^[13][a-km-zA-HJ-NP-Z0-9]{26,33}$")) {
            //                address = user.getName();
            //                token = "ALL";
            //            }
            //            //name_token_address OR name_address
            //            if(user.getName().matches("^.*_[13][a-km-zA-HJ-NP-Z0-9]{26,33}$")) {
            //                //TODO FLDC users here
            //
            //                String[] split = user.getName().split("_");
            //                if(split.length == 1){
            //                    address = split[0];
            //                    token = "ALL";
            //                }
            //                if(split.length == 2){
            //                    address = split[2];
            //                }
            //            }
            //            if(!address.isEmpty() && !token.isEmpty()){
            //                fldcBatch.bind(format, userID, address, user.getTotalPoints(), token);
            //            }
        }
        userBatch.execute();
        dataBatch.execute();
        
        //        SelectQuery<Record> records = dslContext.selectQuery();
        //        records.addSelect(USER_DATA.DATE, USER_DATA.USER_ID, USER_DATA.POINTS_TOTAL, USERS.NAME);
        //        records.addFrom(USERS.join(USER_DATA).on(USER_DATA.USER_ID.eq(USERS.USER_ID)));
        //        records.addConditions(USERS.NAME.likeRegex("^[13][a-km-zA-HJ-NP-Z0-9]{26,33}$"), USERS.NAME.likeRegex("^.*_[13][a-km-zA-HJ-NP-Z0-9]{26,33}$"));
        //        records.fetch();
        //        BatchBindStep fldcBatch = dslContext.batch(dslContext.insertInto(USERS_FLDC, USERS_FLDC.DATE, USERS_FLDC.USER_ID, USERS_FLDC.ADDRESS, USERS_FLDC.CREDIT_NEW, USERS_FLDC.TOKEN).values((Date) null, null, null, null,null).onDuplicateKeyIgnore());
        //
        //
        //        fldcBatch.execute();
        
        LOG.info("Users have been inserted. Took {}ms.", toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    private static void insertTeamMembers() {
        LOG.info("Starting insertion of Team Members.");
        final long startTime = System.currentTimeMillis();
        
        
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(TEAM_MEMBERS, TEAM_MEMBERS.TEAM_ID, TEAM_MEMBERS.USER_ID).values((Integer) null, null).onDuplicateKeyIgnore());
        Map<Integer, Set<Long>> entries = new HashMap<>();
        for(Team team : TEAM_ENTRIES.values()) {
            Set<Long> set = new HashSet<>();
            for(User user : team.getMembers()) {
                set.add(NAME_TO_ID.get(user.getName()));
            }
            entries.put(team.getId(), set);
        }
        for(Entry<Integer, Set<Long>> entry : entries.entrySet()) {
            Integer teamID = entry.getKey();
            for(Long userID : entry.getValue()) {
                batch.bind(teamID, userID);
            }
        }
        batch.execute();
        
        LOG.info("Team Members have been inserted. Took {}ms.", toHumanTime(System.currentTimeMillis() - startTime));
    }
    
    private static void processTeams() {
        
        if(!FILE_DAILY_TEAMS.exists()) {
            
            downloadBZip2(BZ2_DAILY_TEAMS, FILE_DAILY_TEAMS, "http://fah-web.stanford.edu/daily_team_summary.txt.bz2");
        }
        
        LOG.info("Starting initial team processing.");
        
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
        
        LOG.info("Skipped {} teams with 0 points.", skipped);
        LOG.info("Initial team processing has finished. Took {}ms. Found {} teams. {} teams were invalid.", toHumanTime(System.currentTimeMillis() - startTime), TEAM_ENTRIES.size(), errors);
    }
    
    private static void processUsers() {
        
        if(!FILE_DAILY_USERS.exists()) {
            
            downloadBZip2(BZ2_DAILY_USERS, FILE_DAILY_USERS, "http://fah-web.stanford.edu/daily_user_summary.txt.bz2");
        }
        
        LOG.info("Starting initial user processing.");
        
        final long startTime = System.currentTimeMillis();
        int skipped = 0;
        long userID = dslContext.select(DSL.max(USERS.USER_ID)).from(USERS).fetchOne().value1() + 1;
        try {
            
            List<String> lines = FileUtils.readLines(FILE_DAILY_USERS, StandardCharsets.UTF_8);
            lines.remove(0);
            lines.remove(0);
    
            SelectQuery<Record> query = dslContext.selectQuery();
            query.addSelect(USERS.USER_ID);
            query.addSelect(USERS.NAME);
            query.addFrom(USERS);
            Result<Record> fetch = query.fetch();
            LOG.info("Fetched. Took {}ms. Found {} user entries.", toHumanTime(System.currentTimeMillis() - startTime));
            for(Record record : fetch) {
                int id = record.getValue(USERS.USER_ID);
                String name = record.getValue(USERS.NAME);
                NAME_TO_ID.put(name, (long) id);
            }
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
                    }
    
    
                    if(NAME_TO_ID.putIfAbsent(user.getName(),  userID) == null){
                        userID++;
                    }
                    USER_ENTRIES.add(user);
                }
            }
        } catch(final IOException e) {
            
            LOG.trace("Error reading user data.", e);
        }
        
        LOG.info("Skipped {} user entries that had 0 points.", skipped);
        LOG.info("Initial user processing has finished. Took {}ms. Found {} user entries.", toHumanTime(System.currentTimeMillis() - startTime), USER_ENTRIES.size());
    }
    
    private static void mergeUsers() {
        
        LOG.info("Starting user merge process.");
        
        final long startTime = System.currentTimeMillis();
        final int startingUserCount = USER_ENTRIES.size();
        
        for(final User user : USER_ENTRIES) {
            
            MERGED_USERS.merge(user.getName(), user, (existing, toMerge) -> existing.merge(toMerge));
        }
        
        LOG.info("Users with same name have been merged. Took {}ms. Merged {} users, {} remaining.", toHumanTime(System.currentTimeMillis() - startTime), startingUserCount - MERGED_USERS.size(), MERGED_USERS.size());
    }
    
    private static void mergeGoogle() {
        
        LOG.info("Starting merger of automated google users.");
        
        final long startTime = System.currentTimeMillis();
        final User google = new User("TeamGoogle");
        
        int merged = 0;
        
        for(final Iterator<Map.Entry<String, User>> it = MERGED_USERS.entrySet().iterator(); it.hasNext(); ) {
            
            final Entry<String, User> entry = it.next();
            final String name = entry.getKey();
            
            if(name.startsWith("google") && Character.isDigit(name.charAt(name.length() - 1)) && Character.isDigit(name.charAt(6))) {
                
                google.merge(entry.getValue());
                
                merged++;
                it.remove();
            }
        }
        NAME_TO_ID.putIfAbsent("TeamGoogle", (long) (NAME_TO_ID.size() + 1));
        
        MERGED_USERS.put("TeamGoogle", google);
        
        LOG.info("Finished merging google users. Took {}ms. Merged {} users, {} remaining.", toHumanTime(System.currentTimeMillis() - startTime), merged, MERGED_USERS.size());
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