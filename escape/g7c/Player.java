package escape.g7c;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.*;
import java.lang.Integer;

public class Player implements escape.sim.Player {
    private Random rand;
	
    private int turn;
    private int n;
    private int lastMove;
    private int lastLastMove;
    private int ownedHandle = -1;
    private ArrayList<Integer> moves;  // Represents the handles held in the previous turns. Zero-based.
    private ArrayList<List<Integer>> conflictsPerRound;
    private double[] weightsEven;
    private double[] weightsOdd;
    private double totalWeightOdd = 0.0;
    private double totalWeightEven = 0.0;
    private int ownedEven = -1;
    private int ownedOdd = -1;
    private double tieBreakerValue = 0;
    private double turnLimit = 0;
    //private Map<Integer,Integer> conflictsEven = new HashMap<>();
    //private Map<Integer,Integer> conflictsOdd = new HashMap<>();
    private int forceExclude = -1;
    private int[][] conflictsEven;
    private int[][] conflictsOdd;
	
    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        this.conflictsPerRound = new ArrayList<List<Integer>>();
        this.moves = new ArrayList<Integer>();
        weightsEven = new double[n];
        weightsOdd = new double[n];
        for (int i=0; i<n; ++i){
            totalWeightOdd += 100;
            totalWeightEven += 100;
            weightsEven[i] = 100;
            weightsOdd[i] = 100;
        }
        turnLimit = Math.ceil(this.n*1.8);
        conflictsEven = new int[this.n][this.n];
        conflictsOdd = new int[this.n][this.n];
        //System.out.println(conflictsEven[0][1]);
        //System.out.println(conflictsOdd[4][5]);
        //System.exit(1);
        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        int move = this.getMove(conflicts);
        this.lastLastMove = this.lastMove;
        this.lastMove = move;
        this.moves.add(move);
        this.turn++;
        return move + 1;
    }
    
    public int getMove(List<Integer> conflicts) {
        /*if (conflicts!=null){
            for (int t=0; t<conflicts.size(); ++t)
                conflictsOdd.compute(this.lastMove, (k, v) -> v==null ? 1 : v+1);//Oddconflicts.get(i)
        }*/
        System.out.println("conflictsEven: " + Arrays.deepToString(conflictsEven));
        if (conflicts!=null){
            //System.out.println(conflicts.toString());
            if (this.turn%2==0){
                for (int t=0; t<conflicts.size(); ++t){
                    conflictsEven[this.lastMove][conflicts.get(t)-1] += 1;
                }
            }
            else{
                for (int t=0; t<conflicts.size(); ++t){
                    conflictsOdd[this.lastMove][conflicts.get(t)-1] += 1;
                }
            }
        }
                

        if (this.turn == 0) 
            return 0;
         
        else if (this.turn == 1){
            if (conflicts.size() == 0) 
                this.ownedEven = this.lastMove; 
            return 1;
        }
        
        else if (this.turn == 2){
            if (conflicts.size() == 0) 
                this.ownedOdd = this.lastMove; 
            if (this.ownedEven != -1) 
                return this.ownedEven; 

            else
                return this.chooseRandomExcluding(this.ownedOdd, conflicts);
        }

        else if ((this.turn % 2) != 0) { //odd turns
            if (conflicts.size() == 0 && this.ownedEven == -1) {
                this.ownedEven = this.lastMove;
            }
            if (this.ownedOdd != -1) { 
                return this.ownedOdd;
            } 
            
            else {
                double lowerWeight = 0.0;
                lowerWeight = Math.floor(0.8 * weightsOdd[this.lastLastMove]);
                weightsOdd[this.lastLastMove] -= lowerWeight;
                totalWeightOdd -= lowerWeight;
                //int randomHandle = this.chooseRandom(conflicts);
                int randomHandle = this.chooseRandomExcluding(this.ownedEven, conflicts);

                System.out.println("weightsEven: " + Arrays.toString(weightsEven));
                System.out.println("weightsOdd: " + Arrays.toString(weightsOdd));
                return randomHandle;
            }
        } 
        else { //even turns
            if (conflicts.size() == 0){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1) {
                return this.ownedEven;
            } 
            
            else {
                double lowerWeight = 0.0;
                lowerWeight = Math.floor(0.8 * weightsEven[this.lastLastMove]);
                weightsEven[this.lastLastMove] -= lowerWeight;
                totalWeightEven -= lowerWeight;
                //int randomHandle = this.chooseRandom(conflicts);
                int randomHandle = this.chooseRandomExcluding(this.ownedOdd,conflicts);

                System.out.println("weightsEven: " + Arrays.toString(weightsEven));
                System.out.println("weightsOdd: " + Arrays.toString(weightsOdd));
                return randomHandle;
            }
        }
    }
    
    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts);
    }
    
    public int chooseRandomExcluding(int excluding, List<Integer> conflicts) {
        int randomNum = this.weightedRandom(excluding, conflicts);
        return randomNum;
    }

    public int weightedRandom(int excluding, List<Integer> conflicts){
        int randomIndex = -1;
        double tempTotalWeightOdd = totalWeightOdd;
        double tempTotalWeightEven = totalWeightEven;
        double tempExcluding = 0;
        double tempConflict = 0;
        /*if (this.turn > (this.n*1.5+10)){
            System.out.println("okokok");
            if (this.ownedOdd != -1 && this.ownedEven == -1){
                if (this.n*1.5+10%2 == 0)
                    ;
                else if (this.turn%2!=0){
                    return this.randExcludeOwned(this.ownedOdd, conflicts);
                }
                else{
                    if (conflicts.size()==0)
                        return this.ownedOdd;
                    else if (conflicts.size()!=0 && this.lastMove != this.ownedOdd)
                        return this.ownedOdd;
                    else
                        return this.randExcludeOwned(this.ownedOdd, conflicts);
                }
            }
            else if (this.ownedOdd == -1 && this.ownedEven != -1){
                ;}

        }
        else if (this.turn <= (this.n*1.5+10)){
            if (this.turn %2 != 0){
                if (this.ownedOdd != -1)
                    return this.ownedOdd;
            }
            else{
                if (this.ownedEven != -1)
                    return this.ownedEven;
            }
        }*/
        if (conflicts.size() != 0){
            if ((this.turn % 2) != 0){
                tempConflict = weightsOdd[this.lastMove];
                weightsOdd[this.lastMove] = 0;
                tempTotalWeightOdd -= tempConflict;
            }
            else{
                tempConflict = weightsEven[this.lastMove];
                weightsEven[this.lastMove] = 0;
                tempTotalWeightEven -= tempConflict;
            }
        }

        if (excluding != -1){
            if (this.turn %2 ==0){
                if (this.ownedOdd != -1){
                    tempExcluding = weightsEven[excluding];
                    weightsEven[excluding] = 0;
                    tempTotalWeightEven -= tempExcluding;
                }
            }
            else{
                if (this.ownedEven != -1){
                    tempExcluding = weightsOdd[excluding];
                    weightsOdd[excluding] = 0;
                    tempTotalWeightOdd -= tempExcluding;
                }
            }
        }


        double weightMultiplier = ((this.turn%2) == 0) ? tempTotalWeightEven : tempTotalWeightOdd;
        System.out.println("weightMult: " + weightMultiplier);
        double random = Math.random() * weightMultiplier;
        
        for (int i=0; i<this.n; ++i){
            if ((this.turn % 2) != 0){
                random -= weightsOdd[i];
                //System.out.println("[" + i + "]: " + random);
                if (random <= 0.0d){
                    randomIndex = i;
                    break;
                }
            }
            else{
                random -= weightsEven[i];
                if (random <= 0.0d){
                    randomIndex = i;
                    break;
                }
            }
        }
        if (tempExcluding!=0){
            if ((this.turn % 2) != 0){
                weightsOdd[excluding] = tempExcluding;
            }
            else{
                weightsEven[excluding] = tempExcluding;
            }
        }
        if (tempConflict!=0){
            if ((this.turn % 2) != 0){
                weightsOdd[this.lastMove] = tempConflict;
            }
            else{
                weightsEven[this.lastMove] = tempConflict;
            }
        }
        System.out.println("randomindex: :" + randomIndex);
        return randomIndex;
    }

    public int randExcludeOwned(int excluding, List<Integer> conflicts){
        int retval = this.rand.nextInt(this.n);
        int conflict = -1;
        if (conflicts.size()!=0)
            conflict = this.lastMove;
        while (retval == excluding || conflict != -1){
            retval = this.rand.nextInt(this.n);
        }
        return retval;
    }
        
}
