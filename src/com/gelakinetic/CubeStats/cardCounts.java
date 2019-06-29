package com.gelakinetic.CubeStats;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

class cardCounts {

	private transient final Comparator<String> typeKeyComparator = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			// Two non-creature
			if (!s1.contains("-") || !s2.contains("-")) {
				return s1.compareTo(s2);
			}
			// Two creature
			else {
				int cmc1 = Integer.parseInt(s1.split("-")[1]);
				int cmc2 = Integer.parseInt(s2.split("-")[1]);
				return Integer.compare(cmc1, cmc2);
			}
		}
	};

	private final TreeMap<String, Double> counts = new TreeMap<>(typeKeyComparator);

	/**
	 * TODO
	 * 
	 * @param other
	 */
	public cardCounts(cardCounts other) {
		for (String key : other.counts.keySet()) {
			this.counts.put(key, other.counts.get(key));
		}
	}

	/**
	 * TODO
	 */
	public cardCounts() {
		;
	}

	/**
	 * TODO
	 * 
	 * @param targetType
	 * @param cardToCheck
	 */
	public void putCard(String targetType, MtgCard cardToCheck) {
		if (MagicConstants.CREATURE.equals(targetType)) {
			targetType += "-" + cardToCheck.getCmc();
		}

		double currentCount = 0;
		if (this.counts.containsKey(targetType)) {
			currentCount = this.counts.get(targetType);
		}
		currentCount += cardToCheck.getRarityMultiplier();
		this.counts.put(targetType, currentCount);
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public double getTotalCount() {
		double allCounts = 0;
		for (String key : counts.keySet()) {
			allCounts += counts.get(key);
		}
		return allCounts;
	}

	/**
	 * TODO
	 * 
	 * @param totalCount
	 * @param cubeSize
	 */
	public void scale(double totalCount, int cubeSize) {
		for (String key : counts.keySet()) {
			counts.put(key, (double) ((counts.get(key) / totalCount) * cubeSize));
		}
	}

	/**
	 * TODO
	 */
	public void round() {
		for (String key : counts.keySet()) {
			counts.put(key, (double) Math.round(counts.get(key)));
		}
	}

	/**
	 * TODO
	 * 
	 * @param avgColoredCount
	 * @param originalCounts
	 * @param cubeSize
	 */
	public void tweak(int avgColoredCount, cardCounts originalCounts, int cubeSize) {

		// See how many cards we're off by
		int diff = (int) (this.getTotalCount() - avgColoredCount);
		if (diff == 0) {
			return;
		}

		// Find the errors for each key, -0.5 to 0.5
		HashMap<Double, String> errors = new HashMap<>();
		for (String key : originalCounts.counts.keySet()) {
			double error = originalCounts.counts.get(key) - Math.round(originalCounts.counts.get(key));
			errors.put(error, key);
		}

		// Sort the errors
		List<Double> sortedKeys = errors.keySet().stream().collect(Collectors.toList());
		Collections.sort(sortedKeys);

		// While there are cards to add or remove
		while (diff < 0) {
			// Add cards
			Double keyToAdd = sortedKeys.get(sortedKeys.size() - 1);
			String typeKey = errors.get(keyToAdd);
			double currentCount = this.counts.get(typeKey);
			this.counts.put(typeKey, currentCount + 1);
			sortedKeys.remove(keyToAdd);
			diff++;
		}
		while (diff > 0) {
			Double keyToRemove = sortedKeys.get(0);
			String typeKey = errors.get(keyToRemove);
			double currentCount = this.counts.get(typeKey);
			this.counts.put(typeKey, currentCount - 1);
			sortedKeys.remove(keyToRemove);
			diff--;
		}
	}
}