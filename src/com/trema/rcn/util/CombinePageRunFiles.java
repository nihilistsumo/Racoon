package com.trema.rcn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/*
 * This class takes run file generated for hierarchical/toplevel sections and
 * combines it to produce another run file for page
 * 
 */
public class CombinePageRunFiles {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			//(new Word2VecMapper()).map(p);
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(p.getProperty("out-dir")+"/jordan-page-run")));
			HashMap<String, ArrayList<String>> hierParaMap = DataUtilities.getPageParaMapWithScoresFromRunfile(p.getProperty("out-dir")+"/jordan-hier-run");
			HashMap<String, HashMap<String, Double>> pageParaMap = new HashMap<String, HashMap<String, Double>>();
			HashMap<String, Double> alreadySeenPara = new HashMap<String, Double>();
			String page;
			for(String secid:hierParaMap.keySet()){
				page = secid.split("/")[0];
				if(!pageParaMap.keySet().contains(page)){
					HashMap<String, Double> paraScoreMap = new HashMap<String, Double>();
					for(String ps:hierParaMap.get(secid)){
						paraScoreMap.put(ps.split(" ")[0], Double.parseDouble(ps.split(" ")[1]));
					}
					pageParaMap.put(page, paraScoreMap);
				}
				else{
					HashMap<String, Double> paraScoreMap = pageParaMap.get(page);
					for(String ps:hierParaMap.get(secid)){
						double score = Double.parseDouble(ps.split(" ")[1]);
						String para = ps.split(" ")[0];
						if(paraScoreMap.containsKey(para)){
							if(paraScoreMap.get(para)<score)
								paraScoreMap.put(para, score);
						}
						else
							paraScoreMap.put(para, score);
					}
				}
			}
			for(String pageid:pageParaMap.keySet()){
				for(String paraid:pageParaMap.get(pageid).keySet()){
					bw.write(pageid+" Q0 "+paraid+" 0 "+pageParaMap.get(pageid).get(paraid)+" PAGE\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
