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

public class CubeStats {

    /** The color label for this statistics entry */
    String mColor;

    /** The type label for this statistics entry */
    String mType;
    
    /** The converted mana cost for this statistics entry */
    int mCmc;
    
    /** The scaled query result count for this statistics entry */
    int mCount;
    
    /**
     * Plain 'ole constructor which sets the Rarity's variables
     * 
     * @param color The color label for this statistics entry
     * @param type  The type label for this statistics entry
     * @param cmc   The converted mana cost for this statistics entry
     * @param count The scaled query result count for this statistics entry
     */
    public CubeStats(String color, String type, int cmc, int count) {
        this.mColor = color;
        this.mType = type;
        this.mCmc = cmc;
        this.mCount = count;
    }
    
    /**
     * @return A string label for this statistics entry
     */
    public String toString() {
        
        /* The label has the color & type */
        String label = mColor + ", " + mType;
        
        /* If the entry has a converted mana cost, add it to the label */
        if(mCmc >= 0) {
            return label + ", {" + mCmc + "}: ";            
        }
        
        /* Otherwise leave it out */
        return label + ": ";
    }
}
