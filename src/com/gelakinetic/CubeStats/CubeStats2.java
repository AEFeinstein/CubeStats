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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CubeStats2 {

	private static class cardCounts {
		double Sorcery;
		double Enchantment;
		double Land;
		HashMap<Integer, Double> Creature;
		double Instant;
		double Planeswalker;
		double Artifact;

		public cardCounts(cardCounts other) {
			this.Sorcery = other.Sorcery;
			this.Enchantment = other.Enchantment;
			this.Land = other.Land;
			this.Instant = other.Instant;
			this.Planeswalker = other.Planeswalker;
			this.Artifact = other.Artifact;

			this.Creature = new HashMap<>();
			for (Integer key : other.Creature.keySet()) {
				this.Creature.put(key, other.Creature.get(key));
			}
		}

		public cardCounts() {
			this.Sorcery = 0;
			this.Enchantment = 0;
			this.Land = 0;
			this.Instant = 0;
			this.Planeswalker = 0;
			this.Artifact = 0;
			this.Creature = new HashMap<>();
		}

		public double getTotalCount() {
			double allCreatureCounts = 0;
			for (Integer key : Creature.keySet()) {
				allCreatureCounts += Creature.get(key);
			}
			return Sorcery + Enchantment + Land + Instant + Planeswalker + Artifact + allCreatureCounts;
		}

		public void round(double cardCount, int cubeCount) {
			for (Integer key : Creature.keySet()) {
				Creature.put(key, (double) Math.round((Creature.get(key) / cardCount) * cubeCount));
			}
			Sorcery = Math.round((Sorcery / cardCount) * cubeCount);
			Enchantment = Math.round((Enchantment / cardCount) * cubeCount);
			Land = Math.round((Land / cardCount) * cubeCount);
			Instant = Math.round((Instant / cardCount) * cubeCount);
			Planeswalker = Math.round((Planeswalker / cardCount) * cubeCount);
			Artifact = Math.round((Artifact / cardCount) * cubeCount);
		}

//		public void printOutliers(double cardCount, int cubeCount) {
//			// TODO Auto-generated method stub
//			double[] min = { Double.MAX_VALUE };
//			double[] max = { 0 };
//			String minStr = null, maxStr = null;
//
//			if (checkMin(Sorcery, min, cardCount, cubeCount)) {
//				minStr = "Sorcery";
//			}
//			if (checkMax(Sorcery, max, cardCount, cubeCount)) {
//				maxStr = "Sorcery";
//			}
//
//			if (checkMin(Enchantment, min, cardCount, cubeCount)) {
//				minStr = "Enchantment";
//			}
//			if (checkMax(Enchantment, max, cardCount, cubeCount)) {
//				maxStr = "Enchantment";
//			}
//
//			if (checkMin(Land, min, cardCount, cubeCount)) {
//				minStr = "Land";
//			}
//			if (checkMax(Land, max, cardCount, cubeCount)) {
//				maxStr = "Land";
//			}
//
//			if (checkMin(Instant, min, cardCount, cubeCount)) {
//				minStr = "Instant";
//			}
//			if (checkMax(Instant, max, cardCount, cubeCount)) {
//				maxStr = "Instant";
//			}
//
//			if (checkMin(Planeswalker, min, cardCount, cubeCount)) {
//				minStr = "Planeswalker";
//			}
//			if (checkMax(Planeswalker, max, cardCount, cubeCount)) {
//				maxStr = "Planeswalker";
//			}
//
//			if (checkMin(Artifact, min, cardCount, cubeCount)) {
//				minStr = "Artifact";
//			}
//			if (checkMax(Artifact, max, cardCount, cubeCount)) {
//				maxStr = "Artifact";
//			}
//
//			for (Integer key : Creature.keySet()) {
//				if (checkMin(Creature.get(key), min, cardCount, cubeCount)) {
//					minStr = "Creature-" + key;
//				}
//				if (checkMax(Creature.get(key), max, cardCount, cubeCount)) {
//					maxStr = "Creature-" + key;
//				}
//			}
//
//			System.out.println("  Cut " + minStr);
//			System.out.println("  Add " + maxStr);
//		}
//
//		private static boolean checkMax(double sorcery2, double[] max, double cardCount, int cubeCount) {
//			double scaled = (sorcery2 / cardCount) * cubeCount;
//			double decimal = scaled - (long) (scaled);
//			if (decimal > max[0]) {
//				max[0] = decimal;
//				return true;
//			}
//			return false;
//		}
//
//		private static boolean checkMin(double sorcery2, double[] min, double cardCount, int cubeCount) {
//			if (sorcery2 == 0) {
//				return false;
//			}
//			double scaled = (sorcery2 / cardCount) * cubeCount;
//			double decimal = scaled - (long) (scaled);
//			if (decimal < min[0]) {
//				min[0] = decimal;
//				return true;
//			}
//			return false;
//		}

		public void tweak(int avgColoredCount, cardCounts originalCounts, int cubeSize) {
			int diff = (int) (this.getTotalCount() - avgColoredCount);

			if (diff == 0) {
				return;
			}

			double originalSize = originalCounts.getTotalCount();

			HashMap<Double, String> differences = new HashMap<>();
			addDifference(differences, "Sorcery", originalCounts.Sorcery, originalSize, cubeSize);
			addDifference(differences, "Instant", originalCounts.Instant, originalSize, cubeSize);
			addDifference(differences, "Enchantment", originalCounts.Enchantment, originalSize, cubeSize);
			addDifference(differences, "Artifact", originalCounts.Artifact, originalSize, cubeSize);
			addDifference(differences, "Planeswalker", originalCounts.Planeswalker, originalSize, cubeSize);
			addDifference(differences, "Land", originalCounts.Land, originalSize, cubeSize);
			for (Integer key : originalCounts.Creature.keySet()) {
				addDifference(differences, "Creature-" + key, originalCounts.Creature.get(key), originalSize, cubeSize);
			}

			List<Double> sortedKeys = differences.keySet().stream().collect(Collectors.toList());
			Collections.sort(sortedKeys);

			while (diff < 0) {
				// Add cards

				Double keyToAdd = sortedKeys.get(sortedKeys.size() - 1);
				switch (differences.get(keyToAdd)) {
					case "Sorcery": {
						this.Sorcery++;
						break;
					}
					case "Instant": {
						this.Instant++;
						break;
					}
					case "Enchantment": {
						this.Enchantment++;
						break;
					}
					case "Land": {
						this.Land++;
						break;
					}
					case "Artifact": {
						this.Artifact++;
						break;
					}
					case "Planeswalker": {
						this.Planeswalker++;
						break;
					}
					default: {
						if (differences.get(keyToAdd).contains("Creature")) {
							String[] parts = differences.get(keyToAdd).split("-");
							double count = this.Creature.get(Integer.parseInt(parts[1]));
							count++;
							this.Creature.put(Integer.parseInt(parts[1]), count);
						}
					}
				}
				sortedKeys.remove(keyToAdd);

				diff++;
			}

			while (diff > 0) {
				Double keyToRemove = sortedKeys.get(0);
				switch (differences.get(keyToRemove)) {
					case "Sorcery": {
						this.Sorcery--;
						break;
					}
					case "Instant": {
						this.Instant--;
						break;
					}
					case "Enchantment": {
						this.Enchantment--;
						break;
					}
					case "Land": {
						this.Land--;
						break;
					}
					case "Artifact": {
						this.Artifact--;
						break;
					}
					case "Planeswalker": {
						this.Planeswalker--;
						break;
					}
					default: {
						if (differences.get(keyToRemove).contains("Creature")) {
							String[] parts = differences.get(keyToRemove).split("-");
							double count = this.Creature.get(Integer.parseInt(parts[1]));
							count--;
							this.Creature.put(Integer.parseInt(parts[1]), count);
						}
					}
				}
				sortedKeys.remove(keyToRemove);

				diff--;
			}
		}

		private void addDifference(HashMap<Double, String> differences, String string, double sorcery2,
				double originalSize, int cubeSize) {
			double count = ((sorcery2 / originalSize) * cubeSize);
			if (count != Math.round(count)) {
				differences.put(count - Math.round(count), string); // big numbers get added, small numbers get removed
			}
		}
	}

	/**
	 * All the types. They are in distinct buckets (i.e. Artifact Lands don't count
	 * as Artifacts. The search is ordered, so first it searches for all lands, then
	 * creatures NOT lands, then artifacts NOT creatures NOT lands, etc
	 */
	private static final String[] TYPES = { "Creature", "Artifact", "Enchantment", "Instant", "Sorcery", "Planeswalker",
			"Land", };

	/**
	 * Given the database of Magic cards used by MTG Familiar and a cube size,
	 * figure out how many of each card color, type, and converted mana cost (for
	 * creatures) are necessary
	 *
	 * @param args
	 *			Unused
	 */
	public static void main(String[] args) {
		try {
			// Get all the cards from the database
			ArrayList<MtgCard> allCards = doDatabaseQuery();

			// Bucket the results into the hash map
			HashMap<String, cardCounts> counts = bucketDatabaseResults(allCards);

			// TODO scale everything to a cube size
			roundCounts(counts, 14 * 3 * 10);

			// Print the results
			printResults(counts);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void roundCounts(HashMap<String, cardCounts> counts, int cubeSize) {
		// TODO Auto-generated method stub
		double totalCount = 0;
		for (String key : counts.keySet()) {
			totalCount += counts.get(key).getTotalCount();
		}

		// Make a deep copy
		HashMap<String, cardCounts> original = new HashMap<String, cardCounts>();
		for (String key : counts.keySet()) {
			original.put(key, new cardCounts(counts.get(key)));
		}

		// Round each card to a whole number
		for (String key : counts.keySet()) {
			counts.get(key).round(totalCount, cubeSize);
		}

		// Find the average number of cards for a color
		String[] colorKeys = { "White", "Blue", "Black", "Red", "Green" };
		int coloredCounts = 0;
		for (String key : colorKeys) {
			coloredCounts += (int) counts.get(key).getTotalCount();
		}
		int avgColoredCount = Math.round(coloredCounts / 5.0f);
		
		// For each color, tweak it to match the average number of cards for a color
		for (String key : colorKeys) {
			counts.get(key).tweak(avgColoredCount, original.get(key), cubeSize);
		}

		// See how many cards are in the current cube
		int totalCountRounded = 0;
		for (String key : counts.keySet()) {
			totalCountRounded += counts.get(key).getTotalCount();
		}

		// Add lands to get to the cube size
		counts.get("Colorless").Land += (cubeSize - totalCountRounded);

//		// Debug print
//		totalCountRounded = 0;
//		for (String key : counts.keySet()) {
//			System.out.println(key + " " + counts.get(key).getTotalCount());
//			totalCountRounded += counts.get(key).getTotalCount();
//		}
//		System.out.println("Rounded count " + totalCountRounded);
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
				"select cards.color_identity, cards.rarity, cards.cmc, cards.supertype, cards.color, cards.manacost\r\n"
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
		for (String targetType : TYPES) {
			// Iterate over all the cards
			for (int cIdx = 0; cIdx < allCards.size(); cIdx++) {
				MtgCard cardToCheck = allCards.get(cIdx);
				// If the type matches
				if (cardToCheck.getType().toLowerCase().contains(targetType.toLowerCase())) {
					// Figure out what bucket to put this card in

					// Make sure this color bucket exists first
					if (!counts.containsKey(cardToCheck.getColorKey())) {
						counts.put(cardToCheck.getColorKey(), new cardCounts());
					}
					cardCounts colorBucket = counts.get(cardToCheck.getColorKey());

					switch (targetType) {
						case "Creature": {
							if (!colorBucket.Creature.containsKey(cardToCheck.getCmc())) {
								colorBucket.Creature.put(cardToCheck.getCmc(), (double) 0);
							}
							double currentCount = colorBucket.Creature.get(cardToCheck.getCmc());
							colorBucket.Creature.put(cardToCheck.getCmc(),
									currentCount + cardToCheck.getRarityMultiplier());
							break;
						}
						case "Artifact": {
							colorBucket.Artifact += cardToCheck.getRarityMultiplier();
							break;
						}
						case "Enchantment": {
							colorBucket.Enchantment += cardToCheck.getRarityMultiplier();
							break;
						}
						case "Instant": {
							colorBucket.Instant += cardToCheck.getRarityMultiplier();
							break;
						}
						case "Sorcery": {
							colorBucket.Sorcery += cardToCheck.getRarityMultiplier();
							break;
						}
						case "Planeswalker": {
							colorBucket.Planeswalker += cardToCheck.getRarityMultiplier();
							break;
						}
						case "Land": {
							colorBucket.Land += cardToCheck.getRarityMultiplier();
							break;
						}
					}

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
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		String str = gson.toJson(counts);
		System.out.println(str);
	}
}
