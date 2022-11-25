package com.gelakinetic.CubeStats;

import java.sql.ResultSet;
import java.sql.SQLException;

class MtgCard {

    private final int cmc;
    private final String supertype;
    char rarity;
    private String color;
    private float rarityMultiplier = 0;

    /**
     * TODO
     *
     * @param cursor
     * @throws SQLException
     */
    MtgCard(ResultSet cursor) throws SQLException {
        this.color = cursor.getString(cursor.findColumn("color")).toUpperCase();
        this.rarity = Character.toUpperCase((char) cursor.getInt(cursor.findColumn("rarity")));
        this.cmc = cursor.getInt(cursor.findColumn("cmc"));
        this.supertype = cursor.getString(cursor.findColumn("supertype"));

        // Clean up color stuff by removing all non color chars
        this.color = this.color.replaceAll("[^WUBRG]", "");
    }

    /**
     * TODO
     *
     * @return
     */
    double getRarityMultiplier() {
//		switch (rarity) {
//		case 'C':
//			return 10;
//		case 'U':
//			return 3;
//		case 'R':
//			return 7 / 8.0d;
//		case 'M':
//			return 1 / 8.0d;
//		case 'T':
//			return 1;
//		default:
//			return 0;
//		}
        return this.rarityMultiplier;
    }

    public void setRarityMultiplier(float[] rarityCount) {
        switch (this.rarity) {
            case 'c':
            case 'C': {
                this.rarityMultiplier = 11 / rarityCount[0];
                break;
            }
            case 'u':
            case 'U': {
                this.rarityMultiplier = 3 / rarityCount[1];
                break;
            }
            case 'r':
            case 'R': {
                if (rarityCount[3] > 0) {
                    this.rarityMultiplier = 7 / (8 * rarityCount[2]);
                } else {
                    this.rarityMultiplier = 1 / rarityCount[2];
                }
                break;
            }
            case 'm':
            case 'M': {
                if (rarityCount[3] > 0) {
                    this.rarityMultiplier = 1 / (8 * rarityCount[3]);
                } else {
                    this.rarityMultiplier = 0;
                }
                break;
            }
            case 't':
            case 'T': {
                this.rarityMultiplier = 1 / rarityCount[4];
                break;
            }
        }
    }

    /**
     * TODO
     *
     * @return
     */
    int getCmc() {
        return cmc;
    }

    /**
     * TODO
     *
     * @return
     */
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
            return MagicConstants.COLORLESS;
        } else if (color.length() > 1) {
            return MagicConstants.GOLD;
        } else {
            switch (color) {
                case "W":
                    return MagicConstants.WHITE;
                case "U":
                    return MagicConstants.BLUE;
                case "B":
                    return MagicConstants.BLACK;
                case "R":
                    return MagicConstants.RED;
                case "G":
                    return MagicConstants.GREEN;
            }
        }
        return MagicConstants.COLORLESS;
    }
}
