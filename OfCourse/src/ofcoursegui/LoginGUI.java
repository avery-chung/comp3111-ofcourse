package ofcoursegui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ofcourse.Network;

@SuppressWarnings("serial")
public class LoginGUI extends JDialog {
	
	private Frame parent = null;
	
	public LoginGUI(final Frame parent) {
		super(parent, true);
		this.parent = parent;
		this.setSize(300, 160);
		this.setLocationRelativeTo(parent);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setTitle("Login");
		JLabel msg1 = new JLabel("Username: ");
		JLabel msg2 = new JLabel("Password: ");
		msg1.setPreferredSize(new Dimension(80, 30));
		msg2.setPreferredSize(new Dimension(80, 30));
		JButton btnLogin = new JButton("Login");
		final JTextField usernameTextField = new JTextField(10);
		final JPasswordField passwordField = new JPasswordField(10);
		
		usernameTextField.setName("username");
		passwordField.setName("password");
		
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = usernameTextField.getText();
				String password = new String(passwordField.getPassword());
				Network network = Network.login(username, password);
				if (network.chkFirstPW()) { // first login, using first PW
					LoginGUI.this.setVisible(false);
					new ChangePwGUI((Frame) LoginGUI.this.parent, password, true);
					dispose();
					if (!MainWindow.haveLogined()){ // user cancels input
						JOptionPane.showMessageDialog(parent, "Login only available after set up a new password.");
					}
					else {
						MainWindow.loginAs.setText("Currently Login As: "+username);
						MainWindow.own_table.importString(network.getTimeTable());
						JOptionPane.showMessageDialog(parent, "Login Successfully.",
								"Congratulations!", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else { // not first login
					if (!MainWindow.haveLogined()){ // login fails
						JOptionPane.showMessageDialog(parent, "Incorrect Username or Wrong Password!",
								"Login Fails", JOptionPane.WARNING_MESSAGE);
					}
					else { // login successfully
						dispose();
						MainWindow.loginAs.setText("Currently Login As: "+username);
						MainWindow.own_table.importString(network.getTimeTable());
						JOptionPane.showMessageDialog(parent, "Login Successfully.",
								"Congratulations!", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			
		});
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(8, 10, 10, 10));
		this.setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(3, 1));
		JPanel p1 = new JPanel();
		p1.add(msg1);
		p1.add(usernameTextField);
		contentPane.add(p1);
		JPanel p2 = new JPanel();
		p2.add(msg2);
		p2.add(passwordField);
		contentPane.add(p2);
		JPanel p3 = new JPanel();
		p3.add(btnLogin);
		contentPane.add(p3);
		contentPane.getRootPane().setDefaultButton(btnLogin);
		contentPane.setVisible(true);
		MainWindow.addEscapeListener(this);
		this.setVisible(true);
	}

}
