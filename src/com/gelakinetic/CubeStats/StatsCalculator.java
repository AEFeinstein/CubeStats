/**
 * Copyright 2016 Adam Feinstein
 * 
 * This file is part of CubeStats.
 * 
 * CubeStats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CubeStats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with CubeStats.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gelakinetic.CubeStats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StatsCalculator {

    /** The size of the desired cube */
    private static final int CUBE_SIZE = 480;
    
    /** All the colors. The first entry to each array is just a label */
    private static final String[][] COLORS = {
            {"White", "W", "AW"},
            {"Blue", "U", "AU"},
            {"Black", "B", "AB"},
            {"Red", "R", "AR"},
            {"Green", "G", "AG"},
            {"Colorless", "A", "C"},
            {"Land", "L", "LG", "AL"},
            {"Gold", "WU", "BR", "UB", "WG", "RG", "WR", "UG", "BG", "UR",
                "WB", "UBR", "BRG", "WUG", "WUB", "WRG", "WBR", "UBG", "URG",
                "WUR", "WBG", "AUB", "AWB", "AWU", "ABR", "ARG", "AWG", "AUR",
                "AUG", "AWUB", "AUBR", "WBRG", "UBRG", "WURG", "WUBG", "WUBR",
                "WUBRG", "AWUBRG"}
    };
    
    /** All the types. They are in distinct buckets (i.e. Artifact Lands don't
     *  count as Artifacts */
    private static final String[][] TYPES = {
            {"Land", "Artifact Land", "Land Creature", "Basic Land",
                "Legendary Land", "Basic Snow Land", "Legendary Snow Land",
                "Snow Land"},
            {"Creature", "Artifact Creature", "Legendary Artifact Creature",
                    "Legendary Creature", "Snow Artifact Creature",
                    "Snow Creature", "Enchantment Creature",
                    "Legendary Enchantment Creature"},
            {"Artifact", "Snow Artifact", "Legendary Artifact",
                        "Tribal Artifact", "Legendary Enchantment Artifact"},
            {"Enchantment", "Legendary Enchantment", "Tribal Enchantment",
                            "Snow Enchantment"},
            {"Instant", "Tribal Instant"},
            {"Sorcery", "Tribal Sorcery"},
            {"Planeswalker"}
    };
    
    /** All the converted mana costs */
    private static final int CONVERTED_MANA_COSTS[] = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };
    
    /** All the rarities, with their probability of occurrence */
    private static final Rarity RARITIES[] = {
            new Rarity('C', 88), /* Common      11  per pack */
            new Rarity('U', 24), /* Uncommon    3   per pack */
            new Rarity('R', 8),  /* Rare        1   per pack */
            new Rarity('M', 1),  /* Mythic      1/8 per pack */
            new Rarity('T', 8)   /* Timeshifted 1   per pack */
    };
    
    /**
     * Given the database of Magic cards used by MTG Familiar and a cube size,
     * figure out how many of each card color, type, and converted mana cost
     * (for creatures) are necessary
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        Connection dbConnection = null;
        
        try {
            int totalSize = 0;
            ArrayList<CubeStats> allStats = new ArrayList<>();

            /* Open up the database */
            Class.forName("org.sqlite.JDBC");
            dbConnection = DriverManager.getConnection(
                "jdbc:sqlite:C:\\Users\\Adam\\workspace\\CubeStats\\mtg.db");                        
            
            /* For all the colors */
            for (String[] color : COLORS) {
                System.out.println("Processing " + color[0]);
                
                /* For all the card types */
                for(String[] type : TYPES) {
                    System.out.println("\t" + type[0]);
                    
                    /* If the type is Creature */
                    if(type[0].equals("Creature")) {
                        
                        /* For all the mana costs */
                        for(int cmc : CONVERTED_MANA_COSTS) {
                            
                            /* Do the query and store the result */
                            System.out.println("\t\t" + cmc);
                            int count = getScaledQueryCount(dbConnection,
                                    color, type, cmc);
                            allStats.add(new CubeStats(color[0], type[0], cmc,
                                    count));

                            /* Keep a count of all scaled query counts */
                            totalSize += count;
                        }
                    }
                    else {
                        
                        /* For non-creatures, don't worry about converted mana
                         * cost. Do the query and store the result */
                        int count = getScaledQueryCount(dbConnection, color,
                                type, -1);
                        allStats.add(new CubeStats(color[0], type[0], -1,
                                count));

                        /* Keep a count of all scaled query counts */
                        totalSize += count;
                    }
                }
            }
            
            /* Now that we know the count for each color/type/cmc, and the total
             * number of returned rows, scale them to the desired cube size
             */
            int totalCubeCount = 0;
            for(CubeStats stats: allStats) {
                
                /* Do a little math to scale to the cube size, round, and clamp
                 * to an integer
                 */
                int cubeCount = (int) Math.round(
                        CUBE_SIZE * (stats.mCount / ((double)totalSize)));
                
                /* If there is some number of this color/type/cmc combo */
                if(cubeCount != 0) {
                    
                    /* Print it out */
                    System.out.print(stats);
                    System.out.println(cubeCount);
                    totalCubeCount += cubeCount;
                }
            }
            
            /* Print the total number of scaled cube cards */
            System.out.println("Total Cube Count: " + totalCubeCount);
            
        } catch (SQLException | ClassNotFoundException e) {
            /* For exceptions, just print them out and exit cleanly */
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            e.printStackTrace();
        } finally {
            
            /* Close the database */
            if(dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException e) {
                    /* For exceptions, just print them out and exit cleanly */
                    System.err.println(e.getClass().getName() + ": " +
                                       e.getMessage());
                    System.exit(0);
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Given a set of colors, a type, and converted mana cost, return the number
     * of rows in the database that match the query, and scale that result by
     * the rarity occurrence of cards in a pack
     *
     * @param connection A connection to the SQLite database
     * @param colors The result must be one of the given colors (WUBRGALC)
     * @param type   The result must be one of the given types (Land, Instant..)
     * @param mRarity The result must be the given rarity ('C', 'U', etc)
     * @param cmc    The result must have the given converted mana cost (0, 1..)
     *               If this value is negative, it is ignored
     * @return The number of rows returned by the query, scaled by rarity
     * @throws SQLException
     */
    public static int getScaledQueryCount(Connection connection,
            String[] colors, String[] type, int cmc)
            throws SQLException {

        int scaledCount = 0;
        
        /* For each rarity */
        for(Rarity rarity : RARITIES) {
            
            /* Perform the query */
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(buildQuery(colors,
                    type, rarity.mRarity, cmc));
            
            /* Count the results */
            int tmpCount = 0;
            while (resultSet.next()) {
                tmpCount++;
            }
            
            /* Clean up */
            resultSet.close();
            statement.close();
            
            /* Scale the result by the rarity */
            scaledCount += (tmpCount * rarity.multiplier);
        }
        return scaledCount;
    }
    
    /**
     * Build a SQL query given a set of colors, a set of supertypes, the
     * rarity, and converted mana cost
     * 
     * @param colors The result must be one of the given colors (WUBRGALC)
     * @param type   The result must be one of the given types (Land, Instant..)
     * @param rarity The result must be the given rarity ('C', 'U', etc)
     * @param cmc    The result must have the given converted mana cost (0, 1..)
     *               If this value is negative, it is ignored
     * @return       A SQL query to search for cards
     */
    public static String buildQuery(String[] colors, String[] type, char rarity,
            int cmc) {
        /* Select all cards from Modern, excluding basic lands */
        String query = "SELECT _id FROM"
                + " (cards JOIN legal_sets"
                + " ON (cards.expansion = legal_sets.expansion))"
                + " WHERE (legal_sets.format = 'Modern')"
                + " AND (cards.supertype NOT LIKE 'Basic%')";
        
        /* For nonlands, make sure they have a mana cost (filters multicards) */
        if(!type[0].equals("Land")) {
            query += " AND (cards.manacost != '')";
        }
        
        /* Add a rarity filter */
        if (rarity != '\0') {
            query += " AND (cards.rarity = " + (int) rarity + ") ";
        }
        
        /* Add a supertype filter */
        if (type != null) {
            query += appendListToQuery("cards.supertype", type);
        }
        
        /* Add a cmc filter */
        if (cmc >= 0) {
            query += " AND (cards.cmc = " + cmc + ") ";
        }
        
        /* Add a color filter */
        if(colors != null) {
            query += appendListToQuery("cards.color", colors);
        }
        
        /* Return the built query */
        return query + ";";
    }
    
    /**
     * Build a SQL string which some field has to match at least one of the
     * options
     *
     * @param  field   The field to check in the SQL database
     * @param  options All the options for that field
     * @return A string of the form:
     *         " AND (field = 'option1' OR field = 'option2' OR ...)"
     */
    private static String appendListToQuery(String field, String[] options) {
        boolean first = true;
        String query = " AND (";
        for (String option : options) {
            if (!first) {
                query += "OR ";
            }
            query += field + " = '" + option + "' ";
            first = false;
        }
        return query + ")";
    }
}
