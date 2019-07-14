package com.gelakinetic.CubeStats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ScryfallReader {

	private static final String[] edhBanlist = {
			"Ancestral Recall", 
			"Balance", 
			"Biorhythm", 
			"Black Lotus", 
			"Braids, Cabal Minion", 
			"Chaos Orb", 
			"Coalition Victory", 
			"Channel", 
			"Emrakul, the Aeons Torn", 
			"Erayo, Soratami Ascendant", 
			"Falling Star", 
			"Fastbond", 
			"Gifts Ungiven", 
			"Griselbrand", 
			"Iona, Shield of Emeria", 
			"Karakas", 
			"Leovold, Emissary of Trest", 
			"Library of Alexandria", 
			"Limited Resources", 
			"Mox Emerald", 
			"Mox Jet", 
			"Mox Pearl", 
			"Mox Ruby", 
			"Mox Sapphire", 
			"Paradox Engine", 
			"Panoptic Mirror", 
			"Primeval Titan", 
			"Prophet of Kruphix", 
			"Recurring Nightmare", 
			"Rofellos, Llanowar Emissary", 
			"Shahrazad", 
			"Sundering Titan", 
			"Sway of the Stars", 
			"Sylvan Primordial", 
			"Time Vault", 
			"Time Walk", 
			"Tinker", 
			"Tolarian Academy", 
			"Trade Secrets", 
			"Upheaval", 
			"Worldfire", 
			"Yawgmoth's Bargain"
	};

	class ScryfallCard implements Comparable<ScryfallCard> {
		String object;
		String id;
		String oracle_id;
		long[] multiverse_ids;
		long tcgplayer_id;
		String name;
		String lang;
		String released_at;
		URI uri;
		URI scryfall_uri;
		String layout;
		boolean highres_image;
		HashMap<String, URI> image_uris;
		String mana_cost;
		double cmc;
		String type_line;
		String oracle_text;
		String loyalty;
		char[] colors;
		char[] color_identity;
		HashMap<String, String> legalities;
		String[] games;
		boolean reserved;
		boolean foil;
		boolean nonfoil;
		boolean oversized;
		boolean promo;
		boolean reprint;
		boolean variation;
		String set;
		String set_name;
		String set_type;
		URI set_uri;
		URI set_search_uri;
		URI scryfall_set_uri;
		URI rulings_uri;
		URI prints_search_uri;
		String collector_number;
		boolean digital;
		String rarity;
		String illustration_id;
		String card_back_id;
		String artist;
		String border_color;
		String frame;
		boolean full_art;
		boolean textless;
		boolean booster;
		boolean story_spotlight;
		String[] promo_types;
		long edhrec_rank;
		HashMap<String, URI> related_uris;

		/**
		 * TODO
		 * 
		 * @param o
		 * @return
		 */
		@Override
		public int compareTo(ScryfallCard o) {
			return Long.compare(this.edhrec_rank, o.edhrec_rank);
		}

		/**
		 * TODO
		 * 
		 * @param obj
		 * @return
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ScryfallCard) {
				return this.name.equals(((ScryfallCard) obj).name);
			}
			return false;
		}

	}

	private ArrayList<ScryfallCard> cards = new ArrayList<ScryfallReader.ScryfallCard>();

	/**
	 * TODO
	 */
	public ScryfallReader() {
		try {
			for (ScryfallCard card : new Gson().fromJson(
					new InputStreamReader(new FileInputStream("scryfall-default-cards.json"), StandardCharsets.UTF_8),
					ScryfallCard[].class)) {
				if (card.edhrec_rank > 0 /* && !cards.contains(card) */) {
					cards.add(card);
				}
			}
			Collections.sort(cards);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO
	 * 
	 * @param cardToLookup
	 * @return
	 */
	public long getEdhRecRank(MtgCard cardToLookup) {

		for (ScryfallCard card : cards) {
			if (card.name.equals(cardToLookup.getName())) {
				return card.edhrec_rank;
			}
			for (long mId : card.multiverse_ids) {
				if (mId == cardToLookup.getMultiverseId()) {
					return card.edhrec_rank;
				}
			}
		}

		for (String bannedCard : edhBanlist) {
			if (bannedCard.equals(cardToLookup.getName())) {
				return Long.MAX_VALUE;
			}
		}

		System.err.println("EDHREC not found for " + cardToLookup.getName());
		return Long.MAX_VALUE;
	}
}
