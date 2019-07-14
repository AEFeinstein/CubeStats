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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
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
			int cubeSize = 540;// 14 * 3 * 10;

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

			buildEdhrecCube(counts);

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
		ResultSet resultSet = statement.executeQuery(
				"SELECT\r\n" + 
				"	cards.rarity,\r\n" + 
				"	cards.cmc,\r\n" + 
				"	cards.supertype,\r\n" + 
				"	cards.color,\r\n" + 
				"	cards.suggest_text_1,\r\n" + 
				"	cards.number,\r\n" + 
				"	cards.color_identity,\r\n" + 
				"	cards.manacost,\r\n" + 
				"	cards.multiverseID\r\n" + 
				"FROM cards JOIN sets ON cards.expansion = sets.code\r\n" + 
				"WHERE (\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Archenemy%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Box Set%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Arsenal%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Duel Decks%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Explorers of Ixalan%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%From the Vault%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Promo%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Mythic Edition%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Guild Kits%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Masterpiece%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Event Deck%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Planechase%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Premium Deck Series%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Signature Spellbook%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Box Toppers%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Unglued%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Unhinged%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Unstable%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Visions%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Welcome Deck%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Expeditions%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Vanguard%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Chronicles%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Timeshifted%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Masters%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Gift Pack%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Game Night%' AND\r\n" + 
				"	sets.suggest_text_1 NOT LIKE '%Anthology%' AND\r\n" + 
				"	sets.online_only = 0 AND\r\n" + 
				"	cards.number not like '%B' AND\r\n" + 
				"	cards.supertype NOT LIKE 'Basic%')\r\n" + 
				"ORDER BY\r\n" + 
				"	cards.color_identity DESC,\r\n" + 
				"	cards.supertype DESC,\r\n" + 
				"	cards.cmc DESC");

		/* Objectify the results */
		ArrayList<MtgCard> allCards = new ArrayList<>();
		while (resultSet.next()) {
			allCards.add(new MtgCard(resultSet));
		}

		/* Clean up */
		resultSet.close();
		statement.close();

		/* Append edhrec ranks */
		ScryfallReader sfr = new ScryfallReader();
		for (MtgCard card : allCards) {
			card.setEdhRecRank(sfr.getEdhRecRank(card));
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
		double totalCountRounded = 0;
		for (String key : counts.keySet()) {
			System.out.println(key + " " + counts.get(key).getTotalCount());
			totalCountRounded += counts.get(key).getTotalCount();
		}
		System.out.println("Rounded count " + totalCountRounded);

		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		String str = gson.toJson(counts);
		System.out.println(str);
	}

	/**
	 * TODO
	 * 
	 * @param counts
	 */
	private static void buildEdhrecCube(HashMap<String, cardCounts> counts) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("autocube.txt"))) {
			for (String color : counts.keySet()) {
				cardCounts count = counts.get(color);
				
				// For gold cards only, resort into buckets by color, not type
				// This guarantees an even distribution of gold cards
				if (MagicConstants.GOLD.equals(color)) {
					int numPerGold = (int) (count.getTotalCount() / 10);

					HashMap<String, ArrayList<MtgCard>> goldByColor = new HashMap<String, ArrayList<MtgCard>>(10);

					for (String type : count.counts.keySet()) {
						ArrayList<MtgCard> bCards = count.bucketedCards.get(type);
						for (MtgCard card : bCards) {
							if (null == goldByColor.get(card.getColor())) {
								goldByColor.put(card.getColor(), new ArrayList<MtgCard>());
							}
							goldByColor.get(card.getColor()).add(card);
						}
					}

					for (String key : goldByColor.keySet()) {
						if (key.length() == 2) {
							System.out.println(key);
							ArrayList<MtgCard> bCards = goldByColor.get(key);
							Collections.sort(bCards);
							for (int idx = 0; idx < numPerGold; idx++) {
								System.out.println(
										"\t" + bCards.get(idx).getEdhrecRank() + " " + bCards.get(idx).getName());
								bw.write(bCards.get(idx).getName());
								bw.newLine();
							}
						}
					}

				} else {
					for (String type : count.counts.keySet()) {

						System.out.println(String.format("Looking for %d cards of (%s, %s)",
								count.counts.get(type).intValue(), color, type));

						ArrayList<MtgCard> bCards = count.bucketedCards.get(type);
						Collections.sort(bCards);
						for (int idx = 0; idx < count.counts.get(type).intValue(); idx++) {
							System.out
									.println("\t" + bCards.get(idx).getEdhrecRank() + " " + bCards.get(idx).getName());
							bw.write(bCards.get(idx).getName());
							bw.newLine();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
