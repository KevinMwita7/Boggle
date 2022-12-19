import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.SET;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;

public class BoggleSolver {
    private final String[] dictionary;
    private Map<Character, Bag<Integer>> charLocations;
    private Digraph digraph;
    private SET<String> validWords;
//    TST<Integer> trie = new TST<>();

    private static class Cell {
        int row;
        int col;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "(" + row + "," + col + ")";
        }
    }

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        this.dictionary = new String[dictionary.length];

        for (int i = 0; i < dictionary.length; ++i) {
//            trie.put(dictionary[i], i);
            this.dictionary[i] = dictionary[i];
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        charLocations = new LinkedHashMap<>();
        digraph = new Digraph(board.rows() * board.cols());
        validWords = new SET<>();
        // Boolean flag to check whether the board just contains Q
        boolean boardFullOfQs = true;

        // Build digraph and a map of locations where a char is located on the board
        for (int i = 0; i < board.rows(); ++i) {
            for (int j = 0; j < board.cols(); ++j) {
                char letter = board.getLetter(i, j);
                int pos = i * board.cols() + j;

                if (letter != 'Q') boardFullOfQs = false;

                if (!charLocations.containsKey(letter)) {
                    charLocations.put(letter, new Bag<>());
                }

                charLocations.get(letter).add(pos);

                for (Cell c : getNeighbours(new Cell(i, j))) {
                    if (isValidCell(c, board.rows(), board.cols())) {
                        digraph.addEdge(pos, c.row * board.cols() + c.col);
                    }
                }
            }
        }

        // Board just contains the letter Q.
        // Don't DFS as this is the worst case for this BoggleSolver algorithm. Takes almost forever to finish
        if (boardFullOfQs) {
            StringBuilder sb = new StringBuilder("QUQU");
            for (String word : dictionary) {
                if (sb.toString().equals(word) && sb.length() <= (board.cols() * board.rows() * 2)) {
                    validWords.add(sb.toString());
                    sb.append("QU");
                }
            }

        } else {
            // Try and locate every word on the board
            for (String word : dictionary) {
                StringBuilder sb = new StringBuilder();
                char firstChar = word.charAt(0);

                if (charLocations.containsKey(firstChar)) {
                    sb.append(firstChar);
                    // Q is almost always followed by U
                    if (firstChar == 'Q') sb.append('U');
                    for (int location : charLocations.get(firstChar)) {
                        boolean[] visited = new boolean[board.rows() * board.cols()];
                        visited[location] = true;
                        // Skip the U on the word string
                        if (firstChar == 'Q') doDfs(word, 1, sb, visited, location);
                        else doDfs(word, 0, sb, visited, location);
                    }
                }
            }
        }

        return new SET<>(validWords);
    }


    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word.length() < 3 || Arrays.binarySearch(dictionary, word) < 0) return 0;

        switch (word.length()) {
            case 3:
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 3;
            case 7:
                return 5;
            default:
                return 11;
        }
    }

    private void doDfs(String word, int i, StringBuilder sb, boolean[] visited, int source) {
        if (sb.length() >= 3 && word.equals(sb.toString())) {
            validWords.add(word);
        }

        if (i >= word.length() - 1) return;

        char nextChar = word.charAt(i + 1);

        // If characters do not exist on board, don't bother
        if (!charLocations.containsKey(nextChar)) return;

        // Trace path from current char to next one in word
        for (int neighbour : digraph.adj(source)) {
            for (int nextCharLocation : charLocations.get(nextChar)) {
                if (neighbour == nextCharLocation && !visited[neighbour]) {
                    visited[neighbour] = true;
                    sb.append(nextChar);

                    // Q is almost always followed by U.
                    // Append the U and skip the next character on the string
                    if (nextChar == 'Q') {
                        sb.append('U');
                        doDfs(word, i + 2, sb, visited, neighbour);
                        // Delete the U appended earlier
                        sb.deleteCharAt(sb.length() - 1);
                    } else doDfs(word, i + 1, sb, visited, neighbour);
                    sb.deleteCharAt(sb.length() - 1);
                    visited[neighbour] = false;
                }
            }
        }
    }

    private Cell[] getNeighbours(Cell c) {
        return new Cell[] {
                // Horizontal and vertical neighbours
                new Cell(c.row - 1, c.col),
                new Cell(c.row + 1, c.col),
                new Cell(c.row, c.col - 1),
                new Cell(c.row, c.col + 1),
                // Diagonal neighbours
                new Cell(c.row - 1, c.col - 1),
                new Cell(c.row - 1, c.col + 1),
                new Cell(c.row + 1, c.col - 1),
                new Cell(c.row + 1, c.col + 1)
        };
    }

    private boolean isValidCell(Cell c, int rows, int cols) {
        return c.row >= 0 && c.row < rows && c.col >= 0 && c.col < cols;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}
