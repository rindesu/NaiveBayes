package dc;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class BayesClassifier {
	DC dc;
	int GROUPS = 20;
	int M = 100;
	double cross = 0.9;
	double[] pp = new double[GROUPS];
	//P(Doc|C)
	double[] pdc = new double[GROUPS];
	int[][] nji = new int[GROUPS][DC.sizeOfDict];
	public BayesClassifier(){
		dc = new DC();
		dc.loadStopWDict();
		dc.loadDict();	
		calculatePP();
		
		
	}
	
	public void loadNij(int test){
		System.out.println("Loading nij...");		
		File readFile = new File("nij_"+String.valueOf(test)+".txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(readFile));	
			reader.readLine();
		for( int classes = 0; classes < GROUPS; classes++){
			reader.readLine();
			String str = reader.readLine();
			String[] num = str.split(" ");
			for( int i = 0; i < dc.sizeOfDict; i++){
				nji[classes][i] = Integer.parseInt(num[i]);
					//System.out.println(classes+":"+dc.dict.get(i)+" "+nji[classes][i]);
			}
		}
		reader.close();
		
        }catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void test(){
		//loadNij();
		System.out.println("????");
		List<String> doc = dc.preprocess("H:\\课程\\机器学习概论\\文档集-Newgroups 20\\alt.atheism\\51123");
		//for( int i = 0; i < doc.size(); i++)
			//System.out.print(doc.get(i)+" ");
		System.out.println(doc.size());
		for( int i = 0; i < doc.size(); i++){
			System.out.println(doc.get(i));
			for( int classes = 0; classes < GROUPS; classes++){					
				//保存分类结果
				
				System.out.println(dc.dict.indexOf(doc.get(i))+" "+nji[classes][dc.dict.indexOf(doc.get(i))]);
			}
		}
		//System.out.println(doc.size());
		for( int classes = 0; classes < GROUPS; classes++){					
			//保存分类结果
			pdc[classes] = calcProd(doc,classes);//关键字在分类的条件概率
			//System.out.println("In process.");
			System.out.println(" class: "+classes + "：" + pdc[classes]);
		}
		//for( int i =0; i<20; i++)
			//System.out.println(nji[i][11856]);
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BayesClassifier bayesClassifier = new BayesClassifier();
		//bayesClassifier.test();
		
		//测试1
		for(int i=3;i<=10;i++)
			//bayesClassifier.train(i);
			bayesClassifier.classify(i);
	}	//计算训练集总文档数
	public int getTrainingFileCount(){
		int[] numofDocInEachCla = dc.numOfDocInEachCla;
		int total = 0;
		for( int i = 0; i < numofDocInEachCla.length; i++)
			total += numofDocInEachCla[i];
		return total;
	}
	//计算先验概率
	public void calculatePP() {
		int[] numofDocInEachCla = dc.numOfDocInEachCla;
		int total = getTrainingFileCount();
		for( int i = 0; i < numofDocInEachCla.length; i++){
			pp[i] = ((double)numofDocInEachCla[i])/total;
		}
	}
	//计算term的条件概率
	public double calculatePxc(int x, int group) {
		double nij = nji[group][x];
		double nj = dc.numOfDocInEachCla[group]*cross;

		double V = 20;
		//adjust after
		double probility = (nij + 1) / (nj + V); // 为了避免出现0这样极端情况，进行加权处理
		return probility;
	}
	//计算P(Doc|C）
	public double calcProd( List<String> doc , int group) {
		double probility = 1.0;
		for (int p = 0; p < doc.size(); p++) {
			int wordID = -1;
			if (dc.dict.indexOf(doc.get(p)) >= 0)
				wordID = dc.dict.indexOf(doc.get(p));
			else{
				System.out.println("amazing!!! "+doc.size()+" "+doc.get(p));
				continue;
			}
			//System.out.println(doc.get(p));
			probility *= calculatePxc(wordID, group)*M;		//计算给定的文本属性向量word在给定的分类group中的分类条件概率
		}
		probility *= pp[group];			//乘以先验概率
		return probility;
	}
	public void train(int test){	
		System.out.println("Begin trainning...");
		//calculatePP();
		File writeFile = new File("nij_"+String.valueOf(test)+".txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(writeFile));	
		for( int classes = 0; classes < GROUPS; classes++){
			System.out.println("train class: "+classes);
			dc.calcuNij(test, classes);
			//nji[classes] = dc.nij;
			writer.write("\ngroup: "+classes+"\n");
			for( int i = 0; i < dc.sizeOfDict; i++){
				nji[classes][i] = dc.nij[i];
				//写入文件保存nij
				writer.write(nji[classes][i]+" ");
			}
		}
		writer.close();
		
        }catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void classify(int test) throws IOException {
		loadNij(test);
		System.out.println("Begin testing...");
		int i, m, et, minBound, maxBound;
		File writeFile = new File("classifyResult_"+String.valueOf(test)+ ".txt");
		FileWriter filewriter = new FileWriter(writeFile, true);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(filewriter);			
		//遍历每个组的测试集
		for( int group = 0; group < GROUPS; group++){
			//System.out.println("test group: "+group);
			et = dc.numOfDocInEachCla[group] / 10;// 第j个类别的每组数据的数目
			minBound = (test - 1) * et;
			maxBound = test * et;
			if (test == 10)
				maxBound = dc.numOfDocInEachCla[group];
			//System.out.println("minBound: " + minBound + " maxBound: " + maxBound);
			String[] textPath = dc.readTextPath(group);
			//遍历测试文档
			for (m = minBound; m < maxBound; m++) {
			// System.out.println(textPath[m]);
				List<String> doc = dc.preprocess(textPath[m]);				
				//确定放大系数M
				if( doc.size() > 500)
					if( doc.size() > 1000)
						M = 250;
					else
						M = 100;
				else
					M = 10;
				//System.out.println(M);
				for( int classes = 0; classes < GROUPS; classes++){					
					//保存分类结果
					pdc[classes] = calcProd(doc,classes);//关键字在分类的条件概率
					//System.out.println(pdc[classes]);
				}
				double min = 0;
				int classifyResult = -1;
				for( int classes = 0; classes < GROUPS; classes++)
					if( pdc[classes] > min ){
						min = pdc[classes];
						classifyResult = classes;
					}
				System.out.println(textPath[m] + " real:"+group+" result: "+ classifyResult);
				//保存分类结果
				
				writer.write(String.valueOf(group)+" "+ String.valueOf(classifyResult));
				writer.newLine();
				writer.flush();
			}
		}
		filewriter.close();
		writer.close();
			
        }catch (IOException e) {
			e.printStackTrace();
		}
	}
}
