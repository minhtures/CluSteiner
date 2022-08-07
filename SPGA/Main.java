package SPGA;

import java.io.File;

import Graph.Graph;

public class Main {
    public static String inputpath="Input_OCT\\Non-Euclidean Instances";
    public static String outputpath;  
	public static void main(String[] args) {
    	String type= "Type_1_Large";
    	String file= "10pcb442.txt";
    	
    	if (args.length>=2) {
    		file = args[0];
    		type = args[1];  
		}
    	//remove format file
    	
    	inputpath= inputpath + "\\"+ type+"\\"+file;
    	
    	String method ="SPGA";
    	file = file.replace(".txt", "");
    	
		String dirType = "Output_"+method;
		File dir = new File(dirType);
		if (!dir.exists()) {
		    dir.mkdir();
		}	
		dirType = dirType +"\\Non-Euclidean";
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
		Graph graph =new Graph(inputpath);
		graph.computeInterGraph();
		
		for (int seed=0; seed<30; ++seed) {		
			SPGA solvingSPH = new SPGA(graph, outputpath+file+"_SEED("+seed+").txt", seed);
			
		}
	}

}
