package ofcoursegui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import ofcourse.Course;
import ofcourse.Network;
import ofcourse.TimePeriod;
import ofcourse.TimetableError;

public class CourseGUI extends JPanel {
	public final HashMap<Integer, Course.Session> linkage = new HashMap<Integer, Course.Session>();
	public final Course course;
	
	JLabel courseCodeLabel = new JLabel("CourseCode");
	JLabel courseNameLabel = new JLabel("CoureName");
	JButton enrollButton = new JButton("Enroll");
	JButton btnAddFav = new JButton("Add to My Favourite");
	public final JTable sessionTable = new JTable();
	@SuppressWarnings("serial")
	DefaultTableModel sessionTableModel = new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Session", "Time", "Room", "Instructor"
			}
			
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			public boolean isCellEditable(int row, int column) {
				return false;
			}
	};
	
	{
		//JPanel coursePanel = new JPanel();
		//MainWindow.searchTabpage.addTab("New tab", null, this, null);
		setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 62, 504, 226);
		add(scrollPane);
		
		sessionTable.setModel(sessionTableModel);
		sessionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sessionTable.setColumnSelectionAllowed(false);
		sessionTable.setRowSelectionAllowed(true);
		
		sessionTable.getColumnModel().getColumn(0).setMaxWidth(55);
		sessionTable.getColumnModel().getColumn(0).setMinWidth(55);
		sessionTable.getColumnModel().getColumn(0).setPreferredWidth(55);
		sessionTable.getColumnModel().getColumn(0).setResizable(false);
		
		sessionTable.getColumnModel().getColumn(2).setPreferredWidth(170);
		
		scrollPane.setViewportView(sessionTable);
		
		courseCodeLabel.setBounds(12, 12, 504, 18);
		add(courseCodeLabel);
		
		courseNameLabel.setBounds(12, 32, 504, 18);
		add(courseNameLabel);
		
		enrollButton.setBounds(418, 533, 98, 28);
		add(enrollButton);
		enrollButton.addActionListener(new EnrollButtonListener());
		
		btnAddFav.setBounds(250, 533, 150, 28);
		add(btnAddFav);
		btnAddFav.addActionListener(new AddFavListener());
	}
	
	private class EnrollButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (MainWindow.timetableTabpage.getSelectedComponent() != MainWindow.own_table.getGUI()) {
				JOptionPane.showMessageDialog(MainWindow.contentPane, "Own timetable tab must be the active tab for the operation.");
				return;
			}
			int[] selecteds = CourseGUI.this.sessionTable.getSelectedRows();
			ArrayList<Course.Session> ss = new ArrayList<Course.Session>();
			for(int i : selecteds) {
				for (Integer j : linkage.keySet()) {
					if (i == j) {
						ss.add(CourseGUI.this.linkage.get(i));
					}
				}
			}

			TimetableError err_code = MainWindow.own_table.addCourse(course, ss.toArray(new Course.Session[ss.size()]));
			MainWindow.showError(err_code, "Enroll Fails");
		}
	}
	
	private class AddFavListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!MainWindow.haveLogined()) {
				MainWindow.showNotLoginError();
				return;
			}
			String newFav = CourseGUI.this.course.getCode().toString();
			Network network = Network.getOurNetwork();
			String myfav = network.getMyFav();
			String[] favCs = myfav.split("!");
			for (String str : favCs) {
				if (str.equals(newFav)) {
					JOptionPane.showMessageDialog(MainWindow.contentPane, "Course already in My Favourite.",
							"Add to My Favourite", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
			myfav = myfav + newFav + "!";
			String returnCode = network.setMyFav(myfav);
			if (!returnCode.equals("100")) {
				JOptionPane.showMessageDialog(MainWindow.contentPane, "Operation Fails!",
						"Add to My Favourite", JOptionPane.WARNING_MESSAGE);
			}
			else {
				MainWindow.updateFavNeeded.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
				JOptionPane.showMessageDialog(MainWindow.contentPane, "Course successfully added to My Favourite.",
						"Add to My Favourite", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	public CourseGUI (Course c) {
		if (c == null) throw new NullPointerException();
		setCourse(c);
		this.course = c;
	}
	
	/*public void setCoureCodeText(String couseCode) {
		courseCodeLabel.setText(couseCode);
	}	
	public void setCoureNameText(String couseCode) {
		courseNameLabel.setText(couseCode);
	}*/
	private void setCourse(Course c) {
		courseCodeLabel.setText(c.getCode().toString());
		courseNameLabel.setText(c.getName());
		for (Course.Session s : c.getSessions()) {
			int numOfDistinctTime = 0;
			ArrayList<ArrayList<Integer>> arr =  new ArrayList<ArrayList<Integer>>();
			for (TimePeriod tp : s.getSchedule()) {
				int[] start_end = tp.getStartEndID();
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				tmp.add(start_end[0]%100);
				tmp.add(start_end[1]%100);
				if (!arr.contains(tmp)){
					numOfDistinctTime++;
				}
				arr.add(tmp);
			}
			if (numOfDistinctTime==0) numOfDistinctTime=1; // Time = TBA
			sessionTableModel.addRow(new String[] {s.toString(), s.getSchedule().toString(), s.getRoom().toString(), s.getInstructors().toString()});
			sessionTable.setRowHeight(sessionTableModel.getRowCount()-1, MainWindow.RowHeight*numOfDistinctTime);
			linkage.put(sessionTableModel.getRowCount()-1, s);
		}
	}
	
	// return sessions selected, course of sessions can be obtained by class no
	public Course.Session[] getSelectedSessions() {
		int[] selecteds = CourseGUI.this.sessionTable.getSelectedRows();
		ArrayList<Course.Session> ss = new ArrayList<Course.Session>();
		for(int i : selecteds) {
			for (Integer j : linkage.keySet()) {
				if (i == j) {
					ss.add(CourseGUI.this.linkage.get(i));
				}
			}
		}
		return ss.toArray(new Course.Session[ss.size()]);
	}
}
