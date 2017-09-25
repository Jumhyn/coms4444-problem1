package escape.g7c;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Player implements escape.sim.Player {
    private boolean DEBUG = false;
    private double RESET_COEF = 2.5;

    private Random rand;
    private int turn;
    private int n;
    
    private int lastMove = -1;
    private int nextLastMove = -1;
    private int lastEvenMove = -1;
    private int lastOddMove = -1;
    private int turnsSinceReset;
    private int evenStart;
    private int oddStart;
    private ArrayList<Integer> moves;  // Represents the handles held in the previous turns. Zero-based.
    private int seenPlayersEven[];
    private int seenPlayersOdd[];

    private int oddOwnedHandle = -1;
    private int evenOwnedHandle = -1;


    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.turnsSinceReset = 0;
        this.n = n;
        this.moves = new ArrayList<Integer>();
        this.evenStart = 0;
        this.oddStart = n / 2;
        this.seenPlayersEven = new int[n];
        Arrays.fill(this.seenPlayersEven, -1);
        this.seenPlayersOdd = new int[n];
        Arrays.fill(this.seenPlayersOdd, -1);

        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        if (conflicts != null) {
            this.recordResults(conflicts);
        }
        int move = this.getMove(conflicts);
        this.nextLastMove = this.lastMove;
        this.lastMove = move;
        if (this.turn % 2 == 0) {
            this.lastEvenMove = move;
        } else {
            this.lastOddMove = move;
        }
        this.moves.add(move);
        this.turn++;
        if (DEBUG) {
            System.out.printf("oddOwnedHandle: %d, evenOwnedHandle: %d, moves: %s\n",
                oddOwnedHandle, evenOwnedHandle, moves);
        }
        return move + 1;
    }
    
    public void recordResults(List<Integer> conflicts) {
        int turn = this.turn - 1;
        int move = this.lastMove;
        /*
         * If there are not conflicts, and the previous handle is not already
         * owned in the next turn, own it in this kind (even or odd) of turns.
         */
        if (conflicts.size() == 0) {
            if (turn % 2 == 0) {
                this.evenOwnedHandle = move;
            } else {
                this.oddOwnedHandle = move;
            }
        } else if (conflicts.size() == 1) {
            int other = conflicts.get(0) - 1;
            // If we are just conflicting with one other player, we
            // check to see if we have done that previously during the
            // current 'round robin' of the handles. If so, we know
            // that one of those handles must be free, so we restart the
            // current 'round robin' at a random spot in hopes that one
            // each will find the two spots.
            if (turn % 2 == 1 && this.oddOwnedHandle == -1) {
                if (this.seenPlayersOdd[other] != -1) {
                    this.lastOddMove = this.oddStart + rand.nextInt(move - this.oddStart) - 1;
                    this.lastEvenMove = (this.lastOddMove + (this.n / 2)) % this.n;
                    Arrays.fill(this.seenPlayersEven, -1);
                    Arrays.fill(this.seenPlayersOdd, -1);
                    this.turnsSinceReset = 0;
                } else {
                    // If we havent seen the player before, just record where
                    // we saw them
                    this.seenPlayersOdd[other] = move;
                }
            } else if (turn % 2 == 0 && this.evenOwnedHandle == -1) {
                if (this.seenPlayersEven[other] != -1) {
                    this.lastEvenMove = this.evenStart + rand.nextInt(move - this.evenStart) - 1;
                    this.lastOddMove = (this.lastEvenMove - (this.n / 2)) % this.n;
                    Arrays.fill(this.seenPlayersEven, -1);
                    Arrays.fill(this.seenPlayersOdd, -1);
                    this.turnsSinceReset = 0;
                } else {
                    this.seenPlayersEven[other] = move;
                }
            }
        }
        
        // If we have made it all the way back to where we started, then
        // we must have conflicted with players who also do not own
        // handles. In this case, start at a random point so that the
        // players we overlap with on each handle will be different
        if (this.turnsSinceReset > 0 && this.turnsSinceReset % this.n == 0) {
            this.lastOddMove = rand.nextInt(this.n);
            this.lastEvenMove = (this.lastOddMove + (this.n / 2)) % this.n;
            this.turnsSinceReset = 0;
        }
        
        // Finally, deadlock prevention. If we own a handle on one round
        // and havent converged, give up ownership and try to converge
        // again. I think there is probably something more intelligent we
        // could do here...
        if ((this.oddOwnedHandle == -1) != (this.evenOwnedHandle == -1) && turn > 0 && turn % (int) (this.n * RESET_COEF) == 0) {
            this.oddOwnedHandle = -1;
            this.oddOwnedHandle = -1;
        }
        this.turnsSinceReset++;
    }

    public int getMove(List<Integer> conflicts) {
        if (this.turn == 0) {
            return evenStart;
        }
        
        if (this.turn == 1) {
            return oddStart;
        }
        
        /* If we own a handle for either parity, just do that */
        if (this.turn % 2 == 1) {
            if (this.oddOwnedHandle != -1) {
                return this.oddOwnedHandle;
            } else if (this.evenOwnedHandle != -1) {
                return this.chooseNextExcluding(this.evenOwnedHandle, conflicts);
            }
        } else {
            if (this.evenOwnedHandle != -1) {
                return this.evenOwnedHandle;
            } else if (this.oddOwnedHandle != -1) {
                return this.chooseNextExcluding(this.oddOwnedHandle, conflicts);
            }
        }
        
        /* Otherwise, just go down the line... */
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
        int move = (this.nextLastMove + 1) % this.n;
        if (move == this.lastMove && conflicts.size() != 0) {
            move = (move + 1) % n;
        }
        return move;
    }

    public int chooseNextExcluding(int excluding, List<Integer> conflicts) {
        boolean validMove;
        int nextMove = this.nextLastMove;
        boolean hasConflicted = conflicts.size() != 0;

        List<Integer> invalidMoves = new ArrayList<Integer>();
        invalidMoves.add(excluding);
        invalidMoves.add(this.lastMove);

        do {
            nextMove = (nextMove+1) % this.n;
        } while (invalidMoves.contains(nextMove));
        return nextMove;
    }
}
