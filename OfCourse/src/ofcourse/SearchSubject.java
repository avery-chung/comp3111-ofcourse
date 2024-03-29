package ofcourse;

import java.util.Collection;
import java.util.ArrayList;;

public class SearchSubject extends SearchCourse {

	private ArrayList<String> subjects = new ArrayList<String>();
	
	public SearchSubject(Collection<Course> prevPipe) {
		super(prevPipe);
	}
	
	public SearchSubject(Collection<Course> prevPipe, ArrayList<String> subjects) {
		super(prevPipe);
		this.subjects = subjects;
	}
	

	@Override
	public boolean checkCriteria(Course course) {
		if (subjects == null) return true;
		for(String s : subjects){
			if(course.getCode().getDept().equals(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder result;
		if (prevPipe != null) result = new StringBuilder(prevPipe.toString());
		else result = new StringBuilder("");
		if(subjects == null || subjects.size() == 0) return result.toString();
		result.append("Subject is");
		for (String s : subjects) {
			result.append(" " + s);
		}
		result.append(". ");
		return result.toString();
	}

}
