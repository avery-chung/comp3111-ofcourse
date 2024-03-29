package ofcourse;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This class represents a list of courses with the same subject, or 
 * department, or precisely, start with the same 4-character code in 
 * their course codes. 
 * 
 * @author Kelvin Kong
 *
 */
public class CourseParse {
	private String subject;
	private ArrayList<Course> courses = new ArrayList<Course>();
	public final static String URL = "https://w5.ab.ust.hk/wcq/cgi-bin/1410/subject/";
	public final static long expireTime = 15 * 60 * 1000; //TTL for the local cache of quota page , 15 min
	//14 summer would change to /1340/, fall would change to /1410/, 
	//need to figure out some ways to change semester
	
	//

	/**
	 * Create a Course Parse.
	 */
	public CourseParse() {

	}
	
	
	/**
	 * Gets the subject name of the courses in this CourseParse
	 * @return
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Sets the subject name of the courses in this CourseParse
	 * @param subject The subject name to be set. 
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * Gets all courses in this CourseParse in an ArrayList.
	 * @return ArrayList of all courses in this CourseParse. 
	 * Any modification of the ArrayList is reflected and stored in this CourseParse.
	 */
	public ArrayList<Course> getCourses() {
		return courses;
	}

	/**
	 * Add a <tt>Course</tt> to this courseParse
	 * 
	 * @param c <tt>Course</tt> object to be added. The course should have the same subject with this courseParse.
	 */
	public void add(Course c) {
		courses.add(c);
	}

	/**
	 * Find the course with the provided course code in <tt>String</tt>.
	 * 
	 * @param course A String represents the course code of the target course.
	 * @return a <tt>Course</tt> object with the provided Course code.  If no course is matched, null is returned instead.
	 */
	public Course findByCode(String course) {			//Find the course with code
		for (Course cp : courses) {
			if (cp.getCode().toString().equals(course))
				return cp;
		}
		return null;
	}

