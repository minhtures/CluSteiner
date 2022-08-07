package PBGA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import BSPH.SPH;
import Graph.Graph;

public class Individual {
	private ArrayList<Double> genes;
	private ArrayList<Integer> edge1 ;
	private ArrayList<Integer> edge2;
	private double cost;
	private double localCost;
	private double interCost;
	
	//first element has largest priority
	Comparator<double[] > priorityComparator = new Comparator<>() {
        @Override
        public int compare(double[] s1, double[] s2) {
        	if (s1[0] == s2[0]) return 0;
        	else if (s1[0] < s2[0]) return 1;
        	else return -1;
        }
    };
    
	public Individual(ArrayList<Double> genes, Graph graph) {
		this.genes = genes;
		calculateCost(graph);
		// TODO Auto-generated constructor stub
	}
	public Individual(Graph graph, Random rand) {
		calculateCostSPH(graph, rand);
	}
	private void calculateCostSPH(Graph graph, Random rand) {
	   	cost=0;	
		//LEVEL1: BUILD TREE FOR THE CLUSTER
		Set<Integer> intermediate = new LinkedHashSet<>(graph.getIntermediate());
		//cluster include dest and used intermediate node
		ArrayList<Set<Integer>> cluster = new ArrayList<>();
		ArrayList<Set<Integer>> interOfCluster = new ArrayList<>();
		this.genes = new ArrayList<Double>();
		edge1 = new ArrayList<>();
		edge2 = new ArrayList<>();
		ArrayList<ArrayList<Double>> priority = new ArrayList<>();
		
		ArrayList<Integer> order = new ArrayList<Integer>();
		for (int i = 0; i < graph.getN_cluster(); ++i) {   		
			cluster.add(new LinkedHashSet<Integer>());
			interOfCluster.add(new LinkedHashSet<Integer>());
			order.add(i);
			priority.add(new ArrayList<>());
		}
		Collections.shuffle(order, rand);
//		System.out.println(order);
		
		for (int i : order) {	
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
		localCost=cost;
	
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
		
		for (int c: order) {
			for (int j:cluster.get(c)) {
				group.set(j,c+graph.getN());
			}	
			interVertex.add(c+graph.getN());
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
		interCost = steinerTree.getCost();
		cost+= interCost;	
		//	encode gene from tree
		// scale : [0-0.5) avoid many individual have same value in gene
//		double scale = rand.nextDouble(); 
		double scale=0.5*rand.nextDouble();
		double p=scale*Math.pow(0.8,graph.getN_cluster());
		for (int i:steinerTree.getVertex()) {
			int c =i-graph.getN();
			if (c<0)	continue;
			for (int j: graph.getCluster().get(c)) {
				priority.get(c).add(p);
			}
			
			for (int t: cluster.get(c)) {
				int j = graph.getCluster().get(c).indexOf(t);
				if (j<0) continue;
				priority.get(c).set(j, p+scale);
				p*=0.99;
			}
			p*=0.9;
		}
		
		p=scale;
		for (int c: order) {
			int t = new ArrayList<Integer>(cluster.get(c)).get(0);
			int j = graph.getCluster().get(c).indexOf(t);
			double delta = p- priority.get(c).get(j);
			int size = priority.get(c).size();
			delta /= (size-1);
			
			for (int i=0; i<size ; ++i) {
				double temp = priority.get(c).get(i);
				temp -=delta;
				priority.get(c).set(i,temp);
			}			
			priority.get(c).set(j,p+scale);
			p*=0.99;			
		}
		for (int i=0; i<graph.getN_cluster();++i) {
			for (int j=0;j <graph.getCluster().get(i).size();++j) {
				this.genes.add(priority.get(i).get(j));
			}
		}
	}

	private void calculateCost(Graph graph) {	
		edge1=new ArrayList<>();
    	edge2=new ArrayList<>();
    	
		ArrayList<PriorityQueue<double[]>> order = new ArrayList<>();
		PriorityQueue<double[]> interOrder = new PriorityQueue<>(priorityComparator);
		PriorityQueue<double[]> interVertexOrder = new PriorityQueue<>(priorityComparator);
		
		int count =0;
    	for (int i = 0; i < graph.getN_cluster(); ++i) {   		
    		PriorityQueue<double[]> intraOrder = new PriorityQueue<>(priorityComparator); 		
    		
    		double priorityVertexOrder=0;
    		for (int j: graph.getCluster().get(i)) {
    			double[] temp= {this.genes.get(count),j};
    			count++;
        		intraOrder.add(temp);
        		priorityVertexOrder+=temp[0];
    		}  		
    		order.add(intraOrder);
    		
    		double[] temp= {intraOrder.peek()[0],i};
    		interOrder.add(temp);
    		
    		double[] temp2 = {priorityVertexOrder/intraOrder.size(),i};
    		interVertexOrder.add(temp2);	
    	}
		
    	cost=0;	
    	//LEVEL1: BUILD TREE FOR THE CLUSTER
    	Set<Integer> intermediate = new LinkedHashSet<>(graph.getIntermediate());
    	//cluster include dest and used intermediate node
    	ArrayList<Set<Integer>> cluster = new ArrayList<>();	
    	ArrayList<Set<Integer>> interOfCluster = new ArrayList<>();	
    	
    	for (int i = 0; i < graph.getN_cluster(); ++i) {   		
    		cluster.add(new LinkedHashSet<Integer>());
    		interOfCluster.add(new LinkedHashSet<Integer>());
    	}
    	
    	for (int i = 0; i < graph.getN_cluster(); ++i) {
    		double[] temp = interOrder.poll();
    		int c = (int) temp[1];

    		// graph.getCluster().get(c) with order
    		ArrayList<Integer> target =new ArrayList<>();
    		while(!order.get(c).isEmpty()) {
    			double[] temp2 = order.get(c).poll();
    			int vertex = (int) temp2[1];
    			target.add(vertex);
    		}
			PBS steinerTree = new PBS(graph.getN(),  new ArrayList<Integer>(intermediate), target , graph.getEdge());
			
			edge1.addAll(steinerTree.getEdge1());
			edge2.addAll(steinerTree.getEdge2());
			
			cluster.get(c).addAll(steinerTree.getVertex());		
			interOfCluster.get(c).addAll(steinerTree.getVertex());
			interOfCluster.get(c).removeAll(graph.getCluster().get(c));			
			intermediate.removeAll(interOfCluster.get(c));
			
			cost += steinerTree.getCost();
		}
    	localCost=cost;
		
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
		
		for (double[] temp: interVertexOrder) {
			int c = (int) temp[1];
			for (int j:cluster.get(c)) {
				group.set(j,c+graph.getN());
			}	
			interVertex.add(c+graph.getN());
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
		PBS steinerTree = new PBS(nInterGraph, new ArrayList<Integer>(intermediate), interVertex, interDistance);
		for (int i=0; i< steinerTree.getEdge1().size(); ++i) {
			int u =steinerTree.getEdge1().get(i);
			int v = steinerTree.getEdge2().get(i);
			edge1.add(representOfCluster.get(u).get(v));
			edge2.add(representOfCluster.get(v).get(u));
		}
		interCost= steinerTree.getCost();
		cost+=interCost;
	}

	public ArrayList<Double> getGenes() {
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
	public double getLocalCost() {
		return localCost;
	}
	public double getInterCost() {
		return interCost;
	}

}
