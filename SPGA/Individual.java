package SPGA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import BSPH.SPH;
import Graph.Graph;

public class Individual {
	private ArrayList<Integer> genes;
	private ArrayList<Integer> edge1 =new ArrayList<>();
	private ArrayList<Integer> edge2 =new ArrayList<>();
	double cost;
	
	public Individual(ArrayList<Integer> genes, Graph graph, Random rand) {
		this.genes = genes;
		calculateCost(graph, rand);
		// TODO Auto-generated constructor stub
	}
	public Individual(Individual ind) {
		this.genes=ind.getGenes();
		this.cost = ind.getCost();
		this.edge1 = ind.getEdge1();
		this.edge2 = ind.getEdge2();
	}
	
	private void calculateCost(Graph graph, Random rand) {
    	cost=0;	
    	//LEVEL1: BUILD TREE FOR THE CLUSTER
    	Set<Integer> intermediate = new HashSet<>(graph.getIntermediate());
    	//cluster include dest and used intermediate node
    	ArrayList<Set<Integer>> cluster = new ArrayList<>();
    	ArrayList<Set<Integer>> interOfCluster = new ArrayList<>();
    	edge1.clear();
    	edge2.clear();
    	
    	for (int i = 0; i < graph.getN_cluster(); ++i) {   		
    		cluster.add(new HashSet<Integer>());
    		interOfCluster.add(new HashSet<Integer>());
    	}
    	
		for (int i : this.genes) {
//			System.out.println("Cluster:"+i);	
			int start = graph.getCluster().get(i).get(rand.nextInt(graph.getCluster().get(i).size()));
			SPH steinerTree = new SPH(graph.getN(), start, new ArrayList<Integer>(intermediate), graph.getCluster().get(i), graph.getEdge());
			edge1.addAll(steinerTree.getEdge1());
			edge2.addAll(steinerTree.getEdge2());
			cluster.get(i).addAll(steinerTree.getVertex());
			
			interOfCluster.get(i).addAll(steinerTree.getVertex());
			interOfCluster.get(i).removeAll(graph.getCluster().get(i));
				
			intermediate.removeAll(interOfCluster.get(i));
			cost += steinerTree.getCost();
		}
		
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
		int start = interVertex.get(rand.nextInt(interVertex.size()));	
		SPH steinerTree = new SPH(nInterGraph,start, new ArrayList<Integer>(intermediate), interVertex, interDistance);
		for (int i=0; i< steinerTree.getEdge1().size(); ++i) {
			int u =steinerTree.getEdge1().get(i);
			int v = steinerTree.getEdge2().get(i);
			edge1.add(representOfCluster.get(u).get(v));
			edge2.add(representOfCluster.get(v).get(u));
		}
		cost += steinerTree.getCost();
	}

	public ArrayList<Integer> getGenes() {
		return genes;
	}

	public ArrayList<Integer> getEdge1() {
		return edge1;
	}

	public ArrayList<Integer> getEdge2() {
		return edge2;
	}

	public double getCost() {
		return cost;
	}

}
