package com.trema.rcn.rank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class LuceneRankerForPage {
	
	public void rank(String indexDirPath, String outlinePath, String outRunPath, String method, int retNo) throws IOException {
		
		FileInputStream fis = new FileInputStream(new File(outlinePath));
		final Iterator<Data.Page> pageIt = DeserializeData.iterAnnotations(fis); 
		Iterable<Data.Page> pageIterable = ()->pageIt;
		IndexSearcher is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(indexDirPath).toPath()))));
		if(method.equalsIgnoreCase("bm25"))
			is.setSimilarity(new BM25Similarity());
		else if(method.equalsIgnoreCase("bool"))
			is.setSimilarity(new BooleanSimilarity());
		else if(method.equalsIgnoreCase("classic"))
			is.setSimilarity(new ClassicSimilarity());
		else if(method.equalsIgnoreCase("lmds"))
			is.setSimilarity(new LMDirichletSimilarity());
		QueryParser qp = new QueryParser("parabody", new StandardAnalyzer());
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outRunPath)));
		StreamSupport.stream(pageIterable.spliterator(), true).forEach(page -> {
			try {
				HashSet<String> secIDsinPage = new HashSet<String>();
				HashMap<String, Float> retrievedResult = new HashMap<String, Float>();
				secIDsinPage = this.getAllSectionIDs(page);
				String queryString = page.getPageId();
				for(String s:secIDsinPage)
					queryString+=" "+s;
				queryString = queryString.replaceAll("[/,%20]", " ").replaceAll("enwiki:", "");
				Query q = qp.parse(queryString);
				TopDocs tds = is.search(q, retNo);
				ScoreDoc[] retDocs = tds.scoreDocs;
				for (int i = 0; i < retDocs.length; i++) {
					Document d = is.doc(retDocs[i].doc);
					retrievedResult.put(d.getField("paraid").stringValue(), tds.scoreDocs[i].score);
				}
				for(String para:retrievedResult.keySet()) {
					bw.write(page.getPageId()+" Q0 "+para+" 0 "+retrievedResult.get(para)+" "+method.toUpperCase()+"-MAP\n");
				}
				System.out.println("Done Page: "+page.getPageId());
			} catch (ParseException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		bw.close();
	}
	
	private HashSet<String> getAllSectionIDs(Data.Page page){
		HashSet<String> secIDList = new HashSet<String>();
		String parent = page.getPageId();
		for(Data.Section sec:page.getChildSections())
			addSectionIDToList(sec, secIDList, parent);
		return secIDList;
	}
	
	private void addSectionIDToList(Data.Section sec, HashSet<String> idlist, String parent){
		if(sec.getChildSections() == null || sec.getChildSections().size() == 0){
			idlist.add(parent+"/"+sec.getHeadingId());
		}
		else{
			idlist.add(parent+"/"+sec.getHeadingId());
			parent = parent+"/"+sec.getHeadingId();
			for(Data.Section child:sec.getChildSections())
				addSectionIDToList(child, idlist, parent);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
