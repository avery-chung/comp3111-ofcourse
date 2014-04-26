package test.gui;

import ofcoursegui.MainWindow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class MyFavPanelGUITest extends UISpecTestCase {

	private Window win = null;
	
	{
		UISpec4J.init();
	}
		
	@Before
	public void setUp() throws Exception {
		this.setAdapter(new MainClassAdapter(MainWindow.class, new String[0]));
		win = this.getMainWindow();
		//Network.login("ctestdab", "bbb");
		WindowInterceptor.init(win.getMenuBar().getMenu("Account").getSubMenu("Login").triggerClick())
		.process(new WindowHandler("LoginGUI") {
				public Trigger process(Window dialog) {
					assertTrue(dialog.titleEquals("Login"));
					dialog.getInputTextBox("username").setText("ctestdab");
					dialog.getPasswordField("password").setPassword("bbb");
					return dialog.getButton("Login").triggerClick();
				}
		})
		.run();
	}
		
	@After
	public void tearDown() throws Exception {
		logout();
	}
	
	private void logout() {
		WindowInterceptor.run(win.getMenuBar().getMenu("Account").getSubMenu("Logout").triggerClick());
	}
	
	@Test
	public void testMyFavPanel() {
		// the account must have at least 2 favourite course for this test
		win.getTabGroup("searchTabpage").selectTab("My Favourite");
		assertTrue(win.getTabGroup("searchTabpage").getSelectedTab().getListBox("myfavlist").getSize()>=2);
		win.getTabGroup("searchTabpage").getSelectedTab().getListBox("myfavlist").selectIndex(0);
		String course_selected = win.getTabGroup("searchTabpage").getSelectedTab().getListBox("myfavlist").getAwtComponent().getSelectedValue().toString();
		win.getTabGroup("searchTabpage").getSelectedTab().getButton("View Course Details").click();
		assertTrue(win.getTabGroup("searchTabpage").getSelectedTab().getDescription().contains(course_selected.substring(0, 11)));
		win.getTabGroup("searchTabpage").selectTab("My Favourite");
		win.getTabGroup("searchTabpage").getSelectedTab().getListBox("myfavlist").selectIndex(0);
		int oldsize = win.getTabGroup("searchTabpage").getSelectedTab().getListBox("myfavlist").getSize();
		win.getButton("Remove from My Favourite").click();
		int newsize = win.getTabGroup("searchTabpage").getSelectedTab().getListBox("myfavlist").getSize();
		assertTrue(newsize == oldsize-1);
	}
	
}