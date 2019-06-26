package com.gelakinetic.CubeStats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

class MtgCard {

    private String color;
    private char rarity;
    private int cmc;
    private String supertype;

    MtgCard(ResultSet cursor) throws SQLException {
        this.color = cursor.getString(cursor.findColumn("color"));
        this.rarity = (char) cursor.getInt(cursor.findColumn("rarity"));
        this.cmc = cursor.getInt(cursor.findColumn("cmc"));
        this.supertype = cursor.getString(cursor.findColumn("supertype"));
        String manacost = cursor.getString(cursor.findColumn("manacost"));

        if (null == manacost) {
            System.out.println("null mana");
        }

        // Clean up color stuff (artifact, land, and colorless aren't colors)
        this.color.replaceAll("[ALCalc]", "");

        // Create a Pattern object to match manacost
        Pattern r = Pattern.compile("[WUBRGwubrg]");

        // Lands are always colorless
        if (this.supertype.toLowerCase().contains("land")) {
            this.color = "";
        }
        // Artifacts are colorless too
        else if (this.supertype.toLowerCase().contains("artifact") && !r.matcher(manacost).matches()) {
            this.color = "";
        }
    }

    /**
     * TODO
     * 
     * @return
     */
    double getRarityMultiplier() {
        switch (rarity) {
            case 'c':
            case 'C':
                return 10;
            case 'u':
            case 'U':
                return 3;
            case 'r':
            case 'R':
                return 7 / 8.0d;
            case 'm':
            case 'M':
                return 1 / 8.0d;
            default:
                return 0;
        }
    }

    int getCmc() {
        return cmc;
    }

    String getType() {
        return supertype;
    }

    /**
     * TODO
     * 
     * @return
     */
    String getColorKey() {
        if (null == color || color.isEmpty()) {
            return "Colorless";
        }
        else if (color.length() > 1) {
            return "Gold";
        }
        else {
            switch (color) {
                case "W":
                    return "White";
                case "U":
                    return "Blue";
                case "B":
                    return "Black";
                case "R":
                    return "Red";
                case "G":
                    return "Green";
            }
        }
        return "Colorless";
    }
}
