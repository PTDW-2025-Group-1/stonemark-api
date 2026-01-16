package pt.estga.shared.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class StringSimilarityUtils {

    private StringSimilarityUtils() {
        // Utility class
    }

    /**
     * Checks if either string contains the other, ignoring case.
     *
     * @param str1 The first string.
     * @param str2 The second string.
     * @return true if str1 contains str2 or str2 contains str1.
     */
    public static boolean containsIgnoreCase(String str1, String str2) {
        if (str1 == null || str2 == null) return false;
        String s1 = str1.toLowerCase();
        String s2 = str2.toLowerCase();
        return s1.contains(s2) || s2.contains(s1);
    }

    /**
     * Calculates the Levenshtein similarity between two strings.
     *
     * @param str1 The first string.
     * @param str2 The second string.
     * @return A value between 0.0 and 1.0, where 1.0 is identical.
     */
    public static double calculateLevenshteinSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        String s1 = str1.toLowerCase();
        String s2 = str2.toLowerCase();

        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();
        int distance = levenshtein.apply(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        if (maxLength == 0) return 1.0;
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * Calculates a similarity score based on word matching.
     *
     * @param str1 The first string.
     * @param str2 The second string.
     * @param minWordLength Minimum length of a word to be considered.
     * @param maxWordTypoDistance Maximum allowed typos per word.
     * @return The number of matching words.
     */
    public static int countMatchingWords(String str1, String str2, int minWordLength, int maxWordTypoDistance) {
        if (str1 == null || str2 == null) {
            return 0;
        }

        String s1 = str1.toLowerCase();
        String s2 = str2.toLowerCase();
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();

        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");

        int matchCount = 0;
        for (String w1 : words1) {
            if (w1.length() < minWordLength) continue;
            for (String w2 : words2) {
                if (w2.length() < minWordLength) continue;
                if (levenshtein.apply(w1, w2) <= maxWordTypoDistance) {
                    matchCount++;
                    break; // Count each word from str1 at most once
                }
            }
        }
        return matchCount;
    }

    /**
     * Checks if two strings are similar based on containment, Levenshtein distance, or word matching.
     *
     * @param str1 The first string to compare.
     * @param str2 The second string to compare.
     * @param similarityThreshold The threshold for Levenshtein similarity (0.0 to 1.0).
     * @param minWordLength The minimum length of words to consider for word matching.
     * @param maxWordTypoDistance The maximum Levenshtein distance allowed for matching individual words.
     * @return true if the strings are considered similar, false otherwise.
     */
    public static boolean areStringsSimilar(String str1, String str2, double similarityThreshold, int minWordLength, int maxWordTypoDistance) {
        if (containsIgnoreCase(str1, str2)) return true;
        if (calculateLevenshteinSimilarity(str1, str2) > similarityThreshold) return true;
        return countMatchingWords(str1, str2, minWordLength, maxWordTypoDistance) > 0;
    }
}
