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
package matrix.factory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import matrix.matrixImpl.Matrix;
import matrix.matrixImpl.MatrixSparseTrovePar;

/**
 * The matrix constructed by this factory is a subclass of the matrix created by {@link FactoryMatrixTrove}.
 * The only difference is that this sub class implements {@link Matrix#fastMult(Matrix)} and {@link Matrix#fastMult(float)}
 * in a multi-threaded fashion.
 * @see MatrixSparseTrovePar
 * @author Antonela Tommasel
 *
 */
public class FactoryMatrixTrovePar implements FactoryMatrix{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5746045203835894200L;

	@Override
	public Matrix createMatrix(int rowSize, int colSize) {
		return new MatrixSparseTrovePar(rowSize,colSize);
	}

	@Override
	public Matrix createMatrix(Matrix toCopy) {
		return new MatrixSparseTrovePar(toCopy);
	}

	@Override
	public Matrix createIdentity(int rowSize) {
		Matrix identity = new MatrixSparseTrovePar(rowSize,rowSize);
		for(int i=0;i<rowSize;i++)
			identity.setValue(i, i, 1);
		return identity;
	}

	@Override
	public Matrix createMatrix(String pathToCopy) {

		BufferedReader io;
		Matrix loaded = null;
		try {
			io = new BufferedReader(new InputStreamReader(new FileInputStream(pathToCopy)));
			String l = io.readLine();
			String [] sizes = l.split(",");
			loaded = new MatrixSparseTrovePar(Integer.parseInt(sizes[0]),Integer.parseInt(sizes[1]));
			if(sizes.length == 2){ //dense csv format
				l = io.readLine();
				int i = 0;
				while(l!=null){
					String [] row = l.split(",");
					for(int j=0;j<row.length;j++)
						loaded.setValue(i, j, Float.parseFloat(row[j]));
					l = io.readLine();
					i++;
				}
			}else{
				if(sizes.length == 3){ //sparse csv format
					l = io.readLine();
					while(l!=null){
						String [] element = l.split(",");
						loaded.setValue(Integer.parseInt(element[0]),Integer.parseInt(element[1]), Float.parseFloat(element[2]));
						l = io.readLine();
					}
				}
			}
			io.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return loaded;
	}

}
