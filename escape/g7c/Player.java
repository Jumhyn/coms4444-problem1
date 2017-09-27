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
    private double[] weightsEven;
    private double[] weightsOdd;
    private double totalWeightOdd = 0.0;
    private double totalWeightEven = 0.0;
    private int ownedEven = -1;
    private int ownedOdd = -1;
	
    public Player() {
        this.rand = new Random();
    }

    public int init(int n) {
        this.turn = 0;
        this.n = n;
        weightsEven = new double[n];
        weightsOdd = new double[n];
        for (int i=0; i<n; ++i){
            totalWeightOdd += 100;
            totalWeightEven += 100;
            weightsEven[i] = 100;
            weightsOdd[i] = 100;
        }
        return attempt(null);
    }

    public int attempt(List<Integer> conflicts) {
        int move = this.getMove(conflicts);
        this.lastLastMove = this.lastMove;
        this.lastMove = move;
        this.turn++;
        return move + 1;
    }
    
    public int getMove(List<Integer> conflicts) {
        this.scaleWeights();
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
                lowerWeight = weightsOdd[this.lastLastMove]/totalWeightOdd > 0.8 ? (0.1 * weightsOdd[this.lastLastMove]) : (0.8 * weightsOdd[this.lastLastMove]);
                weightsOdd[this.lastLastMove] -= lowerWeight;
                totalWeightOdd -= lowerWeight;
                int randomHandle = this.chooseRandomExcluding(this.ownedEven, conflicts);

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
                lowerWeight = weightsEven[this.lastLastMove]/totalWeightEven > 0.8 ? (0.1 * weightsOdd[this.lastLastMove]) : (0.8 * weightsEven[this.lastLastMove]);
                weightsEven[this.lastLastMove] -= lowerWeight;
                totalWeightEven -= lowerWeight;
                int randomHandle = this.chooseRandomExcluding(this.ownedOdd,conflicts);

                if (this.ownedOdd != -1 && weightsEven[this.ownedOdd]/totalWeightEven > 0.9)
                    this.ownedOdd = -1;
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
        double random = Math.random() * weightMultiplier;
        
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

    public void scaleWeights(){
        if (this.turn%2==0){
            if (totalWeightEven < 1){
                totalWeightEven = 0;
                for (int i=0; i<weightsEven.length; ++i){
                    weightsEven[i] *= 100;
                    totalWeightEven += weightsEven[i];
                }
            }
        }
        else{
            if (totalWeightOdd < 1){
                totalWeightOdd = 0;
                for (int i=0; i<weightsOdd.length; ++i){
                    weightsOdd[i] *= 100;
                    totalWeightOdd += weightsOdd[i];
                }
            }
        }
    }
}
