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
package test.featureSelection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import matrix.adapterDistribution.Configuration;
import matrix.factory.FactoryMatrixHolder;
import matrix.factory.FactoryMatrixSparseHash;
import matrix.matrixImpl.Matrix;

/**
 * Abstract class to encapsulate the common behaviour of data parsers
 * @author Antonela Tommasel
 *
 */
public abstract class Parser implements Serializable, IntData{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	HashSet<String> stopwords=new HashSet<String>();

	//Mapping of Matrix indexes 
	HashMap<String,Integer> users;
	HashMap<String,Integer> posts;
	HashMap<String,Integer> features; 
	HashMap<String,Integer> clases;

	//Data structures related to posts
	HashMap<String,HashSet<String>> userPost; //<user, <published posts> >
	HashMap<String,ArrayList<String>> userUser; //<user, <related users> >
	HashMap<String,HashMap<String,Integer>> postFeatures; // <posts, <feature,frequency> >
	HashMap<String,String> postCats;


	//Matrices to be used for the feature selection hypothesis
	transient Matrix XpostFeature=null;
	transient Matrix YpostCats=null;
	transient Matrix SuserUser=null;
	transient Matrix H=null;
	transient Matrix PcoPost=null;


	ArrayList<String> postsId=null;

	/**
	 * Class Constructor.
	 * It initialises all the data structures
	 */
	public Parser(){
		users=new HashMap<String, Integer>();
		posts=new HashMap<String, Integer>();
		features=new HashMap<String, Integer>();
		clases=new HashMap<String, Integer>();

		userPost=new HashMap<String, HashSet<String>>();
		userUser=new HashMap<String, ArrayList<String>>();
		postFeatures=new HashMap<String, HashMap<String,Integer>>();
		postCats=new HashMap<String, String>();
	}

	protected void loadStopWords(String ruta) throws Exception{
		File f=new File(ruta);
		BufferedReader io=new BufferedReader(new FileReader(f));
		String l=io.readLine();
		while(l!=null){
			stopwords.add(l.toLowerCase());
			l=io.readLine();
		}
		io.close();
	}

	//The default implementation assumes that both matrices are equivalent as data is fully labelled. 
	public Matrix getFpostFeature(){
		return getXpostFeature();
	}

	public Matrix getXpostFeature(){ // [post,feature] = frequency

		Configuration.logger.info("Starts X "+new Date());

		if(XpostFeature==null){

			XpostFeature = FactoryMatrixHolder.getFactory().createMatrix(features.size(),posts.size());

			HashMap<String,Integer> f = null;

			for(String k:postFeatures.keySet()){
				f=postFeatures.get(k);
				for(String e:f.keySet())
					XpostFeature.setValue(Integer.parseInt(e), posts.get(k), f.get(e));
			}
		}

		Configuration.logger.info("Finishes X "+new Date());

		return XpostFeature;

	}

	public Matrix getYpostCats(){ // [post][class] = 1 if post belong to class

		Configuration.logger.info("Starts Y "+new Date());
		if(YpostCats==null){
			YpostCats=FactoryMatrixHolder.getFactory().createMatrix(posts.size(),clases.size());

			for(String k:postCats.keySet())
				YpostCats.setValue(posts.get(k), clases.get(postCats.get(k)), 1);

		}
		Configuration.logger.info("Finishes Y "+new Date());
		
		return YpostCats;


	}

	//Asymmetric user relationships
	public Matrix getSuserUser(){ // [user_i][user_j] = 1 if user_i follows user_j

		Configuration.logger.info("Starts S "+new Date());
		if(SuserUser==null){

			SuserUser=FactoryMatrixHolder.getFactory().createMatrix(users.size(), users.size());

			ArrayList<String> u=null;
			for(String k:userUser.keySet()){
				u=userUser.get(k);
				for(int i=0;i<u.size();i++){
					SuserUser.setValue(users.get(u.get(i)), users.get(k), 1);
				}

			}

		}
		Configuration.logger.info("Finishes S "+new Date());
		return SuserUser;

	}

