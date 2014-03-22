package jobs;

import java.io.*;
import common.*;

//Matrix Vector Multiplication
public class Mvmdouble extends Job {

  @Override
  public void config() {
    setNumTasks(4);
  }

  @Override
  public void task(int tId) {
    try {
      int nRow, nCol;
      double[][] mat;
      double[] vec;
      double[] product;

      File file;
      BufferedReader reader;
      PrintWriter printWriter;
      String line;
      String[] elements;

      //read the shared matrix file
      file = new File("fs/example/matrix_double.txt");
      reader =  new BufferedReader(new FileReader(file));
      line = reader.readLine(); //the first line is dimension
      elements = line.split(" ");
      nRow = Integer.parseInt(elements[0]);
      nCol = Integer.parseInt(elements[1]);

      mat = new double[nRow][nCol];
      for(int i=0; i<nRow; i++) {
        line = reader.readLine();
        elements = line.split(" ");
        for(int j=0; j<nCol; j++) {
          mat[i][j] = Double.parseDouble(elements[j]);
        }
      }
      reader.close();

      //read specific vector file based on task ID
      file = new File("fs/example/vector_double"+tId+".txt");
      reader =  new BufferedReader(new FileReader(file));
      line = reader.readLine();
      elements = line.split(" ");
      if(Double.parseDouble(elements[0]) != nCol || Double.parseDouble(elements[1]) != 1)
        throw new Exception("dimension error");

      vec = new double[nCol];
      for(int i=0; i<nCol; i++) {
        line = reader.readLine();
        vec[i] = Double.parseDouble(line);
      }
      reader.close();

      //calculate the produc
      product = new double[nRow];
      for(int i=0; i<nRow; i++) {
        product[i] = 0;
        for(int j=0; j<nCol; j++) {
          product[i] += mat[i][j] * vec[j];
        }
      }
     
      //write output file
      file = new File("fs/example/out_double"+tId+".txt");
      printWriter = new PrintWriter(file);

      printWriter.println(nRow+" 1");
      for(int i=0; i<nRow; i++) {
        printWriter.println(product[i]);
      }
      printWriter.close();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
