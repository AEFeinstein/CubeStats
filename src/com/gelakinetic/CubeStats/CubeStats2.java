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
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CubeStats2 {

	/**
	 * Given the database of Magic cards used by MTG Familiar and a cube size,
	 * figure out how many of each card color, type, and converted mana cost (for
	 * creatures) are necessary
	 *
	 * @param args Unused
	 */
	public static void main(String[] args) {
		try {
			int cubeSize = 14 * 3 * 10;

			// Get all the cards from the database
			ArrayList<MtgCard> allCards = doDatabaseQuery();

			// Bucket the results into the hash map
			HashMap<String, cardCounts> counts = bucketDatabaseResults(allCards);

			// Scale everything, but have double precision cards
			scaleCounts(counts, cubeSize);
			HashMap<String, cardCounts> originalScaled = deepCopyCounts(counts);

			// Round the cards to ints
			roundCounts(counts);

			// Tweak for color balance
			tweakCounts(counts, originalScaled, cubeSize);

			// Print the results
			printResults(counts);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO
	 * 
	 * @param counts
	 * @return
	 */
	private static HashMap<String, cardCounts> deepCopyCounts(HashMap<String, cardCounts> counts) {
		// Make a deep copy
		HashMap<String, cardCounts> original = new HashMap<String, cardCounts>();
		for (String key : counts.keySet()) {
			original.put(key, new cardCounts(counts.get(key)));
		}
		return original;
	}

	/**
	 * TODO
	 * 
	 * @param counts
	 * @param cubeSize
	 */
	private static void scaleCounts(HashMap<String, cardCounts> counts, int cubeSize) {
		double totalCount = 0;
		for (String key : counts.keySet()) {
			totalCount += counts.get(key).getTotalCount();
		}

		// Round each card to a whole number
		for (String key : counts.keySet()) {
			counts.get(key).scale(totalCount, cubeSize);
		}
	}

	/**
	 * TODO
	 * 
	 * @param counts
	 */
	private static void roundCounts(HashMap<String, cardCounts> counts) {
		// Round each card to a whole number
		for (String key : counts.keySet()) {
			counts.get(key).round();
		}
	}

	/**
	 * TODO
	 * 
	 * @param counts
	 * @param originalScaled
	 * @param cubeSize
	 */
	private static void tweakCounts(HashMap<String, cardCounts> counts, HashMap<String, cardCounts> originalScaled,
			int cubeSize) {
		// Find the average number of cards for a color
		int coloredCounts = 0;
		for (String key : MagicConstants.SINGLE_COLOR_KEYS) {
			coloredCounts += (int) counts.get(key).getTotalCount();
		}
		int avgColoredCount = Math.round(coloredCounts / 5.0f);

		// For each color, tweak it to match the average number of cards for a color
		for (String key : MagicConstants.SINGLE_COLOR_KEYS) {
			counts.get(key).tweak(avgColoredCount, originalScaled.get(key), cubeSize);
		}

		// For gold cards, make sure it's a multiple of 10, steal them from colorless
		int goldCount = (int) counts.get(MagicConstants.GOLD).getTotalCount();
		int colorlessCount = (int) counts.get(MagicConstants.COLORLESS).getTotalCount();
		while (goldCount % 10 != 0) {
			goldCount++;
			colorlessCount--;
		}

		// Then add colorless cards back until the cube size is right
		int totalCountRounded = 0;
		for (String key : counts.keySet()) {
			totalCountRounded += counts.get(key).getTotalCount();
		}
		colorlessCount += (cubeSize - totalCountRounded);

		// Then tweak gold and colorless after their numbers are figured out
		counts.get(MagicConstants.GOLD).tweak(goldCount, originalScaled.get(MagicConstants.GOLD), cubeSize);
		counts.get(MagicConstants.COLORLESS).tweak(colorlessCount, originalScaled.get(MagicConstants.COLORLESS),
				cubeSize);
	}

	/**
	 * TODO
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private static ArrayList<MtgCard> doDatabaseQuery() throws ClassNotFoundException, SQLException {
		/* Open up the database */
		Connection dbConnection = null;

		Class.forName("org.sqlite.JDBC");
		dbConnection = DriverManager.getConnection("jdbc:sqlite:mtg.db");

		Statement statement = dbConnection.createStatement();
		ResultSet resultSet = statement.executeQuery("select cards.rarity, cards.cmc, cards.supertype, cards.color\r\n"
				+ "from cards join sets on cards.expansion = sets.code\r\n" + "where (\r\n"
				+ "	sets.suggest_text_1 like '%Masters%' AND\r\n" + "	sets.online_only = 0 AND\r\n"
				+ "	cards.number not like \"%B\")\r\n"
				+ "order by cards.color_identity desc, cards.supertype desc, cards.cmc desc");

		/* Objectify the results */
		ArrayList<MtgCard> allCards = new ArrayList<>();
		while (resultSet.next()) {
			allCards.add(new MtgCard(resultSet));
		}

		/* Clean up */
		resultSet.close();
		statement.close();

		return allCards;
	}

	/**
	 * TODO
	 * 
	 * @param allCards
	 * @return
	 */
	private static HashMap<String, cardCounts> bucketDatabaseResults(ArrayList<MtgCard> allCards) {

		HashMap<String, cardCounts> counts = new HashMap<>();

		// For each type
		for (String targetType : MagicConstants.TYPES) {
			// Iterate over all the cards
			for (int cIdx = 0; cIdx < allCards.size(); cIdx++) {
				MtgCard cardToCheck = allCards.get(cIdx);
				// If the type matches
				if (cardToCheck.getType().toLowerCase().contains(targetType.toLowerCase())) {
					// Make sure this color bucket exists first, then add the card
					if (!counts.containsKey(cardToCheck.getColorKey())) {
						counts.put(cardToCheck.getColorKey(), new cardCounts());
					}
					counts.get(cardToCheck.getColorKey()).putCard(targetType, cardToCheck);

					// Remove the card and set the index back one
					allCards.remove(cIdx);
					cIdx--;
				}
			}
		}
		return counts;
	}

	/**
	 * TODO
	 * 
	 * @param counts
	 */
	private static void printResults(HashMap<String, cardCounts> counts) {

		// Debug print
//		double totalCountRounded = 0;
//		for (String key : counts.keySet()) {
//			System.out.println(key + " " + counts.get(key).getTotalCount());
//			totalCountRounded += counts.get(key).getTotalCount();
//		}
//		System.out.println("Rounded count " + totalCountRounded);

		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		String str = gson.toJson(counts);
		System.out.println(str);
	}
}