	/**
	 * Parse the courses of the same specified subject code, and return a <tt>courseParse</tt> that contains all these courses.
	 * Therefore, all courses in this <tt>courseParse</tt> object have the same subject.
	 * @param subject The subject code of courses that need to be parsed
	 * @return A <tt>courseParse</tt> that contains all the courses of the specified subject code
	 */
	public static CourseParse parse(String subject) {
		CourseParse cp = new CourseParse();
		Document doc = null;
		cp.subject = subject;
		//////
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss:SSS");
		System.out.println(ft.format(dNow));
		System.out.println(subject);								//Just used for time chking, nothing else
		
		

		java.io.File cache = new java.io.File(subject + ".txt");
		java.io.File history = new java.io.File("history.txt");
		java.io.PrintWriter historywriter = null;
		
		

		try {
			boolean updateNeeded = false;
			if (!cache.exists()) updateNeeded = true;
			
			//Read whole file as a String
			StringBuilder historyContent = null;
			if (history.exists()) {
				java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(history));
				String result = br.readLine();
				br.close();
				if (result != null && result.length() > 0)
					historyContent = new StringBuilder(result);			
				else updateNeeded = true;
				// = new StringBuilder(s.useDelimiter("\\Z").next());
			}
			else history.createNewFile();
			//''
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			int start = -1, end = -1;
			if (historyContent != null && historyContent.length() > 0) {
				Date d1 = null;
				//Find subject last update time
				start = historyContent.indexOf(subject);
				end = historyContent.indexOf("", start);
				if (start == -1) //this subject is not here
					updateNeeded = true;
				if (end == -1) { //parse error
					System.out.println("parse error");
					throw new IOException();
				}
				if(start != -1){
					//start to end include whole entry
					//start + subject.length() to  end incude date and time
					d1 = dateFormat.parse(historyContent.substring(start + subject.length(), end));
					//in milliseconds 
					long diff = dNow.getTime() - d1.getTime();
					@SuppressWarnings("unused")
					long diffMinutes = diff / (60 * 1000) % 60;
					@SuppressWarnings("unused")
					long diffHours = diff / (60 * 60 * 1000) % 24;
					@SuppressWarnings("unused")
					long diffDays = diff / (24 * 60 * 60 * 1000);
					if (diff >= expireTime) {
						updateNeeded = true;
					}
				}
			}
						
			if (updateNeeded){//Get, save in cache
				System.out.println("Update is needed.");
				if(historyContent == null) historyContent = new StringBuilder("");
				//Get
				doc = Jsoup.connect(URL + subject).get();
				//save in cache
				java.io.PrintWriter writer = new java.io.PrintWriter(cache);
				writer.print(doc.outerHtml());
				writer.close();
				//Update last update time
				if (start != -1) {//this subject is here: remove entry
					historyContent.delete(start, end + 1);
				}
				//create entry at the end
				historyContent.append(subject);
				historyContent.append(dateFormat.format(dNow));
				historyContent.append("");
				//Write back
				historywriter = new java.io.PrintWriter(history);
				historywriter.print(historyContent);
				historywriter.close();
			}
			else {//Read cache
				System.out.println("Cache is used.");
				doc = Jsoup.parse(cache, "UTF-8", URL);
			}
			//doc = cacheManager.getDocument(subject);
			Elements cs = doc.select("#classes .course");
			for (Element courseE : cs) {
				//System.out.println(courseE.toString());
				Course temp = new Course(courseE); // Parse course by course
				// temp.out();
				cp.add(temp);
			}
			
			//After that, parse the avg ratings into the course
			Network a=Network.getOurNetwork();
			String[][] RatingSummary=a.getSummary(subject); //{{COMP0001,3.000},{COMP0002,4.233},...}
			a.printArray(RatingSummary);
			try{
			for(int i=0;i<RatingSummary.length;i++)cp.findByCode(RatingSummary[i][0]).setAvgRating(Float.parseFloat(RatingSummary[i][1]));
			}catch (NullPointerException e){
				//if found an invalid course(comp6666), do nothing
				System.out.println("cannot found course");
			}
			
		} 
		catch (IOException e) {
			System.out.println("Connection error");
			e.printStackTrace();
		} 
		catch (ParseException e) {
			e.printStackTrace();
		}
		return cp;
	}

	/**
	 * Parse all courses of all subjects in quota page automatically.
	 * @return An ArrayList<> of courseParse objects, each represents a list of courses with the same subject.
	 * @throws IOException 
	 */
	public static ArrayList<CourseParse> fullparse() {
		ArrayList<CourseParse> cpA = new ArrayList<CourseParse>();
		Document doc;
		String Subject = "COMP";
		ArrayList<String> SubjectList = new ArrayList<String>();
		
		try {	
			doc = Jsoup.connect(URL + Subject).get();
			//writer.println(doc.outerHtml());
			//doc = Jsoup.parse(output, "UTF-8", "http://example.com/URL");
			Elements courses = doc.select("#navigator .depts a");
			for (Element courseE : courses) {
				SubjectList.add(courseE.text());					//It first find a list of majors from /COMP,
			}
			for (String subject : SubjectList) {
				//if(!(subject.equals("COMP") || subject.equals("HUMA") || subject.equals("MATH"))) continue;
				CourseParse cp=parse(subject);						// then it parse major by major, each put in a seperate obj.
				cpA.add(cp);
			}

		} catch (IOException e) {
			System.out.println("Connection error");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cpA;
	}
	
	/**
	 * Searches in the provided ArrayList of CourseParse to find the first occurrence that has the specified subject/major name.
	 * @param cpA The ArrayList of CourseParse.
	 * @param Major The subject/major name to search.
	 * @return The first occurrence of CourseParse that has the specified subject/major name. Null if none.
	 */
	public static CourseParse searchP(ArrayList<CourseParse> cpA, String Major) {

		for (CourseParse cp : cpA) {
			if (cp.subject.equals(Major)) {				//Search for major and return the obj contains the courses of that major.
				return cp;
			}
		}
		return null;

	}

	/**
	 * Searches in the provided ArrayList of CourseParse to find the first Course that has the specified course code, in String.
	 * @param cpA The ArrayList of CourseParse.
	 * @param Course The specified course code in String. If it does not contains any mofiers, a space should be included as the last character.
	 * @return
	 */
	public static Course search(ArrayList<CourseParse> cpA, String Course) {
		Course target = null;

		for (CourseParse cp : cpA) {
			if (cp.subject.equals(Course.substring(0, 4))) {				//For example, if you search for COMP2012, it will find the List of COMP courses, then call findByCourse
				target = cp.findByCode(Course);
				return target;
			}
		}
		return null;

	}
/*
	public static void main(String[] args) {
		ArrayList<CourseParse> cpA = new ArrayList<CourseParse>();
		cpA = fullparse();											//This line parse everything into the arraylist of Majors
		Course target = null;
		while (true) {												//Endless loop of searching and showing course
			try {
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						System.in));
				System.out.print("Lesson to search (COMP2012 ): ");	//Note that if you search course with out modifier(the H in COMP2012H, you MUST add a ' ' after it. eg:"COMP2012 ")
				String text = buf.readLine();
				if(text.length()==8) text = text + " ";
				if(text.length()==9){									//if search COMP, list summary of COMP courses, if search COMP2012, list all course info for COMP2012
					target = search(cpA, text);							//search function
					if (target == null) {
						System.out.println("Not Found");
						continue;
					}
					out(target);										//output
				}														//good case to test: ELEC1100,1200,LANG1003S,SCIE1500,ENVR2001(go check Course line 244),ENVR6990
				else if(text.length()!=4) {
					System.out.println("Invalid. Must be 9 char long for cource code search or 4 char long for subject search.");
					continue;
				}
				else{
					CourseParse result = searchP(cpA,text);
					if (result == null) {
						System.out.println("Not Found");
						continue;
					}
					out(result);
				}
				

				
			} catch (NullPointerException e) {
				System.out.println("Error");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	private static void out(CourseParse cp){
		for(Course c:cp.courses)System.out.println(c.getName()+"\t"+c.getMaxWaitList()+"\t");//+c.rating
		//Name	MaxWaitList	rating(TODO), anything else? (interestingly, ALL courses are waitlist 0)
		//possible to sum all quota from lectures?
	}
	
	//These are outputs from the course obj, used to chk whether they are parsed correctly or not
	private static void out(Course t) {
		System.out.println("Name: "+t.getName());
		System.out.println("Code: "+t.getCode().toString());
		System.out.println("Match session: "+t.isMatchSession());
		System.out.println("Description: "+t.getDescription());
		System.out.println("Avg rating:"+t.getAvgRating());
		System.out.println("Attributes: "+t.getAttributes());		//show only if not null?
		System.out.println("Co-List with: "+t.getCoList());			//
		System.out.println("PreRequsite: "+t.getPreRequisite());	//
		System.out.println("CoRequsite: "+t.getCoRequisite());		//
		System.out.println("Exclusion: "+t.getExclusion());			//
		System.out.println("Previous code: "+t.getPreviousCode());	//
		//avg rating would be shown hare
		for(Session s : t.getSessions()){
			System.out.println("Session: "+s.getSType().toString()+"\t"+s.getSNo()+"\t"+s.getSet()+"\t"+s.getClassNo()+"\t"+s.getQuota()+"\t"+s.getEnrol()+"\t"+s.getavailableQuota()+"\t"+s.getwait()+"\t"+s.getRemarks());
			Set<Instructor> instructor=s.getInstructors();
			for(Instructor i:instructor)System.out.print(i.getName()+"\t");//empty if TBA
			System.out.println();
			Set<String> room=s.getRoom();
			for(String r:room)System.out.print(r+"\t");
			System.out.println();
			Set<TimePeriod> time=s.getSchedule();
			System.out.println("Time: ");
			for(TimePeriod tp:time)System.out.println("From " +  tp.getStartEndID()[0]+"\t to: "+tp.getStartEndID()[1]);//empty if TBA
			System.out.println("SessionString: " + s.toString());
			System.out.println();
		}
		t.parseComments();
		System.out.println("Comments: ");
		for(Comments c:t.getComments()){
			System.out.println("By: "+c.getCommentorName());
			System.out.println("Rating: "+c.getRating());
			System.out.println("Comment: "+c.getComments());
			System.out.println("Date: "+c.getDate());
		}
		//comments and ratings here
	}
*/}


/*
jsoup License
The jsoup code-base (include source and compiled packages) are distributed under the open source MIT license as described below.
The MIT License
Copyright  2009 - 2013 Jonathan Hedley (jonathan@hedley.net)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/