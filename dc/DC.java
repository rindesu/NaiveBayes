package dc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DC {
	static List<String> stopWDict = new ArrayList<String>(), //停用词表
						dict = new ArrayList<String>();//文档中所有非停用词的词干组成的词典
	static int sizeOfDict;//词典大小
	static int[] nij = new int[sizeOfDict];//第j个类别中有nij个文档出现了词i
	static File trainingTextDir = new File("F:\\classification\\文档集-Newgroups 20");//训练语料存放目录
	static String[] classification = {"alt.atheism", "comp.graphics", "comp.os.ms-windows.misc", "comp.sys.ibm.pc.hardware", "comp.sys.mac.hardware",//训练语料分类集合
								"comp.windows.x", "misc.forsale", "rec.autos", "rec.motorcycles", "rec.sport.baseball",
								"rec.sport.hockey", "sci.crypt", "sci.electronics", "sci.med", "sci.space", 
								"soc.religion.christian", "talk.politics.guns", "talk.politics.mideast", "talk.politics.misc", "talk.religion.misc"};
    static int[] numOfDocInEachCla;//每个类别的文档数
	public static void main(String[] args) {
	    	loadStopWDict();
		//createDict();
		loadDict();
		sizeOfDict = dict.size();
		
		for (int i = 0; i < 20; i++) {
			String[] textPath = readTextPath(i);
			numOfDocInEachCla[i] = textPath.length;
		}
		
		calcuNij(2, 19);
	}
	public static void loadStopWDict() {//读入停用词表，存放在容器stopWDict中
		File readFile = new File(trainingTextDir.getPath() + File.separator + "stopWDict.txt");
		BufferedReader reader = null;
		String tempString;
		try {
			reader = new BufferedReader(new FileReader(readFile));
			while ((tempString = reader.readLine()) != null) {
				if (tempString.isEmpty())
					continue;
				stopWDict.add(tempString);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i;
		//for (i = 0; i < stopWDict.size(); i++)
		//	System.out.println(stopWDict.get(i));
		//System.out.println(stopWDict.size());
	}
	public static String[] readTextPath(int c) {//读入类别c中所有文档的路径，参数为c，返回值为包含所有路径的数组
		int j;
		File classDir = new File(trainingTextDir.getPath() +File.separator +classification[c]);
		String[] textPath = classDir.list();
		for (j = 0; j < textPath.length; j++) {
			textPath[j] = trainingTextDir.getPath() +File.separator +classification[c] +File.separator +textPath[j];
		}
		return textPath;
	}
	public static List<String> preprocess(String docPath) {//对一个文档进行去停用词、词干提取、词去重和去标点。参数为文档路径，返回值为词集合，保存在容器doc中
		//提取词干
		List<String> doc = new ArrayList<String>();
		Stemmer stemmer = new Stemmer();
		doc = stemmer.stemming(docPath, stopWDict);
		return doc;
	}
	public static void createDict() {//生成文档中所有非停用词的词干组成的词典，按字典序排好，保存在dict.txt中。无需再调用
		int i, j, k;
		for (i = 0; i < 20; i++) {
			String[] textPath = readTextPath(i);
			for (j = 0; j < textPath.length; j++) {
				List<String> doc = preprocess(textPath[j]);
				for (k = 0; k < doc.size(); k++)
					if (!dict.contains(doc.get(k)))
						dict.add(doc.get(k));
			}
		}	
		Collections.sort(dict, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});
		File writeFile = new File("dict.txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(writeFile));
			for (i = 0; i < dict.size(); i++)
				writer.write(dict.get(i) + " ");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(dict.size());
	}
	public static void loadDict() {//读入词典
		File readFile = new File(trainingTextDir.getPath() + File.separator + "dict.txt");
		BufferedReader reader = null;
		String tempString;
		try {
			reader = new BufferedReader(new FileReader(readFile));
			tempString = reader.readLine();
			String[] words = tempString.split(" ");
			int i;
			for (i = 0; i < words.length; i++) {
				dict.add(words[i]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("dict_size: " + dict.size());
	}
	public static void calcuNij(int k, int j) {//从第j个类别的第k组数据中统计nij，k∈[1, testTimes]
		int i, m, p, et, minBound, maxBound;
		for (i = 0; i < sizeOfDict; i++)
			nij[i] = 0;
		et = numOfDocInEachCla[j] / 10;//第j个类别的每组数据的数目
		minBound = (k - 1) * et;
		maxBound = k * et;
		if (k == 10)
			maxBound = numOfDocInEachCla[j];
		System.out.println("minBound: " + minBound + " maxBound: " + maxBound);
		String[] textPath = readTextPath(j);
		for( m = 0; m < minBound; m++){
			//System.out.println(textPath[m]);
			List<String> doc = preprocess(textPath[m]);
			for (p = 0; p < doc.size(); p++) {
				if (dict.indexOf(doc.get(p)) >= 0)
					nij[dict.indexOf(doc.get(p))]++;
			
				//if (dict.indexOf(doc.get(p)) < 0)
					//System.out.println(doc.get(p));
			}
		}
		for(m=maxBound; m < numOfDocInEachCla[j]; m++){
			//System.out.println(textPath[m]);
			List<String> doc = preprocess(textPath[m]);
			for (p = 0; p < doc.size(); p++) {
				if (dict.indexOf(doc.get(p)) >= 0)
					nij[dict.indexOf(doc.get(p))]++;
				if (dict.indexOf(doc.get(p)) < 0)
					System.out.println("???? "+doc.get(p));
			}
		}
		/*
		for (m = minBound; m < maxBound; m++) {
			//System.out.println(textPath[m]);
			List<String> doc = preprocess(textPath[m]);
			for (p = 0; p < doc.size(); p++) {
				if (dict.indexOf(doc.get(p)) >= 0)
					nij[dict.indexOf(doc.get(p))]++;
				//if (dict.indexOf(doc.get(p)) < 0)
					//System.out.println(doc.get(p));
			}
		}
		for (i = 0; i < sizeOfDict; i++)
			if (nij[i] > 0)
				System.out.println(nij[i]);*/
	}
}
