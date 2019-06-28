package com.gelakinetic.CubeStats;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class cardCounts {
	double Sorcery;
	double Enchantment;
	double Land;
	HashMap<Integer, Double> Creature;
	double Instant;
	double Planeswalker;
	double Artifact;

	/**
	 * TODO
	 * 
	 * @param other
	 */
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

	/**
	 * TODO
	 */
	public cardCounts() {
		this.Sorcery = 0;
		this.Enchantment = 0;
		this.Land = 0;
		this.Instant = 0;
		this.Planeswalker = 0;
		this.Artifact = 0;
		this.Creature = new HashMap<>();
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getTotalCount() {
		double allCreatureCounts = 0;
		for (Integer key : Creature.keySet()) {
			allCreatureCounts += Creature.get(key);
		}
		return Sorcery + Enchantment + Land + Instant + Planeswalker + Artifact + allCreatureCounts;
	}

	/**
	 * 
	 * @param totalCount
	 * @param cubeSize
	 */
	public void scale(double totalCount, int cubeSize) {
		for (Integer key : Creature.keySet()) {
			Creature.put(key, (double) ((Creature.get(key) / totalCount) * cubeSize));
		}
		Sorcery = ((Sorcery / totalCount) * cubeSize);
		Enchantment = ((Enchantment / totalCount) * cubeSize);
		Land = ((Land / totalCount) * cubeSize);
		Instant = ((Instant / totalCount) * cubeSize);
		Planeswalker = ((Planeswalker / totalCount) * cubeSize);
		Artifact = ((Artifact / totalCount) * cubeSize);		
	}
	
	/**
	 * TODO
	 */
	public void round() {
		for (Integer key : Creature.keySet()) {
			Creature.put(key, (double) Math.round(Creature.get(key)));
		}
		Sorcery = Math.round(Sorcery);
		Enchantment = Math.round(Enchantment);
		Land = Math.round(Land);
		Instant = Math.round(Instant);
		Planeswalker = Math.round(Planeswalker);
		Artifact = Math.round(Artifact);
	}

	/**
	 * TODO
	 * 
	 * @param avgColoredCount
	 * @param originalCounts
	 * @param cubeSize
	 */
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
		
//		for(Double key : sortedKeys) {
//			System.out.println("  " + differences.get(key) + " " + key);			
//		}

		while (diff < 0) {
			// Add cards

			Double keyToAdd = sortedKeys.get(sortedKeys.size() - 1);
//			System.out.println("    Adding " + keyToAdd);
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
//			System.out.println("    Removing " + differences.get(keyToRemove));
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

	/**
	 * TODO
	 * 
	 * @param differences
	 * @param string
	 * @param sorcery2
	 * @param originalSize
	 * @param cubeSize
	 */
	private void addDifference(HashMap<Double, String> differences, String typeStr, double scaledCount,
			double originalSize, int cubeSize) {
		if(0 != scaledCount) {
			double error = scaledCount - Math.round(scaledCount);
			differences.put(error, typeStr);
		}
	}
}