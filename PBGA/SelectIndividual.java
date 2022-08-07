package PBGA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class SelectIndividual {
	private static final int sizeMap=15;
	private static final int sizeInter=15;
	private static final int sizeLocal=15;
	private static ArrayList<Double> milestone;
	
	static Comparator<Individual> individualComparator = new Comparator<>() {
        @Override
        public int compare(Individual ind1, Individual ind2) {
        	if (ind1.getCost() == ind2.getCost()) {
        		return 0;
        	}
        	else if (ind1.getCost() > ind2.getCost()) return 1;
        	else return -1;
        }
    };

	public static ArrayList<Individual> selectByMap(ArrayList<Individual> population,double proElite) {
		ArrayList<Individual> newPop = new ArrayList<>();
		ArrayList<PriorityQueue<Individual>> map = new ArrayList<>();
		
		for (int i=0;i<sizeMap;++i) {
			map.add(new PriorityQueue<Individual>(individualComparator));
		}
		population.sort(individualComparator); 
		int tempSize = (int) (population.size()*proElite);
		int POPSIZE=(int) (population.size()*0.5);
		double min=population.get(0).getCost();
		double max=population.get(tempSize-1).getCost();
		
		
		for (int i=0; i< tempSize; ++i) {
			Individual ind = population.get(tempSize-i-1);
			int x = positionInMap(ind.getCost(),min,max);
			map.get(x).add(ind);
		}
		
		boolean flag=true;
		while (flag) {
			for (int i=0;i<sizeMap;++i) {
				if (map.get(i).isEmpty())
					continue;
				newPop.add(map.get(i).poll());
				if (newPop.size()>=POPSIZE)	{
					flag=false;
					break;
				}	
			}		
		}

		return newPop;
	}

	
	private static double scaleFunction(double value, double min, double max) {
//		return (value-min)/(max-min);
		return Math.log1p(value-min)/Math.log1p(max-min);
//		return Math.log1p((value-min)/(max-min))/ Math.log(2);
//		return Math.sqrt((value-min)/(max-min));
	}
	private static double inverseScaleFunction(double scale, double min, double max) {
//		return scale*(max-min)+min;
		return (Math.exp(scale*Math.log1p(max-min)))+min-1;
//		return (Math.exp(Math.log(2)*scale)-1)*(max-min)+min;
//		return Math.pow(scale,2)*(max-min)+min;
	}
	
	private static int positionInMap(double value, double min, double max) {
		int positionInMap = (int) (scaleFunction(value,min,max)*sizeMap);
		return Math.min(positionInMap,sizeMap-1);
	}
	
	public static ArrayList<Individual> selectElist(ArrayList<Individual> population) {
		ArrayList<Individual> newPop = new ArrayList<>();
		int POPSIZE = population.size()/2;
		
		population.sort(individualComparator); 	
		for (int i=0;i<POPSIZE;i++) {
			newPop.add(population.get(i));
		}
		
		return newPop;
	}
	
	
	public static ArrayList<Individual> selectRoulette(ArrayList<Individual> population, Random rand) {
		ArrayList<Double> weight=new ArrayList<>();
		ArrayList<Individual> tempPop = new ArrayList<>(population);
		ArrayList<Individual> newPop = new ArrayList<>();
		int POPSIZE = population.size()/2;
		newPop.add(population.get(0));
		
		Collections.shuffle(tempPop, rand);
		double sum_weight=0;
		for (Individual ind: tempPop) {
			double w=1000.0/Math.pow(ind.getCost(),2);
			sum_weight+=w;
			weight.add(w);
		}
		
		for (int i=1;i<POPSIZE;i++) {
			double r= rand.nextDouble()*sum_weight;
			for (int j=0; j< tempPop.size();j++) {
				r-=weight.get(j);
				if (r<0) {
					newPop.add(tempPop.get(j));
					sum_weight-=weight.get(j);
					tempPop.remove(j);
					weight.remove(j);
					break;
				}
			}
		}			
		newPop.sort(individualComparator);
		return newPop;
	}
}
