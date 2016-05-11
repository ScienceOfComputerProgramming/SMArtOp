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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Implements the methods to load the data belonging to the Digg Dataset
 * @author Antonela Tommasel
 *
 */
public class DiggParser extends Parser{

	private static final long serialVersionUID = 1L;
	String pathUsers;
	String pathPosts;

	/**
	 * @param pathDirectory path to the directory where the dataset files are stored
	 * @param stopWords path to the stopwords file
	 */
	public DiggParser(String pathDirectory, String stopWords){
		super();
		try {
			loadStopWords(stopWords);
		} catch (Exception e) {
			e.printStackTrace();
		}

		pathUsers = pathDirectory + File.separator+"data"+File.separator+"texts"+File.separator+"user_contact_v1.m";
		pathPosts = pathDirectory + File.separator+"data"+File.separator+"texts"+File.separator+"user_submit_story_keyword_topic_v1.m";
	}

	/* 
	 * Each line of the file contains <id_user> <id_user>
	 * Modifies userUser
	 * */
	@Override
	public void buildUserRelationships() throws IOException {

		File f=new File(pathUsers);
		BufferedReader io=new BufferedReader(new FileReader(f));
		String l=io.readLine();

		String [] aux=null;
		while(l!=null){

			aux=l.split(" ");
			if(userUser.containsKey(aux[0]))
				userUser.get(aux[0]).add(aux[1]);
			else{
				userUser.put(aux[0], new ArrayList<String>());
				userUser.get(aux[0]).add(aux[1]);
			}

			if(!users.containsKey(aux[0])){
				users.put(aux[0],users.size());
			}

			if(!users.containsKey(aux[1])){
				users.put(aux[1],users.size());
			}

			l=io.readLine();
		}
		io.close();
	}

	/* 
	 * The file to read is: user_submit_story_keyword_topic from which it can be inferred (topic==class)
	 * Each line contains: <user_id> <story_id> <keyword_id> <topic_id> 
	 * Each <user_id> <story_id> appears as many times as keywords has the story
	 * 
	 * This methods modifies:
	 *   --> userPosts
	 *   --> postFeatures
	 *   --> postClass
	 */
	@Override
	public void buildPostStructure() throws IOException {

		File f=new File(pathPosts);
		BufferedReader io=new BufferedReader(new FileReader(f));
		String l=io.readLine();
		String [] aux=null; //<user_id> <story_id> <keyword_id> <topic_id>
		HashSet<String> p=null;
		HashMap<String,Integer> pf=null;

		while(l!=null){
			aux=l.split(" ");

			//Adding the post to the user
			if(userPost.containsKey(aux[0])){
				p=userPost.get(aux[0]);
				if(!p.contains(aux[1])){ 
					p.add(aux[1]);
					posts.put(aux[1],posts.size());
				}
			}
			else{
				p=new HashSet<String>();
				p.add(aux[1]);
				userPost.put(aux[0], p);
				posts.put(aux[1],posts.size());

				if(!users.containsKey(aux[0]))
					users.put(aux[0],users.size());
			}

			p=null;

			//Adding features and topic
			if(!features.containsKey(aux[2]))
				features.put(aux[2],features.size());

			if(postFeatures.containsKey(aux[1])){
				pf=postFeatures.get(aux[1]);
				pf.put(features.get(aux[2]).toString(),1); //frequency info is assumed to be 1 for this dataset
			}
			else{
				pf=new HashMap<String, Integer>();
				pf.put(features.get(aux[2]).toString(), 1);
				postFeatures.put(aux[1],pf);

				postCats.put(aux[1], aux[3]);
			}

			if(!clases.containsKey(aux[3]))
				clases.put(aux[3], clases.size());


			l=io.readLine();
		}
		io.close();	

	}

}
