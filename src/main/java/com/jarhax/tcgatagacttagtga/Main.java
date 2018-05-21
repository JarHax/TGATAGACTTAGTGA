package com.jarhax.tcgatagacttagtga;

import com.google.common.collect.Lists;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.*;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.regex.Pattern;

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
    
    public static final Map<String, Integer> NAME_TO_ID = new HashMap<>();
    
    public static Connection connection;
    
    private static String username;
    private static String password;
    
    public static void main(String... main) {
        username = main[0];
        password = main[1];
        LOG.info("Processing has been started.");
        
        final long startTime = System.currentTimeMillis();
        
        processTeams();
        processUsers();
        mergeUsers();
        mergeGoogle();
        try {
            createDatabaseConnection();
            insertTeams();
            insertUsers();
            //            insertTeamMembers();
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            
        } finally {
            try {
                connection.close();
            } catch(SQLException e1) {
                e1.printStackTrace();
            }
        }
        
        LOG.info("Processing has ended. Total time took {}ms.", System.currentTimeMillis() - startTime);
    }
    
    private static void createDatabaseConnection() throws ClassNotFoundException, SQLException {
        LOG.info("Creating database connection");
        final long startTime = System.currentTimeMillis();
        Class.forName("com.mysql.jdbc.Driver");
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("rewriteBatchedStatements", "true");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/folding", properties);
        //doesn't always update on the current connection
        connection.createStatement().execute("SET GLOBAL max_allowed_packet=1073741825");
        connection.setAutoCommit(false);
        //TODO this is debug code, remove for production
        //        connection.createStatement().executeUpdate("TRUNCATE TABLE team_members");
        //        connection.createStatement().executeUpdate("TRUNCATE TABLE teams");
        //        connection.createStatement().executeUpdate("TRUNCATE TABLE users");
        //        connection.commit();
        
        
        LOG.info("Database connection has finished. Took {}ms.", System.currentTimeMillis() - startTime);
    }
    
    
    private static void insertTeams() throws SQLException {
        LOG.info("Starting insertion of Teams.");
        final long startTime = System.currentTimeMillis();
        
        int i = 1;
        List<List<Team>> lists = Lists.partition(new ArrayList<>(TEAM_ENTRIES.values()), TEAM_ENTRIES.size() / 10);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO folding.teams VALUES" + repeat("(?,?)", 1));
        for(List<Team> teams : lists) {
            
            for(Team team : teams) {
                statement.setLong(1, team.getId());
                statement.setString(2, team.getName());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.clearBatch();
            LOG.info("executed teams " + i++ + " / " + lists.size());
        }
        connection.commit();
        LOG.info("Teams have been inserted. Took {}ms.", System.currentTimeMillis() - startTime);
    }
    
    private static void insertUsers() throws SQLException {
        LOG.info("Starting insertion of Users.");
        final long startTime = System.currentTimeMillis();
    
        final int[] i = {1};
        
        List<List<User>> lists = Lists.partition(new ArrayList<>(MERGED_USERS.values()), MERGED_USERS.size() / 10);
        
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for(List<User> users : lists) {
            
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        PreparedStatement statement = connection.prepareStatement("INSERT INTO folding.users VALUES" + repeat("(?,?)", 1));
                        for(User user : users) {
                            statement.setInt(1, NAME_TO_ID.get(user.getName()));
                            statement.setString(2, user.getName());
                            statement.addBatch();
                            
                        }
                        statement.executeBatch();
                        statement.clearBatch();
                        LOG.info("executed " + users.size() + " users " + i[0]++ + " / " + lists.size());
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                    
                }
            });
            
            
            
        }
        
        pool.shutdown();
        try {
            pool.awaitTermination(100000, TimeUnit.MILLISECONDS);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        //        while(!pool.isTerminated()) {
        //
        //        }
        connection.commit();
        LOG.info("Users have been inserted. Took {}ms.", System.currentTimeMillis() - startTime);
    }
    
    private static void insertTeamMembers() throws SQLException {
        //TODO commit
        LOG.info("Starting insertion of Team Members.");
        final long startTime = System.currentTimeMillis();
        
        int amount = 0;
        for(Team team : TEAM_ENTRIES.values()) {
            amount += team.getMembers().size();
        }
        PreparedStatement statement = connection.prepareStatement("INSERT INTO folding.team_members VALUES" + repeat("(?,?)", 1));
        int i = 1;
        for(Team team : TEAM_ENTRIES.values()) {
            long teamID = team.getId();
            for(User user : team.getMembers()) {
                statement.setLong(1, teamID);
                statement.setLong(2, NAME_TO_ID.get(user.getName()));
                statement.executeUpdate();
            }
        }
        LOG.info("Batched team members");
        
        
        LOG.info("Team Members have been inserted. Took {}ms.", System.currentTimeMillis() - startTime);
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
        LOG.info("Initial team processing has finished. Took {}ms. Found {} teams. {} teams were invalid.", System.currentTimeMillis() - startTime, TEAM_ENTRIES.size(), errors);
    }
    
    private static void processUsers() {
        
        if(!FILE_DAILY_USERS.exists()) {
            
            downloadBZip2(BZ2_DAILY_USERS, FILE_DAILY_USERS, "http://fah-web.stanford.edu/daily_user_summary.txt.bz2");
        }
        
        LOG.info("Starting initial user processing.");
        
        final long startTime = System.currentTimeMillis();
        int skipped = 0;
        int userID = 0;
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
                    }
                    NAME_TO_ID.putIfAbsent(user.getName(), userID++);
                    USER_ENTRIES.add(user);
                }
            }
        } catch(final IOException e) {
            
            LOG.trace("Error reading user data.", e);
        }
        
        LOG.info("Skipped {} user entries that had 0 points.", skipped);
        LOG.info("Initial user processing has finished. Took {}ms. Found {} user entries.", System.currentTimeMillis() - startTime, USER_ENTRIES.size());
    }
    
    private static void mergeUsers() {
        
        LOG.info("Starting user merge process.");
        
        final long startTime = System.currentTimeMillis();
        final int startingUserCount = USER_ENTRIES.size();
        
        for(final User user : USER_ENTRIES) {
            
            MERGED_USERS.merge(user.getName(), user, (existing, toMerge) -> existing.merge(toMerge));
        }
        
        LOG.info("Users with same name have been merged. Took {}ms. Merged {} users, {} remaining.", System.currentTimeMillis() - startTime, startingUserCount - MERGED_USERS.size(), MERGED_USERS.size());
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
        NAME_TO_ID.putIfAbsent("TeamGoogle", NAME_TO_ID.size() + 1);
        
        MERGED_USERS.put("TeamGoogle", google);
        
        LOG.info("Finished merging google users. Took {}ms. Merged {} users, {} remaining.", System.currentTimeMillis() - startTime, merged, MERGED_USERS.size());
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
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        LOG.info("Decompressed {} to {} in {}ms.", archive.getName(), output.getName(), System.currentTimeMillis() - startTime);
    }
    
    private static void downloadFile(File downloadLocation, String url) {
        
        final long startTime = System.currentTimeMillis();
        LOG.info("Starting a download from {}.", url);
        
        try {
            
            FileUtils.copyURLToFile(new URL(url), downloadLocation);
        } catch(final IOException e) {
            
            LOG.catching(e);
        }
        
        LOG.info("Completed download of {} in {}ms from {}.", FileUtils.byteCountToDisplaySize(downloadLocation.length()), System.currentTimeMillis() - startTime, url);
    }
    
    private static String repeat(String str, int amount) {
        
        String[] arr = new String[amount];
        Arrays.fill(arr, str);
        return String.join(",", arr);
    }
    
    
}