package com.jarhax.tcgatagacttagtga;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger("Stats");

    private static final File DIR_INPUT = new File("input");

    private static final File BZ2_DAILY_TEAMS = new File(DIR_INPUT, "daily_team_summary.txt.bz2");
    private static final File BZ2_DAILY_USERS = new File(DIR_INPUT, "daily_user_summary.txt.bz2");

    private static final File FILE_DAILY_TEAMS = new File(DIR_INPUT, "daily_team_summary.txt");
    private static final File FILE_DAILY_USERS = new File(DIR_INPUT, "daily_user_summary.txt");

    public static final Map<String, Team> TEAM_ENTRIES = new HashMap<>();
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

            downloadBZip2(BZ2_DAILY_TEAMS, FILE_DAILY_TEAMS, "http://fah-web.stanford.edu/daily_team_summary.txt.bz2");
        }

        LOG.info("Starting initial team processing.");

        final long startTime = System.currentTimeMillis();
        int errors = 0;
        int skipped = 0;

        try {

            for (final String line : FileUtils.readLines(FILE_DAILY_TEAMS, StandardCharsets.UTF_8)) {

                final String[] parts = line.split("\\t");

                // Filters out invalid teams
                if (parts.length == 2 && NumberUtils.isDigits(parts[0])) {

                    errors++;
                    LOG.debug("Invalid Team: {}", line);
                }

                // Valid team
                else if (parts.length == 4 && NumberUtils.isNumber(parts[3])) {

                    final long points = Long.parseLong(parts[3]);

                    if (points > 0) {

                        TEAM_ENTRIES.put(parts[0], new Team(parts));
                    }

                    else {

                        LOG.debug("Skipping team {} because they have no points.");
                        skipped++;
                    }
                }
            }
        }

        catch (final IOException e) {

            LOG.trace("Error reading team data.", e);
        }

        LOG.info("Skipped {} teams with 0 points.", skipped);
        LOG.info("Initial team processing has finished. Took {}ms. Found {} teams. {} teams were invalid.", System.currentTimeMillis() - startTime, TEAM_ENTRIES.size(), errors);
    }

    private static void processUsers () {

        if (!FILE_DAILY_USERS.exists()) {

            downloadBZip2(BZ2_DAILY_USERS, FILE_DAILY_USERS, "http://fah-web.stanford.edu/daily_user_summary.txt.bz2");
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

                    if (user.getTotalPoints() <= 0) {

                        skipped++;
                        LOG.debug("Skipping {} on team {} for not having points. P: {} WU: {}", user.getName(), team, user.getTotalPoints(), user.getTotalWorkUnits());
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

    private static void downloadBZip2 (File input, File output, String url) {

        downloadFile(input, url);
        decompress(input, output);
    }

    private static void decompress (File archive, File output) {

        final long startTime = System.currentTimeMillis();
        LOG.info("Decompressing {}.", archive.getName());

        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(archive)); FileOutputStream out = new FileOutputStream(output)) {

            IOUtils.copyLarge(bzIn, out);
        }

        catch (final IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        LOG.info("Decompressed {} to {} in {}ms.", archive.getName(), output.getName(), System.currentTimeMillis() - startTime);
    }

    private static void downloadFile (File downloadLocation, String url) {

        final long startTime = System.currentTimeMillis();
        LOG.info("Starting a download from {}.", url);

        try {

            FileUtils.copyURLToFile(new URL(url), downloadLocation);
        }

        catch (final IOException e) {

            LOG.catching(e);
        }

        LOG.info("Completed download of {} in {}ms from {}.", FileUtils.byteCountToDisplaySize(downloadLocation.length()), System.currentTimeMillis() - startTime, url);
    }
}