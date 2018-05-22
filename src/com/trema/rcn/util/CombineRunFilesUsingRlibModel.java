package com.trema.rcn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

public class CombineRunFilesUsingRlibModel {
	
	public static final int RET_PARA_NO_IN_COMBINED = 200; 
	
	//Arguments: Properties, folder path to run files, filepath to rlib model, filepath to output runfile
	public void writeRunFile(Properties p, String runfilesDir, String rlibModelPath, String combinedRunfilePath) throws IOException{
		//String[] runfiles = p.getProperty("runfile-list").split(" ");
		File folderOfRunfiles = new File(runfilesDir);
		File[] runfiles = folderOfRunfiles.listFiles();
		Arrays.sort(runfiles);
		System.out.println("Files to be combined:"); 
		for(File f:runfiles)
			System.out.println(f.getAbsolutePath());
		RacoonHelper mh = new RacoonHelper(p, "-rf");
		double[] optW = mh.getWeightVecFromRlibModel(rlibModelPath);
		ArrayList<HashMap<String, HashMap<String, Double>>> runfileObjList = new ArrayList<HashMap<String, HashMap<String, Double>>>();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(combinedRunfilePath)));
		for(File rf:runfiles){
			try {
				runfileObjList.add(this.getRunfileObj(rf.getAbsolutePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Read all runfiles");
		HashSet<String> intersection = new HashSet<String>(runfileObjList.get(0).keySet());
		/*
		for(HashMap<String, HashMap<String, Double>> rf:runfileObjList){
			if(!rf.keySet().equals(runfileObjList.get(0).keySet()))
				throw new Exception("Query list of the run files are different!");
		}
		*/
		for(int i=1; i<runfileObjList.size(); i++){
			//intersection.retainAll(runfileObjList.get(i).keySet());
			intersection.addAll(runfileObjList.get(i).keySet());
		}
		HashMap<String, HashMap<String, Double>> combinedRunfile = new HashMap<String, HashMap<String, Double>>();
		for(String q:intersection){
			Map<String, Double> paraScores = new HashMap<String, Double>();
			HashSet<String> paras = new HashSet<String>();
			//paras.addAll(runfileObjList.get(0).get(q).keySet());
			for(int i=0; i<runfileObjList.size(); i++)
				if(runfileObjList.get(i).containsKey(q))
					paras.addAll(runfileObjList.get(i).get(q).keySet());
			for(String para:paras){
				String runfileLine = "";
				double combinedScore = 0;
				assert(optW.length==runfileObjList.size());
				for(int r=0; r<runfileObjList.size(); r++){
					if(runfileObjList.get(r).containsKey(q)) {
						if(runfileObjList.get(r).get(q).containsKey(para))
							combinedScore+=runfileObjList.get(r).get(q).get(para)*optW[r];
					}
				}
				paraScores.put(para, combinedScore);
				//bw.write(q+" Q0 "+para+" 0 "+combinedScore+" COMBINED\n");
			}
			combinedRunfile.put(q, (HashMap<String, Double>)MapUtil.sortByValue(paraScores, RET_PARA_NO_IN_COMBINED));
		}
		for(String q:combinedRunfile.keySet()) {
			for(String para:combinedRunfile.get(q).keySet())
				bw.write(q+" Q0 "+para+" 0 "+combinedRunfile.get(q).get(para)+" COMBINED\n");
		}
		bw.close();
	}
	
	public HashMap<String, HashMap<String, Double>> getRunfileObj(String runfilePath) throws IOException{
		HashMap<String, HashMap<String, Double>> rfObj = new HashMap<String, HashMap<String, Double>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(runfilePath)));
		String line = br.readLine();
		String q,p,s;
		while(line!=null){
			q = line.split(" ")[0];
			p = line.split(" ")[2];
			s = line.split(" ")[4];
			if(rfObj.keySet().contains(q)){
				rfObj.get(q).put(p, Double.parseDouble(s));
			}
			else{
				HashMap<String, Double> psmap = new HashMap<String, Double>();
				psmap.put(p, Double.parseDouble(s));
				rfObj.put(q, psmap);
			}
			line = br.readLine();
		}
		br.close();
		return rfObj;
	}

	//Arguments: folder path to run files, filepath to rlib model, filepath to output runfile
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			CombineRunFilesUsingRlibModel comb = new CombineRunFilesUsingRlibModel();
			comb.writeRunFile(p, args[0], args[1], args[2]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
