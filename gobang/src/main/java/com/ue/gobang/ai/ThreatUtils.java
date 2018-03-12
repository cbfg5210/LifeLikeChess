package com.ue.gobang.ai;

import android.graphics.Point;

import com.ue.gobang.util.Roles;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to search for threats in a Gomoku game. Threats exist around an
 * existing stone on the board and so we only need to search 4 fields in
 * every direction to find the threat.
 * <p>
 * Currently uses a very brute-force way of finding each threat and does
 * some redundant computations, will need to be improved in the future to
 * provide better AI performance
 */
public class ThreatUtils {

    List<ThreatPattern> REFUTATIONS;
    List<ThreatPattern> THREES;
    List<ThreatPattern> FOURS;

    public ThreatUtils() {
        this.THREES = new ArrayList<>();
        this.FOURS = new ArrayList<>();
        this.REFUTATIONS = new ArrayList<>();

        THREES.add(new ThreatPattern(new int[]{Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.EMPTY, Roles.EMPTY}, new int[]
                {0, 4, 5}));
        THREES.add(new ThreatPattern(new int[]{Roles.EMPTY, Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.EMPTY}, new int[]
                {0, 1, 5}));
        THREES.add(new ThreatPattern(new int[]{Roles.EMPTY, Roles.BLACK, Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.EMPTY}, new int[]
                {0, 2, 5}));
        THREES.add(new ThreatPattern(new int[]{Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.EMPTY, Roles.BLACK, Roles.EMPTY}, new int[]
                {0, 3, 5}));

        FOURS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.EMPTY}, new int[]{4}));
        FOURS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.EMPTY, Roles.BLACK}, new int[]{3}));
        FOURS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.BLACK, Roles.EMPTY, Roles.BLACK, Roles.BLACK}, new int[]{2}));
        FOURS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.BLACK}, new int[]{1}));
        FOURS.add(new ThreatPattern(new int[]{Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.BLACK}, new int[]{0}));

        REFUTATIONS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.BLACK, Roles.BLACK, Roles.EMPTY, Roles.EMPTY}, new
                int[]{3, 4}));
        REFUTATIONS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.BLACK, Roles.EMPTY, Roles.EMPTY, Roles.BLACK}, new
                int[]{2, 3}));
        REFUTATIONS.add(new ThreatPattern(new int[]{Roles.BLACK, Roles.EMPTY, Roles.EMPTY, Roles.BLACK, Roles.BLACK}, new
                int[]{1, 2}));
        REFUTATIONS.add(new ThreatPattern(new int[]{Roles.EMPTY, Roles.EMPTY, Roles.BLACK, Roles.BLACK, Roles.BLACK}, new
                int[]{0, 1}));
    }

    /**
     * Check a field for a broken three or a straight three pattern on the
     * board (0XXX0 and 0X0XX0) belonging to a player.
     *
     * @param playerIndex Player index
     * @return List of moves corresponding to the offensive squares of the
     * threat
     */
    public List<Point> getThrees(State state, Field field, int playerIndex) {
        return getThreatMoves(THREES, state, field, playerIndex);
    }

    /**
     * Check a field for a broken three or a straight three pattern on the
     * board (0XXX0 and 0X0XX0) belonging to a player.
     *
     * @param playerIndex Player index
     * @return List of moves corresponding to the offensive/defensive squares of
     * the threat
     */
    public List<Point> getFours(State state, Field field, int playerIndex) {
        return getThreatMoves(FOURS, state, field, playerIndex);
    }

    /**
     * Check a field for a pattern which can turn into a four, e.g. 00XXX
     *
     * @param playerIndex Player index
     * @return List of moves corresponding to the offensive/defensive squares of
     * the refutation
     */
    public List<Point> getRefutations(State state, Field field, int playerIndex) {
        return getThreatMoves(REFUTATIONS, state, field, playerIndex);
    }

    /**
     * Search for threats around a field in a game state, mapping each threat
     * to offensive/defensive moves if found.
     *
     * @param patternList List of ThreatPattern objects to search for
     * @param state       State to search
     * @param field       Field to search around
     * @param playerIndex Player index to search for
     * @return
     */
    private List<Point> getThreatMoves(List<ThreatPattern> patternList, State state, Field field, int playerIndex) {
        List<Point> threatMoves = new ArrayList<>();
        // Loop around the field in every direction
        // (diagonal/horizontal/vertical)
        for (int direction = 0; direction < 4; direction++) {
            Field[] directionArray = state.directions[field.row][field.col][direction];
            for (ThreatPattern pattern : patternList) {
                // Try to find the pattern
                int patternIndex = matchPattern(directionArray, pattern.getPattern(playerIndex));
                if (patternIndex != -1) {
                    // Found pattern, get the squares in the pattern and map
                    // them to moves on the board
                    for (int patternSquareIndex : pattern.getPatternSquares()) {
                        Field patternSquareField = directionArray[patternIndex + patternSquareIndex];
                        threatMoves.add(new Point(patternSquareField.row, patternSquareField.col));
                    }
                }
            }
        }
        return threatMoves;
    }

    /**
     * Search for a pattern in a field array.
     *
     * @param direction Field array
     * @param pattern   Pattern to match e.g. [2 0 2 2]
     * @return The starting index if found, or -1 if not found
     */
    private int matchPattern(Field[] direction, int[] pattern) {
        for (int i = 0; i < direction.length; i++) {
            // Check if the pattern lies within the bounds of the direction
            if (i + (pattern.length - 1) < direction.length) {
                int count = 0;
                for (int j = 0; j < pattern.length; j++) {
                    if (direction[i + j].index == pattern[j]) {
                        count++;
                    } else {
                        break;
                    }
                }
                // Every element was the same, return the start index
                if (count == pattern.length) {
                    return i;
                }
            } else {
                break;
            }
        }
        return -1;
    }

}
