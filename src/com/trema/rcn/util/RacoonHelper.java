package com.trema.rcn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import com.trema.rcn.rank.LuceneIndexer;
import com.trema.rcn.rank.LuceneRankerForPage;
import com.trema.rcn.rank.LuceneRankerForSection;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;

import edu.unh.cs.treccar_v2.Data;

public class RacoonHelper {
	Properties p;
	HashMap<String, Data.Paragraph> parasMap;
	HashMap<String, ArrayList<String>> preprocessedParasMap;
	int nThreads;
	
	public RacoonHelper(Properties pr, String mode) {
		// TODO Auto-generated constructor stub
		this.p = pr;
		//this.parasMap = DataUtilities.getParaMapFromPath(pr.getProperty("data-dir")+"/"+pr.getProperty("parafile"));
		//this.preprocessedParasMap = DataUtilities.getPreprocessedParaMap(parasMap);
		//this.reducedParasMap = DataUtilities.getReducedParaMap(preprocessedParasMap);
		if(this.p.getProperty("use-default-poolsize").equalsIgnoreCase("yes")||
				this.p.getProperty("use-default-poolsize").equalsIgnoreCase("y"))
			this.nThreads = Runtime.getRuntime().availableProcessors()+1;
		else
			this.nThreads = Integer.parseInt(this.p.getProperty("threads"));
		System.out.println("Thread pool size "+this.nThreads);
	}
	
	public void index(String indexDirPath, String paraCborPath, boolean withEntity) {
		LuceneIndexer li = new LuceneIndexer();
		try {
			li.indexParas(indexDirPath, paraCborPath, withEntity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void rank(Properties prop, String outputRunfile, String level, String method, int retNo, String outlineFile) {
		//Arguments: String indexDirPath, String outlinePath, String outRunPath, String level, String method, int retNo
		try {
			LuceneRankerForSection lrs = new LuceneRankerForSection();
			lrs.rank(prop.getProperty("index-dir"), prop.getProperty("data-dir")+"/"+outlineFile, outputRunfile, level, method, retNo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void rankForPage(Properties prop, String outputRunfile, String method, int retNo, String outlineFile) {
		//Arguments: String indexDirPath, String outlinePath, String outRunPath, String level, String method, int retNo
		try {
			LuceneRankerForPage lrp = new LuceneRankerForPage();
			lrp.rank(prop.getProperty("index-dir"), prop.getProperty("data-dir")+"/"+outlineFile, outputRunfile, method, retNo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void combineRunfilesForRLib(String runfilesDir, String outputFetFilePath, String qrels){
		CombineRunFilesToRLibFetFile rlib = new CombineRunFilesToRLibFetFile();
		try {
			rlib.writeFetFile(p, runfilesDir, outputFetFilePath, qrels);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double[] getWeightVecFromRlibModel(String modelFilePath) throws IOException{
		double[] weightVec;
		BufferedReader br = new BufferedReader(new FileReader(new File(modelFilePath)));
		String line = br.readLine();
		while(line!=null && line.startsWith("#"))
			line = br.readLine();
		String[] values = line.split(" ");
		weightVec = new double[values.length];
		for(int i=0; i<values.length; i++)
			weightVec[i] = Double.parseDouble(values[i].split(":")[1]);
		return weightVec;
	}
}