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
			int cubeSize = 15 * 3 * 8;

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

		} catch (ClassNotFoundException | SQLException e) {
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
			counts.get(key).tweak(avgColoredCount, originalScaled.get(key));
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
		counts.get(MagicConstants.GOLD).tweak(goldCount, originalScaled.get(MagicConstants.GOLD));
		counts.get(MagicConstants.COLORLESS).tweak(colorlessCount, originalScaled.get(MagicConstants.COLORLESS));
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

		// Get a list of all core sets after Alliances (834379200), when the 15 card booster started
		ArrayList<String> coreSetCodes = new ArrayList<>();
		String setQuery = "SELECT code FROM sets WHERE set_type = 'core' AND date > 834379200";
		Statement setStatement = dbConnection.createStatement();
		ResultSet queryResult = setStatement.executeQuery(setQuery);
		while(queryResult.next())
		{
			coreSetCodes.add(queryResult.getString(queryResult.findColumn("code")));
		}
		queryResult.close();
		setStatement.close();

		// Start a collection of all cards to analyze
		ArrayList<MtgCard> allCards = new ArrayList<>();

		// For each set code
		for (String setCode : coreSetCodes) {
			Statement statement = dbConnection.createStatement();
			// Select all non-backface, non-basic lands with unique names
			String query =
					"SELECT\n" +
							"	DISTINCT suggest_text_1,\n" +
							"	rarity,\n" +
							"	cmc,\n" +
							"	supertype,\n" +
							"	color\n" +
							"FROM cards\n" +
							"WHERE (\n" +
							"   expansion = '" + setCode + "' AND \n" +
							"   supertype NOT LIKE 'Basic %' AND \n" +
							"   number NOT LIKE '%b')";
			ResultSet resultSet = statement.executeQuery(query);

			/* Objectify the results */
			ArrayList<MtgCard> setCards = new ArrayList<>();
			while (resultSet.next()) {
				setCards.add(new MtgCard(resultSet));
			}

			/* Clean up */
			resultSet.close();
			statement.close();

			/* Count up all commons, uncommons, and rares */
			float[] rarityCount = {0,0,0,0,0};
			for(MtgCard card : setCards)
			{
				switch (card.rarity){
					case 'c':
					case 'C':
					{
						rarityCount[0]++;
						break;
					}
					case 'u':
					case 'U':
					{
						rarityCount[1]++;
						break;
					}
					case 'r':
					case 'R':
					{
						rarityCount[2]++;
						break;
					}
					case 'm':
					case 'M':
					{
						rarityCount[3]++;
						break;
					}
					case 't':
					case 'T':
					{
						rarityCount[4]++;
						break;
					}
				}
			}

//			if(rarityCount[3] > 0){
//				System.out.println(String.format("%3s [%1.4f, %1.4f, %1.4f, %1.4f]", setCode, 11/rarityCount[0], 3/rarityCount[1], 7/(8*rarityCount[2]), 1/(8*rarityCount[3])));
//			}
//			else {
//				System.out.println(String.format("%3s [%1.4f, %1.4f, %1.4f, %1.4f]", setCode, 11/rarityCount[0], 3/rarityCount[1], 1/rarityCount[2], 0.0f));
//			}

			/* Set rarity multipliers based on the number of commons, uncommons, and rares */
			for(MtgCard card : setCards) {
				card.setRarityMultiplier(rarityCount);
			}
			allCards.addAll(setCards);
		}

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
