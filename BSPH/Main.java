package BSPH;

import java.io.File;

import Graph.Graph;
import SPGA.SPGA;

public class Main {
    public static String inputpath="Input_OCT\\Non-Euclidean Instances";
    public static String outputpath;  
	public static void main(String[] args) {
    	String type= "Type_1_Small";
    	String file= "5berlin52.txt";
    	
    	int seed =0;
    	if (args.length>=2) {
    		file = args[0];
    		type = args[1];  
		}
    	
    	inputpath= inputpath + "\\"+ type+"\\"+file;
    	
    	String method ="BSPH";
    	file = file.replace(".txt", "");
    	
		String dirType = "Output_"+method;
		File dir = new File(dirType);
		if (!dir.exists()) {
		    dir.mkdir();
		}	 
		
		dirType +="\\Non-Euclidean";
		dir = new File(dirType);
		if (!dir.exists()) {
		    dir.mkdir();
		}	
		
		dirType = dirType +"\\" + type;
		dir = new File(dirType);
		if (!dir.exists()) {
		    dir.mkdir();
		} 
		outputpath=dirType+"\\"+method +"_("+file+")\\";
		dir = new File(outputpath);
		if (!dir.exists()) {
		    dir.mkdir();
		}
		
		// TODO Auto-generated method stub
		Graph graph =new Graph(inputpath);
		graph.computeInterGraph();
		
		for (int i=0; i<30; ++i) {
			seed=i;			
			BSPH solvingSPH = new BSPH (graph, outputpath+file+"_SEED("+seed+").txt", seed);
			
		}
	}

}
