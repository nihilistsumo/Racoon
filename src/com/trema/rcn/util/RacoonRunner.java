package com.trema.rcn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;


public class RacoonRunner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(new File("project.properties"))); 
			RacoonHelper mh = new RacoonHelper(prop, args[0]);
			
			//Index paragraphs
			// -i index-directory-path paragraph-cbor-path with-entity?(entity/Entity/...)
			if(args[0].equalsIgnoreCase("-i")) {
				String indexOutPath = args[1];
				String paraCborPath = args[2];
				String withEntity = args[3];
				if(withEntity.startsWith("ent")||withEntity.startsWith("Ent")||withEntity.startsWith("ENT"))
					mh.index(indexOutPath, paraCborPath, true);
				else
					mh.index(indexOutPath, paraCborPath, false);
			}
			
			// -r outline-file output-runfile top/hier bm25/bool/classic/lmds 200
			else if(args[0].equalsIgnoreCase("-r")) {
				String outline = args[1];
				String outputRunfilePath = args[2];
				String level = args[3];
				String method = args[4];
				int retNo = Integer.parseInt(args[5]);
				mh.rank(prop, outputRunfilePath, level, method, retNo, outline);
			}
			
			// -rpage outline-file output-runfile bm25/bool/classic/lmds 200
			else if(args[0].equalsIgnoreCase("-rpage")) {
				String outline = args[1];
				String outputRunfilePath = args[2];
				String method = args[3];
				int retNo = Integer.parseInt(args[4]);
				mh.rankForPage(prop, outputRunfilePath, method, retNo, outline);
			}
			
			//Combine run files to produce rlib feature file
			// -cmb runfiles-directory-path fet-file-output-path qrels-filename
			else if(args[0].equalsIgnoreCase("-cmb")){
				String runfilesDir = args[1];
				String outputFetFilePath = args[2];
				String qrelsFilename = args[3];
				mh.combineRunfilesForRLib(runfilesDir, outputFetFilePath, qrelsFilename);
			}
			//Combine run files to produce rank file using trained Rlib model
			// -cmbrun folder path to run files, filepath to rlib model, filepath to output runfile
			else if(args[0].equalsIgnoreCase("-cmbrun")){ 
				String runfilesDir = args[1];
				String rlibModelPath = args[2];
				String outputRunfilePath = args[3];
				CombineRunFilesUsingRlibModel cmb = new CombineRunFilesUsingRlibModel();
				cmb.writeRunFile(prop, runfilesDir, rlibModelPath, outputRunfilePath);
			}
			//Combine rlib model files to average model file; used to combine cross validation optw of diff folds
			// -cmboptw folder path to models
			else if(args[0].equalsIgnoreCase("-cmboptw")){ 
				String modelsDir = args[1];
				CombineRlibModel cmb = new CombineRlibModel();
				cmb.combine(prop, modelsDir);
			}
			
			/*
			for(String cmd:prop.getProperty("mode").split("-")){
				if(cmd.equalsIgnoreCase("ir")){
					LuceneRanker lr = new LuceneRanker(prop.getProperty("index-dir"), prop.getProperty("data-dir")+"/"+prop.getProperty("parafile"), 
							prop.getProperty("data-dir")+"/"+prop.getProperty("outline"), prop.getProperty("out-dir")+"/"+prop.getProperty("trec-runfile"), 
							Integer.parseInt(prop.getProperty("no-ret")), "index");
					lr.doRanking();
				}
				else if(cmd.equalsIgnoreCase("r")){
					LuceneRanker lr = new LuceneRanker(prop.getProperty("index-dir"), prop.getProperty("data-dir")+"/"+prop.getProperty("parafile"), 
							prop.getProperty("data-dir")+"/"+prop.getProperty("outline"), prop.getProperty("out-dir")+"/"+prop.getProperty("trec-runfile"), 
							Integer.parseInt(prop.getProperty("no-ret")), "no-index");
					lr.doRanking();
				}
				else if(cmd.equalsIgnoreCase("p")){
					mh.saveParaSimilarityData(mh.processParaPairData(DataUtilities.getPageParaMapFromRunfile(
							prop.getProperty("out-dir")+"/"+prop.getProperty("trec-runfile"))),
							prop.getProperty("out-dir")+"/"+prop.getProperty("sim-data-out"));
				}
				else if(cmd.equalsIgnoreCase("l")){
					RLibFileWriterForCluster rlib = new RLibFileWriterForCluster(mh);
					rlib.processParaForRlib();
					rlib.writeFeatureFile();
				}
				else if(cmd.equalsIgnoreCase("c")){
					mh.runHACSimClustering();
				}
				else if(cmd.equalsIgnoreCase("cw")){
					mh.runHACW2VClustering();
				}
				else if(cmd.equalsIgnoreCase("km")){
					mh.runKMeansW2VClustering();
				}
				else if(cmd.equalsIgnoreCase("pm")){
					mh.runParaMapper();
				}
				else if(cmd.equalsIgnoreCase("cm")){
					mh.runClusteringMeasure();
				}
				else if(cmd.equalsIgnoreCase("cb")){
					mh.convertClusterDataToText();
				}
				else if(cmd.equalsIgnoreCase("pt")){
					mh.runPlainTextExtractor();
				}
				else if(cmd.equalsIgnoreCase("cmb")){
					mh.combineRunfilesForRLib();
				}
				else if(cmd.equalsIgnoreCase("sm")){
					SummaryMapper sm = new SummaryMapper(prop);
					sm.map(prop);
				}
				else if(cmd.equalsIgnoreCase("qe")){
					Similarity sim = null;
					if(prop.getProperty("cs-method").equals("BM25"))
					{
						System.out.println("Using BM25 for candidate set generation");
						sim = new BM25Similarity();
					}
					else if(prop.getProperty("cs-method").equals("LM-DS"))
					{
						System.out.println("Using LM-DS for candidate set generation");
						sim = new LMDirichletSimilarity();
					}
					else if(prop.getProperty("cs-method").equals("LM-JM"))
					{
						System.out.println("Using LM-JM for candidate set generation");
						float lambda = Float.parseFloat(args[11]);
						sim = new LMJelinekMercerSimilarity(lambda);
					}
					else
					{
						System.out.println("Using BM25 as default for candidate set generation");
						sim = new BM25Similarity();
					}
					mh.runQueryExpand(prop, new StandardAnalyzer(), sim);
				}
				else if(cmd.equalsIgnoreCase("prc")){
					mh.runPRC(prop.getProperty("out-dir")+"/"+prop.getProperty("cluster-out"), prop.getProperty("index-dir"), 
							prop.getProperty("curl-path"), prop.getProperty("out-dir")+"/"+prop.getProperty("paramap-run"));
				}
			}
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}