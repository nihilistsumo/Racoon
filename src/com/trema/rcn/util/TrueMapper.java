package com.trema.rcn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class TrueMapper {
	
	public void map(Properties p, String candSetFilePath, String runfileOutPath) {
		try {
			HashMap<String, ArrayList<String>> candPageParaMap = DataUtilities.getPageParaMapFromRunfile(candSetFilePath);
			HashMap<String, ArrayList<String>> trueSecParaMap = DataUtilities.getGTMapQrels(
					p.getProperty("data-dir")+"/"+p.getProperty("hier-qrels"));
			
			HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(
					p.getProperty("data-dir")+"/"+p.getProperty("outline"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(runfileOutPath)));
			//ArrayList<String> results = new ArrayList<String>();
			IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(p.getProperty("index-dir")).toPath()))));
			Analyzer analyzer = new StandardAnalyzer();
			HashMap<String, HashMap<String, Double>> rankResult = new HashMap<String, HashMap<String, Double>>();
			StreamSupport.stream(trueSecParaMap.keySet().spliterator(), true).forEach(sec -> { 
				ArrayList<String> relParas = trueSecParaMap.get(sec);
				ArrayList<String> retParas = candPageParaMap.get(sec.split("/")[0]);
				HashMap<String, Double> rankings = new HashMap<String, Double>();
				for(String para:retParas) {
					if(relParas.contains(para))
						rankings.put(para, 1.0);
					else
						rankings.put(para, 0.0);
				}
				rankResult.put(sec, rankings);
			});
			for(String sec:rankResult.keySet()) {
				for(String para:rankResult.get(sec).keySet()) {
					//System.out.println(q+" "+para+" "+rankResult.get(q).get(para));
					bw.write(sec+" Q0 "+para+" 0 "+rankResult.get(sec).get(para)+" TRUE-MAPPER\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			TrueMapper tm = new TrueMapper();
			tm.map(p, "/home/sumanta/Documents/Mongoose-data/Mongoose-results/comb-top200-laura-cand-train-page-run", "/home/sumanta/Documents/Mongoose-data/Mongoose-results/comb-top200-laura-cand-train-truemap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
