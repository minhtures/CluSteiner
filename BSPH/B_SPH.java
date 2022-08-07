package BSPH;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;


public class B_SPH {
	private Graph graph;
	private Random rand;
	private int seed;
	private ArrayList<Integer> edge1Best=new ArrayList<>() ;
	private ArrayList<Integer> edge2Best= new ArrayList<>();
	
	private ArrayList<Integer> edge1 =new ArrayList<>();
	private ArrayList<Integer> edge2 =new ArrayList<>();
	
	private double best = 1e10;
	private ArrayList<Integer> randomOrderOfCluster = new ArrayList();
	private final int NUMBEROFVALUATE = 50000;
	
    public B_SPH(Graph graph, String outputpath, int SEED) {
    	this.graph=graph;
    	this.seed= SEED;
    	rand = new Random(SEED);
    	long start =System.nanoTime();    //thoi gian bat dau chay chuong trinh
    	for (int i = 0; i < graph.getN_cluster(); ++i) {   		
    		randomOrderOfCluster.add(i);
    	}
    	
    	for(int i=0;i< NUMBEROFVALUATE;++i) {
    		double cost = run();
//    		System.out.println(cost);
    		if (cost < best) {
    			edge1Best.clear();
    			edge2Best.clear();
    			edge1Best.addAll(edge1);
    			edge2Best.addAll(edge2);
    			best=cost;
    			
    		}
    	}	
    	long end =System.nanoTime();      //thoi gian ket thuc chay chuong trinh
    	output(outputpath,end-start);
    }
    
