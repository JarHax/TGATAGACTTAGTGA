package com.jarhax.tcgatagacttagtga;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger("Stats");

    private static final File DIR_INPUT = new File("input");
    private static final File DIR_OUTPUT = new File("output");
    private static final File DIR_OUTPUT_TEAMS = new File(DIR_OUTPUT, "teams");
    private static final File DIR_OUTPUT_USERS = new File(DIR_OUTPUT, "users");

    private static final File FILE_DAILY_TEAMS = new File(DIR_INPUT, "daily_team_summary.txt");
    private static final File FILE_DAILY_USERS = new File(DIR_INPUT, "daily_user_summary.txt");

    public static final Map<String, Team> TEAM_ENTRIES = new HashMap<>();
    public static final Map<String, Integer> USER_IDS = new HashMap<>();
    public static final List<User> USER_ENTRIES = new ArrayList<>();
    public static final Map<String, User> MERGED_USERS = new HashMap<>();

    public static void main (String... main) {

        LOG.info("Processing has been started.");

        final long startTime = System.currentTimeMillis();

        processTeams();
        processUsers();
        mergeUsers();
        mergeGoogle();

        LOG.info("Processing has ended. Total time took {}ms.", System.currentTimeMillis() - startTime);
    }

    private static void processTeams () {

        if (!FILE_DAILY_TEAMS.exists()) {

            // TODO do the download now.
            throw new RuntimeException("Could not find find team data file!");
        }

        LOG.info("Starting initial team processing.");

        final long startTime = System.currentTimeMillis();
        final Set<String> errors = new HashSet<>();

        try {

            for (final String line : FileUtils.readLines(FILE_DAILY_TEAMS, StandardCharsets.UTF_8)) {

                final String[] parts = line.split("\\t");

                // Filters out invalid teams
                if (parts.length == 2 && NumberUtils.isDigits(parts[0])) {

                    errors.add(parts[0]);
                    LOG.debug("Invalid Team: {}", line);
                }

                // Valid team
                else if (parts.length == 4) {

                    TEAM_ENTRIES.put(parts[0], new Team(parts));
                }
            }
        }

        catch (final IOException e) {

            LOG.trace("Error reading team data.", e);
        }

        LOG.info("Initial team processing has finished. Took {}ms. Found {} teams. {} teams were invalid.", System.currentTimeMillis() - startTime, TEAM_ENTRIES.size(), errors.size());
    }

    private static void processUsers () {

        if (!FILE_DAILY_USERS.exists()) {

            // TODO download
            throw new RuntimeException("Could not find user data file!");
        }

        LOG.info("Starting initial user processing.");

        final long startTime = System.currentTimeMillis();
        int skipped = 0;

        try {

            for (final String line : FileUtils.readLines(FILE_DAILY_USERS, StandardCharsets.UTF_8)) {

                User user = null;
                String team = "0";
                final String[] parts = line.split("\\t");

                if (parts.length == 3 && NumberUtils.isDigits(parts[0])) {

                    user = new User("anonymous", parts);
                    team = parts[2];
                }

                else if (parts.length == 4) {

                    user = new User(parts);
                    team = parts[3];
                }

                if (user != null) {

                    if (user.getWorkUnits() <= 0) {

                        skipped++;
                        LOG.debug("Skipping {} on team {} for not having points. P: {} WU: {}", user.getName(), team, user.getPoints(), user.getWorkUnits());
                        continue;
                    }

                    final Team teamObj = TEAM_ENTRIES.get(team);

                    if (teamObj != null) {

                        teamObj.addMember(user);
                    }

                    USER_ENTRIES.add(user);
                }
            }
        }

        catch (final IOException e) {

            LOG.trace("Error reading user data.", e);
        }

        LOG.info("Skipped {} user entries that had 0 points.", skipped);
        LOG.info("Initial user processing has finished. Took {}ms. Found {} user entries.", System.currentTimeMillis() - startTime, USER_ENTRIES.size());
    }

    private static void mergeUsers () {

        LOG.info("Starting user merge process.");

        final long startTime = System.currentTimeMillis();
        final int startingUserCount = USER_ENTRIES.size();

        for (final User user : USER_ENTRIES) {

            MERGED_USERS.merge(user.getName(), user, (existing, toMerge) -> existing.merge(toMerge));
        }

        LOG.info("Users with same name have been merged. Took {}ms. Merged {} users, {} remaining.", System.currentTimeMillis() - startTime, startingUserCount - MERGED_USERS.size(), MERGED_USERS.size());
    }

    private static void mergeGoogle () {

        LOG.info("Starting merger of automated google users.");

        final long startTime = System.currentTimeMillis();
        final User google = new User("TeamGoogle");

        int merged = 0;

        for (final Iterator<Map.Entry<String, User>> it = MERGED_USERS.entrySet().iterator(); it.hasNext();) {

            final Entry<String, User> entry = it.next();
            final String name = entry.getKey();

            if (name.startsWith("google") && Character.isDigit(name.charAt(name.length() - 1)) && Character.isDigit(name.charAt(6))) {

                google.merge(entry.getValue());

                merged++;
                it.remove();
            }
        }

        MERGED_USERS.put("TeamGoogle", google);

        LOG.info("Finished merging google users. Took {}ms. Merged {} users, {} remaining.", System.currentTimeMillis() - startTime, merged, MERGED_USERS.size());
    }
}