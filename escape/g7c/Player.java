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
    private int nextLastMove;
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
    private Map<Integer,Integer> conflictsEven = new HashMap<>();
    private Map<Integer,Integer> conflictsOdd = new HashMap<>();
	
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
            //conflictsOdd.put(new Integer(i), new Integer(0));
            //conflictsEven.put(new Integer(i), new Integer(0));
        }
        tieBreakerValue = 0.9 + (0.1-0.9)*(Math.log(0.33)-Math.log(1.0/n))/(Math.log(0.33)-Math.log(0.0005));
        System.out.println("tiebreaker:: " + tieBreakerValue);
        double temptemp = 1.0/this.n;
        turnLimit = Math.ceil(this.n*1.8);
        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        int move = this.getMove(conflicts);
        this.nextLastMove = this.lastMove;
        this.lastMove = move;
        this.moves.add(move);
        this.turn++;
        return move + 1;
    }
    
    public int getMove(List<Integer> conflicts) {
        //System.out.println("conflicts: " + conflicts);
        System.out.println(conflictsOdd);
        /*if (conflicts.size()==0 && this.turn>1){
            System.out.println(conflicts);
            ;
        }*/
        if (this.turn % 2 == 0 && conflicts!=null){
            if (conflicts.size()==0){
            }
            else{
                for (int t=0; t<conflicts.size(); ++t){
                    conflictsOdd.compute(this.lastMove, (k, v) -> v==null ? 1 : v+1);//Oddconflicts.get(i)
                    ;
                }
            }
        }
        if (this.turn % (this.n*2+2) == 0){
            if (this.turn%2 != 0) this.ownedEven = -1;
            else this.ownedOdd = -1;
        }
        if (this.turn == 0) {
            return 0;
        } 
        else if (this.turn == 1){
            if (conflicts.size() == 0){
                this.ownedEven = this.lastMove;
            }
            return 1;
        }
        
        else if (this.turn == 2){
            if (conflicts.size() == 0){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1){
                return this.ownedEven;
            }
            else{
                return this.chooseRandomExcluding(this.ownedOdd, conflicts);
            }
        }

        else if ((this.turn % 2) != 0) { //odd turns
            if (conflicts.size() == 0 && this.ownedEven == -1) {
                this.ownedEven = this.lastMove;
            }
            if (this.ownedOdd != -1) {
                if (conflicts.size()==0)
                    return this.ownedOdd;
                else{
                    if (this.lastMove != this.ownedOdd)
                        return this.ownedOdd;
                    else
                        return this.chooseRandomExcluding(this.ownedEven, conflicts);
                    
                }
            } else {
                System.out.println("ownedOdd: " + this.ownedOdd);
                //double percentage = (weightsOdd[this.nextLastMove]/totalWeight);
                double percentage = 0.8;
                //double lowerWeight = weightsOdd[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weightsOdd[this.nextLastMove];
                double lowerWeight = 0.0;
                if (false){//(this.turn >= turnLimit){
                    if (weightsOdd[this.nextLastMove] > 5){
                        lowerWeight = weightsOdd[this.nextLastMove] - 5;
                    }
                    weightsOdd[this.nextLastMove] -= lowerWeight;
                    weightsOdd[(this.nextLastMove+1)%(this.n-1)] += lowerWeight;
                }
                else{
                    lowerWeight = Math.floor(0.5 * weightsOdd[this.nextLastMove]);
                    weightsOdd[this.nextLastMove] -= lowerWeight;
                    totalWeightOdd -= lowerWeight;
                    /*if (weightsOdd[this.nextLastMove] < 1.0){ ; }
                    else if (weightsOdd[this.nextLastMove]<10 && weightsOdd[this.nextLastMove] >= 1){
                        lowerWeight = weightsOdd[this.nextLastMove] - 1.0;
                    }
                    else if (weightsOdd[this.nextLastMove]/totalWeight > 200.0/totalWeight){
                        lowerWeight = percentage/4.0 * weightsOdd[this.nextLastMove];
                    }
                    else{
                        lowerWeight = percentage * weightsOdd[this.nextLastMove];
                    }
                    double tempTotalWeight = totalWeight - weightsOdd[this.nextLastMove];
                    double adjustLimit = weightsOdd[this.nextLastMove] / 100;
                    System.out.println("lowerWeightOdd: " + lowerWeight);
                    if (true){//(adjustLimit > 0.05){
                        for (int i=0; i<weightsOdd.length; ++i){
                            if (i!=this.nextLastMove){
                                weightsOdd[i] += lowerWeight * (weightsOdd[i]/tempTotalWeight);
                            }
                            else{
                                weightsOdd[i] -= lowerWeight;
                            }
                        }
                    }*/
                }
                //System.out.println("weightsOdd: " + Arrays.toString(weightsOdd));
                //System.out.println("weightsEven: " + Arrays.toString(weightsEven) + "\n");
                //return this.chooseRandomExcluding(this.ownedEven, conflicts);
                int randomHandle = this.chooseRandomExcluding(-1, conflicts);
                if (this.ownedEven != -1 && randomHandle == this.ownedEven)
                    ;
                    //this.ownedEven = -1;
                return randomHandle;
            }
        } 
        else { //even turns
            if (conflicts.size() == 0 && this.ownedOdd == -1){
                this.ownedOdd = this.lastMove;
            }
            if (this.ownedEven != -1) {
                if (conflicts.size()==0){
                    return this.ownedEven;
                }
                else{
                    if (this.lastMove != this.ownedEven)
                        return this.ownedEven;
                    else
                        return this.chooseRandomExcluding(-1, conflicts);
                }
            } else {
                System.out.println("ownedEven: " + this.ownedEven);
                //double percentage = (weightsEven[this.nextLastMove]/totalWeight);
                double percentage = 0.8;
                double lowerWeight = 0.0;
                if (false){//(this.turn >= turnLimit){
                    if (weightsEven[this.nextLastMove] > 5){
                        lowerWeight = weightsEven[this.nextLastMove] - 5;
                    }
                    weightsEven[this.nextLastMove] -= lowerWeight;
                    weightsEven[(this.nextLastMove+1)%(this.n-1)] += lowerWeight;
                }

                else{
                    lowerWeight = Math.floor(0.5 * weightsEven[this.nextLastMove]);
                    weightsEven[this.nextLastMove] -= lowerWeight;
                    totalWeightEven -= lowerWeight;
                    /*if (weightsEven[this.nextLastMove] < 1.0){ ; }
                    else if (weightsEven[this.nextLastMove] < 10 && weightsEven[this.nextLastMove] >= 1){
                        lowerWeight = weightsEven[this.nextLastMove] - 1;
                    }
                    else if (weightsEven[this.nextLastMove]/totalWeight > 200.0/totalWeight){
                        lowerWeight = percentage/2.0 * weightsEven[this.nextLastMove];
                    }
                    else{
                        lowerWeight = percentage * weightsEven[this.nextLastMove];
                    }
                    //double lowerWeight = weightsEven[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weightsEven[this.nextLastMove];
                    //double lowerWeight = weightsEven[this.nextLastMove] - Math.exp(-percentage*4*(this.n/5)) * weightsEven[this.nextLastMove];
                    double tempTotalWeight = totalWeight - weightsEven[this.nextLastMove];
                    double adjustLimit = weightsEven[this.nextLastMove] / 100;
                    System.out.println("lowerWeightEven: " + lowerWeight);
                    if (true){;//(adjustLimit > 0.05){
                        for (int i=0; i<weightsEven.length; ++i){
                            if (i!=this.nextLastMove){
                                weightsEven[i] += lowerWeight * (weightsEven[i]/tempTotalWeight);
                            }
                            else{
                                weightsEven[i] -= lowerWeight;
                            }
                        }
                    }*/
                }


                //System.out.println("weightsEven: " + Arrays.toString(weightsEven));
                //System.out.println("weightsOdd: " + Arrays.toString(weightsOdd) + "\n");
                int randomHandle;
                //randomHandle = this.chooseRandomExcluding(this.ownedOdd,conflicts);
                randomHandle = this.chooseRandomExcluding(-1,conflicts);
                if (this.ownedOdd != -1 && randomHandle == this.ownedOdd)
                    ;
                    //this.ownedOdd = -1;
                return randomHandle;
            }
        }
    }
    
    public int chooseRandom(List<Integer> conflicts) {
        return this.chooseRandomExcluding(-1, conflicts);
    }
    
    public int chooseRandomExcluding(int excluding, List<Integer> conflicts) {
        boolean avoidLast = conflicts.size() == 0;

        int randomNum = this.weightedRandom(excluding, conflicts);
        return randomNum;
    }

    public int weightedRandom(int excluding, List<Integer> conflicts){
        //System.out.println("tiebreaker: " + tieBreakerValue);
        int randomIndex = -1;
        double tempTotalWeightOdd = totalWeightOdd;
        double tempTotalWeightEven = totalWeightEven;
        double tempExcluding = 0;
        double tempConflict = 0;
        if (excluding>-1){
            System.out.println("excluding :" + excluding);
            if ((this.turn % 2) != 0){
                //if (false){
                if (weightsOdd[excluding]/totalWeightOdd > tieBreakerValue && conflicts.size()==0 && this.turn>this.n*1.6 && this.turn % 3 == 0){
                    System.out.println("weightsOddBig: " + weightsOdd[excluding]);
                    return excluding;
                }
                tempExcluding = weightsOdd[excluding];
                weightsOdd[excluding] = 0;
                tempTotalWeightOdd -= tempExcluding;
            }
            else{
                //if (false){
                if (weightsEven[excluding]/totalWeightEven > tieBreakerValue && conflicts.size()==0 && this.turn>this.n*1.6 && this.turn %3 == 0){
                    System.out.println("weightsEvenBig: " + weightsEven[excluding]);
                    return excluding;
                }
                tempExcluding = weightsEven[excluding];
                weightsEven[excluding] = 0;
                tempTotalWeightEven -= tempExcluding;
            }
            //tempTotalWeight -= tempExcluding;
        }

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
            //tempTotalWeight -= tempConflict;
        }

        double random = Math.random() * this.turn%2==0 ? tempTotalWeightEven : tempTotalWeightOdd;
        
        for (int i=0; i<this.n; ++i){
            if ((this.turn % 2) != 0){
                random -= weightsOdd[i];
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

        return randomIndex;
    }
        
}
