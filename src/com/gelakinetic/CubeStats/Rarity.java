/**
 * Copyright 2016 Adam Feinstein
 * <p>
 * This file is part of CubeStats.
 * <p>
 * CubeStats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CubeStats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CubeStats.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gelakinetic.CubeStats;

class Rarity {

    /**
     * The rarity of a card, either C, U, R, M, or T
     */
    public final char mRarity;

    /**
     * The occurrence multiplier for a rarity (88, 24, 8, 1, 8)
     */
    public final int multiplier;

    /**
     * Plain 'ole constructor which sets the Rarity's variables
     *
     * @param c The rarity of a card, either C, U, R, M, or T
     * @param i The occurrence multiplier for a rarity (88, 24, 8, 1, 8)
     */
    public Rarity(char c, int i) {
        mRarity = c;
        multiplier = i;
    }
}