    private double run() { 
    	edge1.clear();
    	edge2.clear();
    	double cost=0;
    	
    	//LEVEL1: BUILD TREE FOR THE CLUSTER
    	Set<Integer> intermediate = new HashSet<>(graph.getIntermediate());
    	//cluster include dest and used intermediate node
    	ArrayList<Set<Integer>> cluster = new ArrayList<>();
    	ArrayList<Set<Integer>> interOfCluster = new ArrayList<>();
    	
    	for (int i = 0; i < graph.getN_cluster(); ++i) {   		
    		cluster.add(new HashSet<Integer>());
    		interOfCluster.add(new HashSet<Integer>());
    	}
   
    	Collections.shuffle(randomOrderOfCluster, rand);
//    	System.out.println(intermediate.size());
    	
		for (int i : randomOrderOfCluster) {
//			System.out.println("Cluster:"+i);	
			int start = graph.getCluster().get(i).get(rand.nextInt(graph.getCluster().get(i).size()));
			SPH steinerTree = new SPH(graph.getN(),start, new ArrayList<Integer>(intermediate), graph.getCluster().get(i), graph.getEdge());
			edge1.addAll(steinerTree.getEdge1());
			edge2.addAll(steinerTree.getEdge2());
			cluster.get(i).addAll(steinerTree.getVertex());
			
			interOfCluster.get(i).addAll(steinerTree.getVertex());
			interOfCluster.get(i).removeAll(graph.getCluster().get(i));
				
			intermediate.removeAll(interOfCluster.get(i));
			cost += steinerTree.getCost();
//			System.out.println("Cost:"+steinerTree.getCost());	
//			System.out.println(cluster.get(i));
		}
//		System.out.println(cost);
//		System.out.println(intermediate.size());
		
		// compute interdomain graph
		ArrayList<Integer> group = new ArrayList<>(graph.getN());	//index of new node
		ArrayList<Integer> interVertex = new ArrayList<>();
		ArrayList<ArrayList<Double>> interDistance = new ArrayList<>();
		ArrayList<ArrayList<Integer>> representOfCluster = new ArrayList<>();
		
		int nInterGraph = graph.getN()+graph.getN_cluster();
		
		for (int i=0;i< nInterGraph;++i) {
			interDistance.add(new ArrayList<>(graph.getInterDistance().get(i)));
			representOfCluster.add(new ArrayList<>(graph.getRepresentOfCluster().get(i)));
		}
		
		for (int i=0; i< graph.getN();++i) {
			group.add(i);
		}
		
		for (int i=0;i<graph.getN_cluster();i++) {
			for (int j:cluster.get(i)) {
				group.set(j,i+graph.getN());
			}	
			interVertex.add(i+graph.getN());
		}  
			
		for (int c1=0; c1< graph.getN_cluster(); ++c1) {
			//distance to other cluster
			for (int c2=0; c2< graph.getN_cluster(); ++c2) {	
				if (c1==c2) continue;
				for (int i: interOfCluster.get(c1)) {
					//from c1 intermediate to c2 target
					// from c2 intermediate to c1 target will be computed in later iteration
					double distance = interDistance.get(i).get(c2+graph.getN());
					if (distance < interDistance.get(c1+graph.getN()).get(c2+graph.getN())) {
						representOfCluster.get(c1+graph.getN()).set(c2+graph.getN(), i);				
						int j = representOfCluster.get(c2+graph.getN()).get(i);
						representOfCluster.get(c2+graph.getN()).set(c1+graph.getN(), j);
		
						interDistance.get(c1+graph.getN()).set(c2+graph.getN(), distance);
						interDistance.get(c2+graph.getN()).set(c1+graph.getN(), distance);
					}
					
					// from c1 intermediate to c2 intermediate
					if (c1>c2) {
						for (int j: interOfCluster.get(c2)) {
							distance = graph.getEdge().get(i).get(j);
							if (distance < interDistance.get(c1+graph.getN()).get(c2+graph.getN())) {
								representOfCluster.get(c1+graph.getN()).set(c2+graph.getN(), i);								
								representOfCluster.get(c2+graph.getN()).set(c1+graph.getN(), j);
								
								interDistance.get(c1+graph.getN()).set(c2+graph.getN(), distance);
								interDistance.get(c2+graph.getN()).set(c1+graph.getN(), distance);
							}
						}
					}
				}
			}
			
			//distance to the intermediate
			for (int j: intermediate) {
				for (int i: interOfCluster.get(c1)) {
					double distance = graph.getEdge().get(i).get(j);
					if (distance < interDistance.get(c1+graph.getN()).get(j)) {
						representOfCluster.get(c1+graph.getN()).set(j, i);
						
						interDistance.get(c1+graph.getN()).set(j, distance);
						interDistance.get(j).set(c1+graph.getN(), distance);
					}
				}
			}
		}
		
		//LEVEL2: BUILD TREE FOR INTERDOMAIN
		int start = interVertex.get(rand.nextInt(graph.getN_cluster()));	
		SPH steinerTree = new SPH(nInterGraph,start, new ArrayList<Integer>(intermediate), interVertex, interDistance);
		for (int i=0; i< steinerTree.getEdge1().size(); ++i) {
			int u =steinerTree.getEdge1().get(i);
			int v = steinerTree.getEdge2().get(i);
			edge1.add(representOfCluster.get(u).get(v));
			edge2.add(representOfCluster.get(v).get(u));
		}
		cost += steinerTree.getCost();
		return cost;
	}
    
    
 	private void output(String path, long runTime) {
 		  double time = (double) runTime/1000000000.0;
    	System.out.print("Cost: "+ best +"\n");
    	System.out.println(time);
       
        try 
        {
            File myOutput = new File(path);
            try	{
                FileWriter myWriter = new FileWriter(path);
                System.out.println("File created: " + myOutput.getName());
                myWriter.write("Filename:" + graph.getName()+"_B_SPH \n");
                myWriter.write("Seed: "+ seed +"\n");
                myWriter.write("Cost: "+ best +"\n");               
                myWriter.write("Time: "+time+"\n");
                myWriter.write("Edge:");
                for (int i=0;i< edge1Best.size();i++) 
                {
                    myWriter.write("\n" +(edge1Best.get(i)+1)+ " - "+ (edge2Best.get(i)+1));
   
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
	}
	
}
