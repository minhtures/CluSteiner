package Graph;

import java.io.File;
import java.io.FileWriter;
import java.security.KeyPair;
import java.util.*;


public class Graph {
	private ArrayList<ArrayList<Integer>> cluster = new ArrayList<>();
	private ArrayList<ArrayList<Double>> edge = new ArrayList<>();
	private Set<Integer> intermediate = new HashSet<>();
	
	private ArrayList<ArrayList<Double>> interDistance;
	private ArrayList<ArrayList<Integer>> representOfCluster;
	
	private String name;
	private int N;
	private int n_cluster;
	
	public Graph(String dir) {

		try {
            Scanner myReader = new Scanner(new File(dir));
            
            name= myReader.nextLine().replace("Name : ","");	//name
            myReader.nextLine();	//TYPE: NON_EUC_CLUSTERED_TREE
            
            N = Integer.parseInt(myReader.nextLine().replace("DIMENSION : ", "")) ;
            n_cluster = Integer.parseInt(myReader.nextLine().replace("NUMBER_OF_CLUSTERS: ", "")) ;
            
            myReader.nextLine();	//EDGE_WEIGHT_SECTION:
            
            for (int i=0;i < N;++i) {
            	intermediate.add(i);
            	ArrayList<Double> tempDistance = new ArrayList<>();
            	for (int j=0;j < N;++j) {
            		tempDistance.add(myReader.nextDouble());
            	}
            	edge.add(tempDistance);
            }
            myReader.nextLine(); //end egde line
            
            
            myReader.nextLine();	//CLUSTER_SECTION:
            
            for (int i=0;i<n_cluster;++i) {
            	int j = myReader.nextInt();		//number of set  
            	ArrayList<Integer> tempCluster = new ArrayList<Integer>();
            	
//            	System.out.print(j+" ");
//            	myWriter.write(j+" ");
            	j= myReader.nextInt();		//node of set
            	
            	while (j != -1 ) {
            		tempCluster.add(j-1);
            		intermediate.remove(j-1);       		
            		j= myReader.nextInt();	
            		
            	}
            	cluster.add(tempCluster);
            }                
            
            myReader.close();
    	}
    	catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } 
	}
	
	
	public void computeInterGraph() {
		this.interDistance = new ArrayList<>();
		this.representOfCluster = new ArrayList<>();
		ArrayList<Integer> group = new ArrayList<>(this.getN());	//index of new node
		
		int nInterGraph = this.getN()+this.getN_cluster();
		
		for (int i=0; i< this.getN();++i) {
			group.add(i);
		}					
		for (int c=0; c<this.getN_cluster(); ++c) {
			for (int j:this.getCluster().get(c)) {
				group.set(j,c+this.getN());
			}	
		} 
			
		for (int i=0;i<nInterGraph;i++) {
			ArrayList<Double> temp = new ArrayList<>();
			ArrayList<Integer> temp2 = new ArrayList<>();
			for (int j=0;j<nInterGraph;j++) {
				temp.add(1e9);
				temp2.add(i);
			}
			this.representOfCluster.add(temp2);
			this.interDistance.add(temp);
		}
	
		for (int i=0;i<this.getN();i++) {
			for (int j=i+1;j<this.getN();j++) {
				int c1 = group.get(i);
				int c2 = group.get(j);
				
				double distance = this.getEdge().get(i).get(j);
				if (this.interDistance.get(c1).get(c2) > distance) {
					this.representOfCluster.get(c1).set(c2,i);
					this.representOfCluster.get(c2).set(c1,j);
					
					this.interDistance.get(c1).set(c2, distance);
					this.interDistance.get(c2).set(c1, distance);
				}		
			}	
		}
	}
	public ArrayList<ArrayList<Integer>> getCluster() {
		return cluster;
	}

	public ArrayList<ArrayList<Double>> getEdge() {
		return edge;
	}
	
	public Set<Integer> getIntermediate() {
		return intermediate;
	}

	public String getName() {
		return name;
	}

	public int getN() {
		return N;
	}

	public int getN_cluster() {
		return n_cluster;
	}

	
	public double edge(int i, int j) {
		return edge.get(i).get(j);
	}


	public ArrayList<ArrayList<Double>> getInterDistance() {
		return interDistance;
	}


	public ArrayList<ArrayList<Integer>> getRepresentOfCluster() {
		return representOfCluster;
	}
}
