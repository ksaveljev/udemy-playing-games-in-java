import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

interface NPuzzleSolver {
    public void solve(Board board);
}

abstract class SlidingPuzzleSolver implements NPuzzleSolver {
    /*
     * NPuzzle is solvable when
     * >>> zeroRow + numberOfInversions is even <<<
     * where zeroRow is the row number of empty tile (row index starts from 1)
     * where numberOfInversions is the amount of elements Ai and Aj such that i < j and Ai > Aj (Ai /= 0, Aj /= 0)
     */
    protected boolean isSolvable(Board board) {
        int zeroRow = 1 + Arrays.asList(board.getValues()).indexOf(0) + board.getDimension();
        int numberOfInversions = 0;

        int[] values = board.getValues();

        for (int i = 0, sz = values.length; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                if (values[i] == 0 || values[j] == 0) continue;

                if (i < j && values[i] > values[j]) numberOfInversions++;
            }
        }

        return (zeroRow + numberOfInversions) % 2 == 0;
    }
}

class DFSSolver extends SlidingPuzzleSolver {
    final static int MAX_DEPTH = 50;

    public void solve(Board board) {
    }
}

class BFSSolver extends SlidingPuzzleSolver {
    public void solve(Board board) {
    }
}

class AStarSolver extends SlidingPuzzleSolver {
    public void solve(Board board) {
    }
}

class IDAStarSolver extends SlidingPuzzleSolver {
    public void solve(Board board) {
    }
}

class Board {
    private final int dimension;
    private final int[] values;

    public Board(int dimension, int[] values) {
        this.dimension = dimension;
        this.values = values;
    }

    public int getDimension() {
        return dimension;
    }

    public int[] getValues() {
        return values;
    }
}

class NPuzzle {
    private Board board;
    private NPuzzleSolver solver;

    public NPuzzle(String fileName) {
        board = buildBoard(fileName);
        solver = new DFSSolver();
        //solver = new BFSSolver();
        //solver = new AStarSolver();
        //solver = new IDAStarSolver();
    }

    private Board buildBoard(String fileName) {
        String input = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(" ");
            }

            input = stringBuilder.toString();
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Integer dimension = 0;
        Scanner scanner = new Scanner(input);

        // TODO: error handling for incorrect input file
        dimension = scanner.nextInt();
        int[] values = new int[dimension * dimension];

        for (int i = 0, sz = dimension * dimension; i < sz; i++) {
            values[i] = scanner.nextInt();
        }

        return new Board(dimension, values);
    }

    public void solve() {
        solver.solve(board);
    }

    public static void main(String[] args) {
        assert(args[0] != null); // TODO: requires correct input handling
        new NPuzzle(args[0]).solve();
    }
}