	public Matrix getPcoPost(){ //user,posts
		Configuration.logger.info("Starts P "+new Date());
		if(PcoPost==null){
			PcoPost=FactoryMatrixHolder.getFactory().createMatrix(users.size(),posts.size());
			HashSet<String> u=null;

			for(String k:userPost.keySet()){
				u=userPost.get(k);
				for(String i:u)
					PcoPost.setValue(users.get(k), posts.get(i), 1);
			}
		}
		Configuration.logger.info("Finishes P "+new Date());
		return PcoPost;
	}

	public Matrix getH() {
		Configuration.logger.info("Starts H "+new Date());
		if(H==null){
			H=FactoryMatrixHolder.getFactory().createMatrix(posts.size(), users.size());

			HashSet<String> u=null;

			for(String k:userPost.keySet()){
				u=userPost.get(k);
				for(String i:u)
					H.setValue(posts.get(i), users.get(k), 1.0f/(float) u.size());
			}
		}
		Configuration.logger.info("Finishes H "+new Date());
		return H;
	}

	/**
	 * Builds the user relations of the dataset
	 * @throws IOException opens and reads files
	 */
	public abstract void buildUserRelationships() throws IOException;

	/**
	 * Builds all the information related to features, class and ownership of posts
	 * @throws IOException opens and reads files
	 */
	public abstract void buildPostStructure() throws IOException;

	/**
	 * @return the number of posts in the dataset
	 */
	public int getNumPosts() {
		return posts.size();
	}

	/**
	 * @return the list of posts ids
	 */
	public ArrayList<String> getPostId(){
		if(postsId==null)
			postsId=new ArrayList<String>(posts.keySet());

		return postsId;
	}

	/**
	 * @param id post from which the features want to be obtained
	 * @return class corresponding to the id passed as parameter
	 */
	public String getClassXid(String id) {
		return postCats.get(id);
	}

	/**
	 * @param id post from which the features want to be obtained
	 * @return features corresponded to the id passed as parameter
	 */
	public HashMap<String, Integer> getFeaturesxId(String id) {
		return postFeatures.get(id);
	}

	/**
	 * @return the total number of features in the dataset
	 */
	public int getNumFeatures(){
		return features.size();
	}

	private int getNumUsers() {
		return users.size();
	}

	private int getNumClases() {
		return clases.size();
	}

	/**
	 * Allows to delete all the information stored in the Parser instance 
	 */
	public void destroy(){
		stopwords=null;

		users=null;
		posts=null;
		features=null; 
		clases=null;

		userPost=null; 
		userUser=null; 
		postFeatures=null;
		postCats=null;

	}

	/**
	 * Parser testing
	 * @param args dataset path and stopword path
	 */
	public static void main(String [] args){

		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());

		args = new String[2];
		args[0] = "C:/Users/Anto/Desktop/datasets/kdd09sup";
		args[1] = args[0]+File.separator+"stopwords.txt";
		
		String path = args[0];
		String stopwordPath = args[1];
		
		Parser p = new DiggParser(path,stopwordPath);

		try {
			p.buildUserRelationships();
			p.buildPostStructure();
		} catch (IOException e) { 
			e.printStackTrace();
		}
		
		System.out.println(p.toString());
		System.out.println(p.getNumFeatures());
		System.out.println(p.getNumPosts());
		System.out.println(p.getNumClases());
		System.out.println(p.getNumUsers());

		System.out.println(new Date());
		System.out.println(p.getFpostFeature().rowSize());
		System.out.println(new Date());
		System.out.println(p.getH().rowSize());
		System.out.println(new Date());
		System.out.println(p.getPcoPost().rowSize());
		System.out.println(new Date());
		System.out.println(p.getSuserUser().rowSize());
		System.out.println(new Date());
		System.out.println(p.getXpostFeature().rowSize());
		System.out.println(new Date());
		System.out.println(p.getYpostCats().rowSize());
		System.out.println(new Date());

	}

}
