package PBGA;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import Graph.Graph;


public class PBGA {
	public Graph graph;
	private Random rand;
	private int seed;
	
	private ArrayList<Integer> edge1;
	private ArrayList<Integer> edge2;
	
	private double cost = 1e10;
	private Individual best;
	private int sizeGen;
	
	private ArrayList<Individual> population = new ArrayList<>();
	private ArrayList<Double> history = new ArrayList<>();
	private ArrayList<Double> historyProElite = new ArrayList<>();
	
	private final int POPSIZE = 100;
	private final int NUMBERGEN = 500;
	private final double pc =0.9;
	private final double pm =0.05;
	private final double nm=15;		//for PM
	private final double alpha=0.2; 		//for BLX
	public static final int lowerbound=0;
	public static final int upperbound=1;
	
	
	Comparator<Individual> individualComparator = new Comparator<>() {
        @Override
        public int compare(Individual ind1, Individual ind2) {
        	if (ind1.getCost() == ind2.getCost()) return 0;
        	else if (ind1.getCost() > ind2.getCost()) return 1;
        	else return -1;
        }
    };
    
	
    public PBGA(Graph graph, String outputpath, int SEED) {
    	this.graph=graph;	
    	//deprecated
    	this.graph.computeInterGraph();
    	
    	this.seed= SEED;
    	rand = new Random(SEED);
    	long start =System.nanoTime();    //thoi gian bat dau chay chuong trinh	
    	run();
    	long end =System.nanoTime();      //thoi gian ket thuc chay chuong trinh
    	output(outputpath,end-start);
    }
    
    private void run() {
    	initialize();
    	best = population.get(0);
    	history.add(best.getCost());
    	sizeGen=best.getGenes().size();
    	double proElite=0.9;
    	historyProElite.add(proElite);
    	
    	for(int it=0; it< NUMBERGEN; ++it) {
    		double avg=0;
    		double max=0;
    		for (Individual ind: population) {
    			avg+= ind.getCost();
    			if (ind.getCost()>max) 
    				max = ind.getCost();
    		}
    		avg/=POPSIZE;
    		System.out.println("Gen "+it+" : "+best.getCost()+" "+avg+" "+max);
    		ArrayList<Individual> offspring = new ArrayList<>();
    		
    		while (offspring.size() < POPSIZE) {
    			int p1 = rand.nextInt(POPSIZE);
    			int p2 =p1;
    			while (p2==p1) {
    				p2 = rand.nextInt(POPSIZE);
    			}
    			if (rand.nextDouble()<pc) {   
    				Individual c1,c2;
    				
    				c1=BLXcrossover(population.get(p1),population.get(p2));
    				c2=BLXcrossover(population.get(p2),population.get(p1));
	    			
	    			if (rand.nextDouble()<pm) {
	    				ArrayList<Double> delta = new ArrayList<Double>();
	    				for (int i=0;i <sizeGen;++i) {
	    					delta.add(calculateDelta(rand.nextDouble()));
	    				}	
	    				c1 = mutation(delta,c1);
	    			}
	    			if (rand.nextDouble()<pm) {
	    				ArrayList<Double> delta = new ArrayList<Double>();
	    				for (int i=0;i <sizeGen;++i) {
	    					delta.add(calculateDelta(rand.nextDouble()));
	    				}	
	    				c2 = mutation(delta,c2);
	    			}
	    			offspring.add(c1);
	    			offspring.add(c2);	
    			}
    		}
    		
    		population.addAll(offspring);
    		population.sort(individualComparator);
    		double PI=1-population.get(0).getCost()/best.getCost();		
    		proElite=Math.min(1, proElite*(1.01-PI*10));
    		proElite=Math.max(0.5, proElite);
    				
    		population= SelectIndividual.selectByMap(population,proElite);
    		
    		System.out.println(proElite);
    		best=population.get(0);
    		history.add(best.getCost());
    		historyProElite.add(proElite);
//    		System.out.println(best.getGenes());
    	}   
    	edge1= best.getEdge1();
    	edge2= best.getEdge2();
    	cost=best.getCost();
    	System.out.println(best.getGenes());
    }
    
