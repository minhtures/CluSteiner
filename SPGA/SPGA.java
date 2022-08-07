package SPGA;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import Graph.Graph;


public class SPGA {
	public Graph graph;
	private Random rand;
	private int seed;
	
	private ArrayList<Integer> edge1;
	private ArrayList<Integer> edge2;
	
	private double cost = 1e10;
	private Individual best;
	
	private ArrayList<Individual> population = new ArrayList<>();
	private ArrayList<Double> history = new ArrayList<>();
	
	private final int POPSIZE = 100;
	private final int NUMBERGEN = 500;
	private final double pc =0.9;
	private final double pm =0.05;
	
	
	Comparator<Individual> individualComparator = new Comparator<>() {
        @Override
        public int compare(Individual ind1, Individual ind2) {
        	if (ind1.getCost() == ind2.getCost()) return 0;
        	else if (ind1.getCost() > ind2.getCost()) return 1;
        	else return -1;
        }
    };
    
	
    public SPGA(Graph graph, String outputpath, int SEED) {
    	this.graph=graph;
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
	    			Individual c1 = crossover(population.get(p1),population.get(p2));
	    			Individual c2 = crossover(population.get(p2),population.get(p1));
	    			if (rand.nextDouble()<pm) {
	    				c1 = mutation(c1);
	    			}
	    			if (rand.nextDouble()<pm) {
	    				c2= mutation(c2);
	    			}
	    			offspring.add(c1);
	    			offspring.add(c2);	
    			}
    		}
    		
    		
    		population.addAll(offspring);
    		//selection	
    		population.sort(individualComparator); 	
	    	while (population.size() > POPSIZE) {
				population.remove(POPSIZE);
			}
    		
    		best=population.get(0);
    		history.add(best.getCost());
    	}   
    	edge1= best.getEdge1();
    	edge2= best.getEdge2();
    	cost=best.getCost();
//    	System.out.println(best.getGenes());
    }
      
 	private Individual mutation(Individual ind) {
		ArrayList<Integer> gene = new ArrayList<>(ind.getGenes());
		
 		int pos1 = rand.nextInt(graph.getN_cluster());
 		int pos2 = rand.nextInt(graph.getN_cluster());
		while (pos1==pos2) {
			pos2 = rand.nextInt(graph.getN_cluster());
		}
		gene.set(pos1, ind.getGenes().get(pos2));
		gene.set(pos2, ind.getGenes().get(pos1));
		Individual c = new Individual(gene, graph, rand);
		return c;
	}

	private Individual crossover(Individual ind1, Individual ind2) {
 		int pos1 = rand.nextInt(graph.getN_cluster()-1);
 		int pos2 = rand.nextInt(graph.getN_cluster());
 		Set<Integer> used = new HashSet<>();
		while (pos1==pos2) {
			pos2 = rand.nextInt(graph.getN_cluster());
		}
		if (pos1>pos2) {
			int t=pos1;
			pos1=pos2;
			pos2=t;
		}
		ArrayList<Integer> gene = new ArrayList<>(ind1.getGenes());
		for (int i=pos1; i<pos2+1;i++) {
			used.add(gene.get(i));
		}
		int pos =pos2+1;
		pos%=graph.getN_cluster();
		
		for (int i=pos2+1; i<graph.getN_cluster(); i++ ) {
			int cluster = ind2.getGenes().get(i);
			if (!used.contains(cluster)) {
				gene.set(pos,cluster);
				pos++;
			}
		}
		pos%=graph.getN_cluster();
		for (int i=0; i<pos2+1; i++ ) {
			int cluster = ind2.getGenes().get(i);
			if (!used.contains(cluster)) {
				gene.set(pos,cluster);
				pos++;
				pos%=graph.getN_cluster();
				if (pos ==pos1)
					break;
			}	
		}
		
		
		Individual c = new Individual(gene, graph, rand);	
		return c;
	}

	private void initialize() {
 		ArrayList<Integer> order = new ArrayList<>();
 		for (int i=0; i<graph.getN_cluster();++i) {
 			order.add(i);
 		}
 		for (int i=0; i<POPSIZE;++i) {
 			Collections.shuffle(order, rand);
 			Individual ind = new Individual(new ArrayList<>(order), graph, rand);
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
	}
	
}

