package escape.g7_ananth;
//org.apache.commons.math3.distribution.*;
import java.util.List;
import java.util.*;
import java.util.Random;
import java.io.*;
import javafx.util.*;


public class Player implements escape.sim.Player {
	private Random rand;
	private int n = 0;
	private int lastOddMove= -1;
	private int lastEvenMove=-1;
	private int lastMove = -1;
	private int turn=-1;
	private int empty_handles;


	private Vector<Integer> prob;
	private Vector<Integer> inverse_prob;
	private int total_weights;
	private Vector<Vector<Integer> >  estimates;


	private int found = -1;
	private Vector<Integer> handle_owned;
	private Vector<Integer> handle_owner;

	private Vector<Boolean> is_taken;
	private Vector<Boolean> has_handle;


	private double stubborness = 0;
	private double eps = 0.17;
	private double mn = 0.000005;
	private int seen_enough = 6;
	HashMap <Pair<Integer,Integer>,Integer> hm = new <Pair<Integer,Integer>,Integer> HashMap();
	//Vector<Integer> double_check = new V
	public Player() {
		rand = new Random();
		//prob = new Vector<Double>();
	}
	public void Normalize(Vector<Integer> dist)
	{

	}
	public void updateProb(List<Integer> conflicts)
	{

	}
	public void updateProbWithEstimates()
	{

	}
	public void updateEstimates(List<Integer> conflicts)
	{

	}
	public int getNonConflictMove(Vector<Integer>  prob)
	{
		int tmp = getRandomTurn(prob);
		while(tmp == lastMove || handle_owner[tmp]!=-1)
			tmp = getRandomTurn(prob);
		return tmp;
	}

	public int getLinearNonConflictMove()
	{
		int tmp = (lastMove+1)%n;
		while(tmp == lastMove || handle_owner[tmp]!=-1)
			tmp = (lastMove+1)%n;
		return tmp;
	}


	public int getRandomTurn(Vector<Integer> prob)
	{
	    int d = rand.nextInt(total_weights)+1;
	    int sum = 0;
	    for(int i= 0;i<n;i++)
	    {
	        sum+=prob.get(i);
	        if(d<=sum)
	        {
	            return i;
	        }

	    }

	    return n-1;
	}
	public int getAway() //Change this to as noncflicitng a move as possible
	{
		//Vector<Double> inverse_prob = new Vector<Double> (Collections.nCopies(n,0.0));
		for(int i = 0;i< n;i++)
		{
			inverse_prob.set(i,prob.get(i));

		}
		//Normalize(inverse_prob);
		return getNonConflictMove(inverse_prob);
	}
	public void initVectors()
	{
			prob = new Vector<Integer>(Collections.nCopies(this.n,(Integer)1000));
			inverse_prob = new Vector<Integer>(Collections.nCopies(this.n,(Integer)1000));
			total_weights=1000*n;

			handle_owned =new Vector<Integer>(Collections.nCopies(this.n+1,(Integer)(-1)));
			handle_owner =new Vector<Integer>(Collections.nCopies(this.n+1,(Integer)(-1)));

			is_taken =new Vector<Integer>(Collections.nCopies(this.n+1,(Boolean)(false)));
			has_handle =new Vector<Integer>(Collections.nCopies(this.n+1,(Boolean)(false)));

	}
	@Override
	public int init(int n) {

		this.turn = 0;
		this.n = n;
		this.empty_handles = n;
		
		initVectors();

		lastMove = getNonConflictMove(prob);
		lastEvenMove = lastMove;
		return lastMove + 1;






	}

	// Strategy: (Just for demostration, may not work.)
	// 1. If no one grabs your handle, stay.
	// 2. Grab the i-th handle where i is the id of some player who grab the same handle as you do.
	public int EvenMove(List<Integer> conflicts,Boolean simple)
	{
		if(found!=-1)
		{
			//System.out.println("Found :" + Integer.toString(found));
			lastMove = found;

			//double_check.add(lastMove);
		}
		else
		{
			System.out.print("Prob: ");
			System.out.println(prob);
			lastMove = getNonConflictMove(prob);

		}
		lastEvenMove = lastMove;


		return lastMove + 1;

	}

	public int OddMove(List<Integer> conflicts,Boolean simple)
	{
		if(conflicts.size()==0 && found==-1)
		{
			found = lastMove;
			//System.out.println("Setting Found :" + Integer.toString(found));
		}
		//updateProb(conflicts);
		if(!simple)
			updateEstimates(conflicts);
		else
			updateProb(conflicts);

		//if(found==-1)
			//lastMove = getAway();
		//else
			//lastMove = (lastMove+1)%n;

		lastMove = getAway();
		lastOddMove = lastMove;
		return lastMove +1;
		 //System.out.println("lastOddMove "+lastOddMove);
	}
	@Override
	public int attempt(List<Integer> conflicts) {
		++ turn;
			if(turn%2==0)
			{
					return EvenMove(conflicts,true);
			}
			else //On even turns, just make sure you can come back.
			{

					return OddMove(conflicts,true);
			}


		//return lastMove+1;
	}

}