 	private double calculateDelta(double u) {
 		if (u>=1) return 1;
 		if (u<=0) return -1;
 		if (u<=0.5) 
 			return Math.pow(2.0*u,1.0/(nm+1))-1;
 		else
 			return 1- Math.pow(2*(1-u),1.0/(nm+1));
 	}
 	
 	private Individual mutation(ArrayList<Double> delta ,Individual ind) {
		ArrayList<Double> gene = new ArrayList<>(ind.getGenes());
		for (int i=0; i<sizeGen;i++) {
			double value = ind.getGenes().get(i);
			if (delta.get(i)<0)
				value+=delta.get(i)*(value-lowerbound);
			else
				value+=delta.get(i)*(upperbound-value);
			gene.add(value);
		}	
		Individual c = new Individual(gene, graph);
		return c;
	}

	private Individual BLXcrossover(Individual ind1, Individual ind2) {
		ArrayList<Double> gene = new ArrayList<>();
		for (int i=0; i<sizeGen;i++) {
			double Pmin= Math.min(ind1.getGenes().get(i), ind2.getGenes().get(i));
			double Pmax= Math.max(ind1.getGenes().get(i), ind2.getGenes().get(i));
			double l=Pmin-(Pmax-Pmin)*alpha;
			double u=Pmax+(Pmax-Pmin)*alpha;
			double value = rand.nextDouble()*(u-l)+l;
					
			if (value>upperbound) value = rand.nextDouble()*(upperbound-Pmax)+Pmax;
			else if (value<lowerbound) value = rand.nextDouble()*(Pmin- lowerbound)+lowerbound;
			gene.add(value);
		}		
		
		Individual c = new Individual(gene, graph);	
		return c;
	}

	private void initialize() {
 		ArrayList<Double> order = new ArrayList<>();
 		for (int i=0; i<POPSIZE ;++i) {
 			// init by BSPH
 			Individual ind = new Individual(graph, rand);
 			population.add(ind);
 		}
 		population.sort(individualComparator);
	}

	private void output(String path, long runTime) {
 		double time = (double) runTime/1000000000.0;
    	System.out.print("Cost: "+ cost +"\n");
    	System.out.println(time);
       
        try 
        {
            File myOutput = new File(path);
            try	{
                FileWriter myWriter = new FileWriter(path);
                System.out.println("File created: " + myOutput.getName());
                myWriter.write("Filename:" + graph.getName()+"_SPGA \n");
                myWriter.write("Seed: "+ seed +"\n");
                myWriter.write("Cost: "+ cost +"\n");               
                myWriter.write("Time: "+time+"\n");
                myWriter.write("Edge:");
                for (int i=0;i< edge1.size();i++) 
                {
                    myWriter.write("\n" +(edge1.get(i)+1)+ " - "+ (edge2.get(i)+1));
   
                }
                myWriter.close();
            } 
            catch (IOException e) {
            	System.out.println("Fail to creat File: "+e);
			} 
        } 
        catch (Exception e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
        try 
        {
            File myOutput = new File(path.replace(".txt", ".gen"));
            try	{
                FileWriter myWriter = new FileWriter(path.replace(".txt", ".gen"));
                System.out.println("File created: " + myOutput.getName());
                myWriter.write("Filename:" + graph.getName()+"_SPGA \n");
                myWriter.write("Seed: "+ seed +"\n");
                for (int i=0;i< history.size();i++) 
                {
                    myWriter.write("Gen " +i+ " : "+ history.get(i)+"\n");
   
                }
                myWriter.close();
            } 
            catch (IOException e) {
            	System.out.println("Fail to creat GenFile: "+e);
			} 
        } 
        catch (Exception e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
        try 
        {
            File myOutput = new File(path.replace(".txt", "_proElite.txt"));
            try	{
                FileWriter myWriter = new FileWriter(path.replace(".txt", "_proElite.txt"));
                System.out.println("File created: " + myOutput.getName());
                myWriter.write("Filename:" + graph.getName()+"_SPGA \n");
                myWriter.write("Seed: "+ seed +"\n");
                for (int i=0;i< historyProElite.size();i++) 
                {
                    myWriter.write("Gen " +i+ " : "+ historyProElite.get(i)+"\n");
   
                }
                myWriter.close();
            } 
            catch (IOException e) {
            	System.out.println("Fail to creat GenFile: "+e);
			} 
        } 
        catch (Exception e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
	}
	
}

