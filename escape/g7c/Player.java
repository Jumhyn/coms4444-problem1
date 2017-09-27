package escape.g7c;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Player implements escape.sim.Player {
    private boolean DEBUG = false;
    private double RESET_COEF = 2.5;

    private Random rand;
    private int turn;
    private int n;

    private int lastMove = -1;
    private int nextLastMove = -1;
    private ArrayList<Integer> moves;  // Represents the handles held in the previous turns. Zero-based.

    private int oddOwnedHandle = -1;
    private int evenOwnedHandle = -1;


    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        this.moves = new ArrayList<Integer>();

        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        int move = this.getMove(conflicts);
        this.nextLastMove = this.lastMove;
        this.lastMove = move;
        this.moves.add(move);
        this.turn++;
        if (DEBUG) {
            System.out.printf("oddOwnedHandle: %d, evenOwnedHandle: %d, moves: %s\n",
                oddOwnedHandle, evenOwnedHandle, moves);
        }
        return move + 1;
    }

    public int getMove(List<Integer> conflicts) {
        boolean isFirstTurn = this.turn == 0;
        boolean isSecondTurn = this.turn == 1;
        if (isFirstTurn) return 0;
        if (isSecondTurn) return this.n / 2;

        boolean isOddTurn = (this.turn % 2) != 0;

        /*
         * Security mechanism to avoid the deadlock that happens when the
         * only handle left in one round is the one the user owns in the
         * other, which is the one he is not allowed to come back to.
         * Reset this round ownership.
         */
        boolean hasHalfConverged = (this.oddOwnedHandle == -1) != (this.evenOwnedHandle == -1);
        if (hasHalfConverged && this.turn % (RESET_COEF*n) == 0) {
            this.oddOwnedHandle = -1;
            this.evenOwnedHandle = -1;
        }

        /*
         * If there are not conflicts, and the previous handle is not already
         * owned in the next turn, own it in this kind (even or odd) of turns.
         */
        boolean hasConflicted = conflicts.size() != 0;
        if (!hasConflicted) {
            if (isOddTurn) {
                if (this.oddOwnedHandle != this.lastMove)
                    this.evenOwnedHandle = this.lastMove;
            } else {
                if (this.evenOwnedHandle != this.lastMove)
                    this.oddOwnedHandle = this.lastMove;
            }
        }

        boolean hasOddOwnedHandle = this.oddOwnedHandle != -1;
        boolean hasEvenOwnedHandle = this.evenOwnedHandle != -1;
        if (isOddTurn) {
            if (hasOddOwnedHandle) {
                return this.oddOwnedHandle;
            } else if (hasEvenOwnedHandle) {
                return this.chooseNextExcluding(this.evenOwnedHandle, conflicts);
            }
        } else {
            if (hasEvenOwnedHandle) {
                return this.evenOwnedHandle;
            } else if (hasOddOwnedHandle) {
                return this.chooseNextExcluding(this.oddOwnedHandle, conflicts);
            }
        }
        return this.chooseNext(conflicts);
    }

    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts);
    }

    public int chooseRandomExcluding(int excluding, List<Integer> conflicts) {
        List<Integer> choices = new ArrayList<Integer>();
        for (int i = 0; i < this.n; i++) {
            choices.add(i);
        }
        if (conflicts.size() != 0) {
            choices.remove(new Integer(this.lastMove));
        }
        choices.remove(new Integer(excluding));
        int index = this.rand.nextInt(choices.size());
        return choices.get(index);
    }

    public int chooseNext(List<Integer> conflicts) {
        return this.chooseNextExcluding(-1, conflicts);
    }

    public int chooseNextExcluding(int excluding, List<Integer> conflicts) {
        boolean validMove;
        int nextMove;

        // Start the first odd movements in a random position. Also restart it
        // every (RESET_COEF * n)+1 turns, to avoid the same players to keep
        // conflicting.
        if (this.nextLastMove == -1 || this.turn % (RESET_COEF*n) == 1) {
            nextMove = this.chooseRandomExcluding(excluding, conflicts);
        } else {
            nextMove = this.nextLastMove;
        }

        List<Integer> invalidMoves = new ArrayList<Integer>();
        invalidMoves.add(excluding);
        invalidMoves.add(this.lastMove);

        do {
            nextMove = (nextMove+1) % this.n;
        } while (invalidMoves.contains(nextMove));
        return nextMove;
    }
}
