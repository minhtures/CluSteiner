package BSPH;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class SPH {	
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
	
	public SPH(int n , int start ,ArrayList<Integer> intermediate, ArrayList<Integer> target, ArrayList<ArrayList<Double>> edge)	{
		ArrayList<Boolean> isTarget= new ArrayList<>();
		PriorityQueue<double[]> heap = new PriorityQueue<>(distanceComparator);
		ArrayList<Double> dist = new ArrayList<>();
		ArrayList<Integer> parent = new ArrayList<>();	
		ArrayList<Boolean> isVisted= new ArrayList<>();
		
		for (int i =0; i<n;++i) {
			dist.add(1e9);
			parent.add(null);
			isTarget.add(false);
			isVisted.add(false);
		}
		
		for (int i : target) {
			isTarget.set(i,true);
		}
//		System.out.println(target);
		
		vertex.add(start);
		isVisted.set(start,true);
		dist.set(start, 0.0);
		double[] temp= {0,start};		
		heap.add(temp);
		
		
		int count = target.size();
		while( heap.size() !=0) {
			temp = heap.remove();
			double dis_u =temp[0];
			int u = (int) temp[1];
//			System.out.println(u+":"+dis_u+"("+ parent.get(u)+")");
			
			if (dis_u >dist.get(u))
				continue;
			
			if (isTarget.get(u)) {
//				System.out.println("target: "+u);
				isTarget.set(u,false);
				
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
				count--;
				if (count ==0) break;
			}
			
			// update neighbor of u
			for (int i: intermediate) {
				if (dist.get(i) > dist.get(u) + edge.get(u).get(i)) {
					dist.set(i,dist.get(u) + edge.get(u).get(i));
					parent.set(i,u);
					double[] temp2 = {dist.get(i),i};
					heap.add(temp2);
				}
			}
			for (int i: target) {
				if (dist.get(i) > dist.get(u) + edge.get(u).get(i)) {
					dist.set(i,dist.get(u) + edge.get(u).get(i));
					parent.set(i,u);
					double[] temp2 = {dist.get(i),i};
					heap.add(temp2);
				}
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
	
	public boolean equals(SPH other) {
		if (this.cost != other.getCost()) return false;
		if (! this.getVertex().equals(other.getVertex()) ) return false;
		return true;
	}
	

}
