package com.trema.rcn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class CombineRlibModel {
	
	public void combine(Properties p, String modelsFolderPath) throws IOException {
		File folderOfModels = new File(modelsFolderPath);
		File[] models = folderOfModels.listFiles();
		RacoonHelper mh = new RacoonHelper(p, "-rf");
		double[] avgOptw = mh.getWeightVecFromRlibModel(models[0].getAbsolutePath());
		for(int i=1; i<models.length; i++) {
			double[] optw = mh.getWeightVecFromRlibModel(models[i].getAbsolutePath());
			for(int j=0; j<optw.length; j++)
				avgOptw[j]+=optw[j];
		}
		for(int i=0; i<avgOptw.length; i++)
			avgOptw[i]/=models.length;
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(modelsFolderPath+"/comb-rlib-model")));
		bw.write("## Combined rlib model using other models in the same folder\n");
		String weight = "";
		for(int i=0; i<avgOptw.length; i++)
			weight+=i+":"+avgOptw[i]+" ";
		bw.write(weight.trim());
		bw.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
