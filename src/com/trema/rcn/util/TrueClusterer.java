package com.trema.rcn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;


public class TrueClusterer {
	
	public void cluster(Properties p, String candSetFilePath, String clusterOutPath) throws IOException {
		HashMap<String, ArrayList<ArrayList<String>>> resultCluster = new HashMap<String, ArrayList<ArrayList<String>>>();
		HashMap<String, ArrayList<String>> candPageParaMap = DataUtilities.getPageParaMapFromRunfile(candSetFilePath);
		HashMap<String, ArrayList<String>> trueSecParaMap = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("hier-qrels"));
		
		HashMap<String, ArrayList<String>> pageSecMap = DataUtilities.getArticleSecMap(
				p.getProperty("data-dir")+"/"+p.getProperty("outline"));
		//ArrayList<String> results = new ArrayList<String>();
		IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(p.getProperty("index-dir")).toPath()))));
		Analyzer analyzer = new StandardAnalyzer();
		StreamSupport.stream(candPageParaMap.keySet().spliterator(), false).forEach(page -> {
			ArrayList<String> parasFromCand = candPageParaMap.get(page);
			ArrayList<String> negCluster = new ArrayList<String>();
			negCluster.addAll(parasFromCand);
			ArrayList<ArrayList<String>> trueClusters = new ArrayList<ArrayList<String>>();
			for(String sec:trueSecParaMap.keySet()) {
				if(sec.startsWith(page)) {
					System.out.println(sec);
					ArrayList<String> intersecParas = findIntersection(trueSecParaMap.get(sec), parasFromCand);
					if(intersecParas.size()>0) {
						negCluster.removeAll(intersecParas);
						trueClusters.add(intersecParas);
					}
				}
			}
			resultCluster.put(page, trueClusters);
			
			
			int negN = negCluster.size()/resultCluster.get(page).size();
			int negClustIndex = 0;
			for(int i=0; i<resultCluster.get(page).size(); i++) {
				for(int j=0; j<negN; j++) {
					resultCluster.get(page).get(i).add(negCluster.get(negClustIndex));
					negClustIndex++;
				}
			}
			int i=0;
			while(negClustIndex<negCluster.size()) {
				resultCluster.get(page).get(i).add(negCluster.get(negClustIndex));
				negClustIndex++;
			}
			//resultCluster.get(page).add(negCluster);
			
			
		});
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(clusterOutPath)));
		oos.writeObject(resultCluster);
		oos.close();
	}
	
	public ArrayList<String> findIntersection(ArrayList<String> listA, ArrayList<String> listB) {
		ArrayList<String> result = new ArrayList<String>();
		for(String para:listA) {
			if(listB.contains(para))
				result.add(para);
		}
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			TrueClusterer tc = new TrueClusterer();
			tc.cluster(p, "/home/sumanta/Documents/Mongoose-data/Mongoose-results/comb-top200-laura-cand-train-page-run", "/home/sumanta/Documents/Mongoose-data/Mongoose-results/comb-top200-laura-cand-train-dist-truecluster");
			//sm.map(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
