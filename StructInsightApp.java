package structinsight2020;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class StructInsightApp 
{
	public static void main(String[] args)
	{
		JFrame start_frame = new JFrame("Structural Insight");

		URL logo_url = StructInsightApp.class.getResource("/MyIcons/contiBeamLogo.jpg");
		Image imageLogo = Toolkit.getDefaultToolkit().getImage(logo_url);
		ImageIcon cbLogo = new ImageIcon(imageLogo); 
		String logoStr = "<HTML><FONT COLOR='BLUE'><B>" + 
						"Click to continue.</B></FONT></HTML>";
		JButton jb1 = new JButton(logoStr, cbLogo);
		jb1.setVerticalTextPosition(AbstractButton.BOTTOM);
		jb1.setHorizontalTextPosition(AbstractButton.CENTER);
		jb1.setContentAreaFilled(false);
		jb1.setToolTipText("Click to start!");
		ActionListener listen1 = new ActionListener()
		{   // Anonymous inner class
			public void actionPerformed(ActionEvent ae)
			{
				structinsight2020.contibeam.ContiBeamMain.displayMenu(args);
				start_frame.setVisible(false);
				start_frame.dispose();
			}
		}; // Anonymous inner class ends.
		jb1.addActionListener(listen1);
		start_frame.getContentPane().add(jb1);

		// TO DO:
		// Create more buttons on similar line to invoke analysis modules for
		// Plane truss, Space truss, Plane frame, Space frame, etc.

		start_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		start_frame.setBounds(200, 200, 410, 250);
		start_frame.setVisible(true);
	}
}


