package com.trema.rcn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class PageRunEvaluator {
	
	public void evaluate(Properties p){
		HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getPageParaMapFromRunfile(
				p.getProperty("out-dir")+"/"+p.getProperty("trec-runfile"));
		
		HashMap<String, ArrayList<String>> truePageParaMap = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
		
		for(String page:truePageParaMap.keySet()){
			ArrayList<String> trueParas = truePageParaMap.get(page);
			ArrayList<String> candParas = pageParaMap.get(page);
			int hit = 0;
			ArrayList<String> truePos = new ArrayList<String>();
			for(String para:trueParas){
				if(candParas.contains(para)){
					truePos.add(para);
					hit++;
				}
			}
			System.out.println(page+" "+hit+" "+(double)hit/trueParas.size()+" "+truePos.toString());
		}
	}
	
	public void numTotalTruePairs(Properties p){
		HashMap<String, ArrayList<String>> pageParaMap = DataUtilities.getPageParaMapFromRunfile(
				p.getProperty("out-dir")+"/"+p.getProperty("trec-runfile"));
		
		HashMap<String, ArrayList<String>> truePageParaMap = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("art-qrels"));
		
		HashMap<String, ArrayList<String>> trueTopSecParaMap = DataUtilities.getGTMapQrels(
				p.getProperty("data-dir")+"/"+p.getProperty("top-qrels"));
		
		HashMap<String, HashMap<String, ArrayList<String>>> tobePageSecParaMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
		
		int totalTruePairs = 0;
		String currPage;
		for(String topsec:trueTopSecParaMap.keySet()){
			currPage = topsec.split("/")[0];
			ArrayList<String> candParas = pageParaMap.get(currPage);
			ArrayList<String> matchedParas = new ArrayList<String>();
			int match = 0;
			for(String para:trueTopSecParaMap.get(topsec)){
				if(candParas.contains(para)){
					match++;
					matchedParas.add(para);
				}
			}
			//if(match>1){
				totalTruePairs+=this.nC2(match);
				if(!tobePageSecParaMap.keySet().contains(currPage)){
					HashMap<String, ArrayList<String>> matchedSecParaMap = new HashMap<String, ArrayList<String>>();
					tobePageSecParaMap.put(currPage, matchedSecParaMap);
				}
				tobePageSecParaMap.get(currPage).put(topsec, matchedParas);
			//}
		}
		//System.out.println("Query Retreived-para Retreived-para-pairs True-para True-para-pairs");
		ArrayList<String> pageoutput = new ArrayList<String>();
		for(String page:tobePageSecParaMap.keySet()){
			int n = 0, nc = 0, tn = 0, tnc = 0;
			int truen = 0, truenc = 0, ttruen = 0, ttruenc = 0;
			ArrayList<String> output = new ArrayList<String>();
			for(String top:tobePageSecParaMap.get(page).keySet()){
				n = tobePageSecParaMap.get(page).get(top).size();
				nc = this.nC2(n);
				tn+=n;
				tnc+=nc;
				truen = trueTopSecParaMap.get(top).size();
				truenc = this.nC2(truen);
				ttruen+=truen;
				ttruenc+=truenc;
				output.add(top+" "+n+" "+nc+" "+truen+" "+truenc);
			}
			pageoutput.add(page+" "+tn+" "+tnc+" "+ttruen+" "+ttruenc);
			/*
			for(String s:output)
				System.out.println(s);
			*/
		}
		/*
		System.out.println();
		for(String s:pageoutput)
			System.out.println(s);
		System.out.println("total true pairs: "+totalTruePairs);
		*/
		try {
			this.parapairQrelsWriter(tobePageSecParaMap, p.getProperty("out-dir")+"/"+p.getProperty("parapair-qrels"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parapairQrelsWriter(HashMap<String, HashMap<String, ArrayList<String>>> tobePageSecParas, String outfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outfile)));
		for(String page:tobePageSecParas.keySet()){
			for(String top:tobePageSecParas.get(page).keySet()){
				ArrayList<String> paras = tobePageSecParas.get(page).get(top);
				if(paras.size()>1){
					for(int i=0; i<paras.size()-1; i++){
						for(int j=i+1; j<paras.size(); j++)
							bw.write(paras.get(i)+" 0 "+paras.get(j)+" 1\n"+paras.get(j)+" 0 "+paras.get(i)+" 1\n");
					}
				}
			}
		}
		bw.close();
	}
	
	private int nC2(int n){
		if(n<2) return 0;
		else if(n==2) return 1;
		else{
			return n*(n-1)/2;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(new File("project.properties")));
			(new PageRunEvaluator()).numTotalTruePairs(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
