import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.TST;
import edu.princeton.cs.algs4.Queue;

public class BoggleSolver {
    private Digraph digraph;
    private SET<String> validWords;
    private final TST<Integer> trie = new TST<>();

    // Initializes the data structure using the given array of strings as the dictionary.
    // (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        for (int i = 0; i < dictionary.length; ++i) {
            trie.put(dictionary[i], i);
        }
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        digraph = new Digraph(board.rows() * board.cols());
        validWords = new SET<>();

        // Build digraph of a character and its neighbours
        for (int i = 0; i < board.rows(); ++i) {
            for (int j = 0; j < board.cols(); ++j) {
                int pos = i * board.cols() + j;

                for (int[] neighbour : getNeighbours(i, j)) {
                    if (isValidCell(neighbour[0], neighbour[1], board.rows(), board.cols())) {
                        digraph.addEdge(pos, neighbour[0] * board.cols() + neighbour[1]);
                    }
                }
            }
        }

        // Loop through every cell on board and build possible words
        for (int i = 0; i < board.rows(); ++i) {
            for (int j = 0; j < board.cols(); ++j) {
                StringBuilder sb = new StringBuilder();
                boolean[] visited = new boolean[board.rows() * board.cols()];
                sb.append(board.getLetter(i, j));
                // Q is almost always followed by U
                if (board.getLetter(i, j) == 'Q') sb.append('U');
                visited[i * board.cols() + j] = true;
                doDfs(board, sb, visited, new int[]{i, j}, board.cols());
            }
        }

        return new SET<>(validWords);
    }


    // Returns the score of the given word if it is in the dictionary, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (word.length() < 3 || !trie.contains(word)) return 0;

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

    private void doDfs(BoggleBoard board, StringBuilder sb, boolean[] visited, int[] cell, int cols) {
        if (sb.length() >= 3) {
            Queue<String> matchingPrefixes = (Queue<String>) trie.keysWithPrefix(sb.toString());
            if (matchingPrefixes.isEmpty()) return;
            if (trie.contains(sb.toString())) validWords.add(sb.toString());
        }

        int current = cell[0] * cols + cell[1];

        for (int neighbour : digraph.adj(current)) {
            if (!visited[neighbour]) {
                visited[neighbour] = true;
                int i = neighbour / cols;
                int j = neighbour - (i * cols);
                char letter = board.getLetter(i, j);
                sb.append(board.getLetter(i, j));
                // Q is almost always followed by U
                if (letter == 'Q') sb.append('U');
                doDfs(board, sb, visited, new int[]{i, j}, cols);
                // Delete extra U appended when Q was encountered
                if (letter == 'Q') sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                visited[neighbour] = false;
            }
        }
    }

    private int[][] getNeighbours(int row, int col) {
        return new int[][] {
                // Horizontal and vertical neighbours
                new int[]{row - 1, col},
                new int[]{row + 1, col},
                new int[]{row, col - 1},
                new int[]{row, col + 1},
                // Diagonal neighbours
                new int[]{row - 1, col - 1},
                new int[]{row - 1, col + 1},
                new int[]{row + 1, col - 1},
                new int[]{row + 1, col + 1}
        };
    }

    private boolean isValidCell(int i, int j, int rows, int cols) {
        return i >= 0 && i < rows && j >= 0 && j < cols;
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
