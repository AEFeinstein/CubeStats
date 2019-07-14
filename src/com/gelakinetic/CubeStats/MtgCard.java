package com.gelakinetic.CubeStats;

import java.sql.ResultSet;
import java.sql.SQLException;

class MtgCard implements Comparable<MtgCard> {

	private String name;
	private String color;
	private char rarity;
	private int cmc;
	private String supertype;
	private long edhrec_rank;
	private long multiverseID;

	/**
	 * TODO
	 * 
	 * @param cursor
	 * @throws SQLException
	 */
	MtgCard(ResultSet cursor) throws SQLException {
		this.name = cursor.getString(cursor.findColumn("suggest_text_1"));
		this.color = cursor.getString(cursor.findColumn("color")).toUpperCase();
		this.rarity = Character.toUpperCase((char) cursor.getInt(cursor.findColumn("rarity")));
		this.cmc = cursor.getInt(cursor.findColumn("cmc"));
		this.supertype = cursor.getString(cursor.findColumn("supertype"));
		this.multiverseID = cursor.getLong(cursor.findColumn("multiverseID"));

		// For split cards, count all part's colors
		String number = cursor.getString(cursor.findColumn("number"));
		if (number.contains("a") || number.contains("b")) {
			this.color = cursor.getString(cursor.findColumn("color_identity"));
		}

		// Clean up color stuff by removing all non color chars
		this.color = this.color.replaceAll("[^WUBRG]", "");

		String manaCost = cursor.getString(cursor.findColumn("manacost"));
		if (null == color || color.isEmpty()) {
			for (String colorChar : MagicConstants.COLOR_CHARS) {
				if (manaCost.contains(colorChar)) {
					color += colorChar;
				}
			}
		}
	}

	/**
	 * TODO
	 * 
	 * @param other
	 */
	MtgCard(MtgCard other) {
		this.name = other.name;
		this.color = other.color;
		this.rarity = other.rarity;
		this.cmc = other.cmc;
		this.supertype = other.supertype;
		this.edhrec_rank = other.edhrec_rank;
		this.multiverseID = other.multiverseID;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	double getRarityMultiplier() {
		switch (rarity) {
		case 'C':
			return 10;
		case 'U':
			return 3;
		case 'R':
			return 7 / 8.0d;
		case 'M':
			return 1 / 8.0d;
		case 'T':
			return 1;
		default:
			return 0;
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

	/**
	 * TODO
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * TODO
	 * 
	 * @param edhRecRank
	 */
	public void setEdhRecRank(long edhRecRank) {
		this.edhrec_rank = edhRecRank;
	}

	/**
	 * TODO
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(MtgCard o) {
		return Long.compare(this.edhrec_rank, o.edhrec_rank);
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public long getEdhrecRank() {
		return this.edhrec_rank;
	}

	/**
	 * TODO
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MtgCard) {
			return this.name.equals(((MtgCard) obj).name);
		}
		return false;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public long getMultiverseId() {
		return this.multiverseID;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public String getColor() {
		return this.color;
	}
}
