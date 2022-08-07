package B_Heu;

import java.io.File;

import SPGA.SPGA;

public class Main {
    public static String inputpath="Input_OCT\\Non-Euclidean Instances";
    public static String outputpath;  
	  public static void main(String[] args) {
    	String type= "Type_5_Large";
    	String file= "25i500-308.txt";
    	
    	int seed =0;
    	if (args.length>=2) {
          file = args[0];
          type = args[1];  
      }
    	
    	inputpath= inputpath + "\\"+ type+"\\"+file;
    	
    	String method ="B_SPH";
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

		Graph graph =new Graph(inputpath);
		graph.computeInterGraph();
		
		for (int seed=0; seed<30; ++seed) {	
			B_SPH solvingSPH = new B_SPH(graph, outputpath+file+"_SEED("+seed+").txt", seed);
			
		}
	}

}
