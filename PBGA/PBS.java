package PBGA;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class PBS {	
	private ArrayList<Integer> edge1= new ArrayList<>();
	private ArrayList<Integer> edge2= new ArrayList<>();
	private Set<Integer> vertex = new LinkedHashSet<>();
	private double cost =0;
	Comparator<double[] > distanceComparator = new Comparator<>() {
        @Override
        public int compare(double[] s1, double[] s2) {
        	if (s1[0] == s2[0]) return 0;
        	else if (s1[0] > s2[0]) return 1;
        	else return -1;
        }
    };
	
	public PBS(int n , ArrayList<Integer> intermediate, ArrayList<Integer> target, ArrayList<ArrayList<Double>> edge)	{
		PriorityQueue<double[]> heap = new PriorityQueue<>(distanceComparator);
		ArrayList<Double> dist = new ArrayList<>();
		ArrayList<Integer> parent = new ArrayList<>();	
		ArrayList<Boolean> isVisted= new ArrayList<>();
		
		this.vertex.clear();
		this.edge1.clear();
		this.edge2.clear();
		cost=0;
		
		for (int i =0; i<n;++i) {
			dist.add(1e9);
			parent.add(null);
			isVisted.add(false);
		}
//		System.out.println(target);
		
		int start = target.get(0);
		this.vertex.add(start);
		
		isVisted.set(start,true);
		double[] temp= {0,start};		
		heap.add(temp);
		dist.set(start, 0.0);
		
		for (int t=1;t<target.size();++t) {	
			while (!heap.isEmpty()) {
				temp = heap.poll();
				double dis_u =temp[0];
				int u = (int) temp[1];
				
				if (dis_u >dist.get(u))
					continue;
				if (dis_u > dist.get(target.get(t))) {
					heap.add(temp);
					break;
				}
					
				if (u == target.get(t))
					break;
				
				// update neighbor of u
				for (int i: intermediate) {
					double newPath=dist.get(u) + edge.get(u).get(i);
					if (dist.get(i) > newPath) {
						dist.set(i,newPath);
						parent.set(i,u);
						double[] temp2 = {newPath,i};
						heap.add(temp2);
					}
				}	
				for (int i: target) {
					double newPath=dist.get(u) + edge.get(u).get(i);
					if (dist.get(i) > newPath) {
						dist.set(i,newPath);
						parent.set(i,u);
						double[] temp2 = {newPath,i};
						heap.add(temp2);
					}
				}	
			}
			
			//add target vertex u to the tree
			//add its path to the heap to update neighbor
			int u =target.get(t);
			while(!isVisted.get(u)) {
				int v=parent.get(u);
				edge1.add(u);
				edge2.add(v);
				
				vertex.add(u);			
				isVisted.set(u,true);
				
				cost += edge.get(u).get(v);
				dist.set(u,0.0);
				
				double[] temp2 = {0,u};
				heap.add(temp2);
				u=v;
			}
		}
		
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

	public Set<Integer> getVertex() {
		return vertex;
	}
	
	public boolean equals(PBS other) {
		if (this.cost != other.getCost()) return false;
		if (! this.getVertex().equals(other.getVertex()) ) return false;
		return true;
	}
	

}
