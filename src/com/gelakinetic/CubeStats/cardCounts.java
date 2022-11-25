package com.gelakinetic.CubeStats;

import java.util.*;

class cardCounts {

    private transient final Comparator<String> typeKeyComparator = (s1, s2) -> {
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
        counts.replaceAll((k, v) -> (counts.get(k) / totalCount) * cubeSize);
    }

    /**
     * TODO
     */
    public void round() {
        counts.replaceAll((k, v) -> (double) Math.round(counts.get(k)));
    }

    /**
     * TODO
     *
     * @param avgColoredCount
     * @param originalCounts
     */
    public void tweak(int avgColoredCount, cardCounts originalCounts) {

        // See how many cards we're off by
        int diff = (int) (this.getTotalCount() - avgColoredCount);
        if (diff == 0) {
            return;
        }

        // While we're not at the target number of cards
        while (diff != 0) {
            // Find the errors for each key
            HashMap<Double, String> errors = new HashMap<>();
            for (String key : originalCounts.counts.keySet()) {
                double error = originalCounts.counts.get(key) - Math.round(this.counts.get(key));
                errors.put(error, key);
            }

            // Sort the errors
            List<Double> sortedKeys = new ArrayList<>(errors.keySet());
            Collections.sort(sortedKeys);

            // Either add or remove cards
            if (diff < 0) {
                // Add cards
                Double keyToAdd = sortedKeys.get(sortedKeys.size() - 1);
                String typeKey = errors.get(keyToAdd);
                double currentCount = this.counts.get(typeKey);
                this.counts.put(typeKey, currentCount + 1);
                diff++;
            } else {
                for (Double keyToRemove : sortedKeys) {
                    String typeKey = errors.get(keyToRemove);
                    double currentCount = this.counts.get(typeKey);
                    if (currentCount > 0) {
                        this.counts.put(typeKey, currentCount - 1);
                        diff--;
                        break;
                    }
                }
            }
        }
    }

    public void printCSV() {
        for (String key : this.counts.keySet()) {
            if (this.counts.get(key) > 0) {
                System.out.println("\t" + key + "\t" + this.counts.get(key).intValue());
            }
        }
    }
}