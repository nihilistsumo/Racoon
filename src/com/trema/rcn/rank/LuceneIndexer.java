package com.trema.rcn.rank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class LuceneIndexer {
	
	public void indexParas(String indexDirPath, String parafilePath, boolean withEntity) throws IOException {
		Directory indexdir = FSDirectory.open((new File(indexDirPath)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);
		
		FileInputStream fis = new FileInputStream(new File(parafilePath));
		final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fis);
		final Iterable<Data.Paragraph> it = ()->paragraphIterator;
		int i=0;
		StreamSupport.stream(it.spliterator(), true).forEach(p->{
			try {
				this.indexPara(iw, p, i, withEntity);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		iw.close();
		
		System.out.println("Indexing complete\n");
	}
	
	private void indexPara(IndexWriter iw, Data.Paragraph para, int count, boolean withEntity) throws IOException {
		Document paradoc = new Document();
		paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
		paradoc.add(new TextField("parabody", para.getTextOnly().toLowerCase(), Field.Store.YES));
		if(withEntity) {
			String entityList = "";
			ArrayList<String> entities = (ArrayList<String>) para.getEntitiesOnly();
			for(String ent:entities)
				entityList+=ent+"+";
			entityList = entityList.substring(0, entityList.lastIndexOf("+")).toLowerCase();
			paradoc.add(new TextField("paraent", entityList, Field.Store.YES));
		}
		iw.addDocument(paradoc);
		count++;
		if(count%1000==0)
			System.out.println(count+" paragraphs indexed");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
