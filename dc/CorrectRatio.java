package dc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CorrectRatio 
{
	public static void main(String[] args) throws IOException {
		File file = new File("classifyResult_10.txt");
		FileReader reader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line;
		String num[];
		int a, b;
		int right = 0;
		int wrong = 0;
		int uncertain = 0;
		while(true)
		{
			line = bufferedReader.readLine();
			if(line == null)
				break;
			num = line.split(" ");
			a = Integer.parseInt(num[0]);
			b = Integer.parseInt(num[1]);
			if(a == b)
				right ++;
			else if(b == -1)
				uncertain ++;
			else
				wrong ++;
		}
		System.out.print("right:"+right+"  wrong:"+wrong+"  uncertain:"+uncertain+"  correct ratio:");
		System.out.print((float)right/(float)(right+wrong+uncertain));
		reader.close();
		bufferedReader.close();
	}
}