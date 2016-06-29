/* 
 *  Copyright 2016 ISISTAN - UNICEN - CONICET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.matrixCreation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import matrix.factory.FactoryMatrixHolder;
import matrix.factory.FactoryMatrixSparseHash;
import matrix.matrixImpl.Matrix;

/**
 * Example of how to serialise and deserialise a Matrix
 * @author Anto
 *
 */
public class TestSerialisation {

	/**
	 * @param args no parameters are needed
	 */
	public static void main(String[] args) {

		//setting of the matrix holder
		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());

		//Manually creating a Matrix
		Matrix A = FactoryMatrixHolder.getFactory().createIdentity(3);

		//Serialise the Matrix
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("matrixA.bin"));
			out.writeObject(A);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Serialise and Compress the Matrix
		try {
			FileOutputStream fileOut = new FileOutputStream("matrixA.gzip");
			GZIPOutputStream gz = new GZIPOutputStream(fileOut);
			ObjectOutputStream seri = new ObjectOutputStream(gz);
			seri.writeObject(A);
			seri.flush();
			seri.close();
			gz.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Deserialising
		ObjectInputStream in;
		Matrix B = null;
		try {
			in = new ObjectInputStream(new FileInputStream("matrixA.bin"));
			B = (Matrix) in.readObject();
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println(B);
		
		//Deserialising and decompressing
		Matrix C = null;
		try {
			FileInputStream fileIn = new FileInputStream("matrixA.gzip");
			GZIPInputStream gz = new GZIPInputStream(fileIn);
			ObjectInputStream in1 = new ObjectInputStream(new BufferedInputStream(gz));
			C = (Matrix) in1.readObject();
			in1.close();
			gz.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println(C);
		
	}

}