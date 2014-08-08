import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class MinMaxAlphaBetaPlayer extends PrintablePlayer {
    private final static int DEPTH_LIMIT = 10;
    private final DummyPlayer ourDummyPlayer;
    private final DummyPlayer opponentDummyPlayer;
    private final Player[] dummyPlayers;

    private class DummyPlayer extends PrintablePlayer {
        public DummyPlayer(char representation) {
            super(representation);
        }

        public PlayerMove suggestMove(ConnectFourGame game) {
            return null;
        }
    }

    public MinMaxAlphaBetaPlayer() {
        this('O');
    }

    public MinMaxAlphaBetaPlayer(char representation) {
        super(representation);
        ourDummyPlayer = new DummyPlayer('1');
        opponentDummyPlayer = new DummyPlayer('2');
        dummyPlayers = new Player[] {ourDummyPlayer, opponentDummyPlayer};
    }

    public PlayerMove suggestMove(ConnectFourGame game) {
        List<PlayerMove> initialBoardMoves = prepareMovesForSimulation(game.getMoves());

        ConnectFourGame simulatedGame = new ConnectFourGame(initialBoardMoves);
        List<PlayerMove> possibleMoves = simulatedGame.getPossibleMoves(ourDummyPlayer);

        int bestResult = Integer.MIN_VALUE;
        List<PlayerMove> candidateMoves = new ArrayList<PlayerMove>();

        for (PlayerMove possibleMove : possibleMoves) {
            int result = alphaBeta(simulatedGame, possibleMove, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            System.out.println("move to column " + possibleMove.getColumnIndex() + " is worth " + result);

            if (result > bestResult) {
                candidateMoves.clear();
                bestResult = result;
                candidateMoves.add(possibleMove);
            } else if (result == bestResult) {
                candidateMoves.add(possibleMove);
            }
        }

        if (candidateMoves.size() > 1) {
            Collections.shuffle(candidateMoves);
        }

        return new PlayerMove(this, candidateMoves.get(0).getColumnIndex());
    }

    private int alphaBeta(ConnectFourGame simulatedGame, PlayerMove playerMove, int playerIndex, int depth, int alpha, int beta) {
        if (simulatedGame.isWinningMove(playerMove)) {
            if (dummyPlayers[playerIndex] == ourDummyPlayer) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        }

        if (depth == DEPTH_LIMIT) {
            return heuristic(simulatedGame, playerMove);
        }

        simulatedGame.makeMove(playerMove);

        int nextPlayerIndex = (playerIndex + 1) % 2;
        List<PlayerMove> possibleMoves = simulatedGame.getPossibleMoves(dummyPlayers[nextPlayerIndex]);
        Collections.shuffle(possibleMoves);

        if (dummyPlayers[nextPlayerIndex] == ourDummyPlayer) {
            for (PlayerMove possibleMove : possibleMoves) {
                alpha = Math.max(alpha, alphaBeta(simulatedGame, possibleMove, nextPlayerIndex, depth + 1, alpha, beta));

                if (beta <= alpha) {
                    break;
                }
            }

            simulatedGame.undoMove();

            return alpha;
        } else {
            for (PlayerMove possibleMove : possibleMoves) {
                beta = Math.min(beta, alphaBeta(simulatedGame, possibleMove, nextPlayerIndex, depth + 1, alpha, beta));

                if (beta <= alpha) {
                    break;
                }
            }

            simulatedGame.undoMove();

            return beta;
        }
    }

    private int heuristic(ConnectFourGame game, PlayerMove playerMove) {
        int value = 0;
        int count = 0;
        Player[][] board = game.getBoard();
        
        int moveRow = game.makeMove(playerMove);
        int moveCol = playerMove.getColumnIndex();

        // row heuristic
        for (int col = moveCol - 3; col <= moveCol; col++) {
            if (col < 0 || col + 3 >= ConnectFourGame.NUMBER_OF_COLUMNS) continue;

            boolean ok = true;
            count = 0;

            for (int i = 0; i < 4; i++) {
                if (board[moveRow][col + i] != playerMove.getPlayer() && board[moveRow][col + i] != null) {
                    ok = false;
                    break;
                }

                if (board[moveRow][col + i] == playerMove.getPlayer()) {
                    count++;
                }
            }

            if (!ok) continue;

            value += countToValue(count);
        }

        // column heuristic
        count = 0;
        for (int row = moveRow; row >= 0; row--) {
            if (board[row][moveCol] != playerMove.getPlayer()) break;
            count++;
        }

        // only if we can actually get 4 in this column
        if (moveRow + (4 - count) < ConnectFourGame.NUMBER_OF_ROWS) {
            value += countToValue(count);
        }

        // left diagonal heuristic
        for (int row = moveRow - 3, col = moveCol - 3; row <= moveRow && col <= moveCol; row++, col++) {
            if (row < 0 || row + 3 >= ConnectFourGame.NUMBER_OF_ROWS || col < 0 || col + 3 >= ConnectFourGame.NUMBER_OF_COLUMNS) continue;

            boolean ok = true;
            count = 0;

            for (int i = 0; i < 4; i++) {
                if (board[row + i][col + i] != playerMove.getPlayer() && board[row + i][col + i] != null) {
                    ok = false;
                    break;
                }

                if (board[row + i][col + i] == playerMove.getPlayer()) {
                    count++;
                }
            }

            if (!ok) continue;

            value += countToValue(count);
        }

        // right diagonal heuristic
        for (int row = moveRow + 3, col = moveCol - 3; row >= moveRow && col <= moveCol; row--, col++) {
            if (row >= ConnectFourGame.NUMBER_OF_ROWS || row - 3 < 0 || col < 0 || col + 3 >= ConnectFourGame.NUMBER_OF_COLUMNS) continue;

            boolean ok = true;
            count = 0;

            for (int i = 0; i < 4; i++) {
                if (board[row - i][col + i] != playerMove.getPlayer() && board[row - i][col + i] != null) {
                    ok = false;
                    break;
                }

                if (board[row - i][col + i] == playerMove.getPlayer()) {
                    count++;
                }
            }

            if (!ok) continue;

            value += countToValue(count);
        }

        game.undoMove();

        return value;
    }

    private int countToValue(int count) {
        int value = 0;

        if (count == 1) {
            value = 1;
        } else if (count == 2) {
            value = 4;
        } else if (count == 3) {
            value = 32;
        }

        return value;
    }

    private List<PlayerMove> prepareMovesForSimulation(List<PlayerMove> moves) {
        List<PlayerMove> simulatedMoves = new ArrayList<PlayerMove>(moves.size());

        for (PlayerMove playerMove : moves) {
            if (playerMove.getPlayer() == this) {
                simulatedMoves.add(new PlayerMove(ourDummyPlayer, playerMove.getColumnIndex()));
            } else {
                simulatedMoves.add(new PlayerMove(opponentDummyPlayer, playerMove.getColumnIndex()));
            }
        }

        return simulatedMoves;
    }
}