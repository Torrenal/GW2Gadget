package com.torrenal.craftingGadget.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;

import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.CLabel;

public class AboutDialog extends CFrame implements ActionListener
{
   private static final long serialVersionUID = 1L;
	private static AboutDialog dialog = null; 

	public AboutDialog()
	{
		super("About " + ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);
		buildContent();
	}
	private void buildContent()
   {
	   Container cp = this.getContentPane();
	   cp.setLayout(new GridBagLayout());
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.gridx = 1;
	   gc.gridy=1;
	   gc.gridwidth = GridBagConstraints.REMAINDER;
	   CLabel label = new CLabel(ResourceManager.PROGRAM_NAME);
	   label.setFont(label.getFont().deriveFont(25F));
	   cp.add(label, gc);

	   gc.gridy++;
	   label = new CLabel(ResourceManager.VERSION);
	   cp.add(label, gc);

	   gc.gridy++;
	   cp.add(Box.createVerticalStrut(30), gc);

	   gc.gridy++;
	   label = new CLabel();
	   {
	   	String areaText;
	   	areaText = "<html>Welcome to " + ResourceManager.PROGRAM_NAME + "!<br/>";
	   	areaText += "It's purpose is to aid crafting in GuildWars 2 by finding items that<br/>";
	   	areaText += "are profitable, moving, or where currencies can be efficiently converted<br/>";
	   	areaText += "to your advantage.<br/>";
	   	areaText += "<br/>";
	   	areaText += "<table border=\"0\">";
	   	areaText += "<tr><td>Designer</td><td>Torrenal</td></tr>"; 
	   	areaText += "<tr><td>Developer</td><td>Torrenal</td></tr>"; 
	   	areaText += "<tr><td>Data Set</td><td>Falgar Usher of Woe</td></tr>";
	   	areaText += "<tr><td>API Support</td><td>Falgar Usher of Woe</td></tr>";
	   	areaText += "</table></html>";
	   	label.setText(areaText);
	   	label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
	   }
	   cp.add(label, gc);

	   gc.gridy++;
	   cp.add(Box.createVerticalStrut(30), gc);

	   gc.gridy++;
	   JButton closeMe = new JButton("Close");
	   closeMe.addActionListener(this);
	   cp.add(closeMe,gc);
	   
	   pack();
   }
	public synchronized static void presentDialog()
   {
	   if(dialog == null)
	   {
	   	new AboutDialog().setVisible(true);
	   } else
	   {
	   	dialog.toFront();
	   }
   }
	@Override
   public void actionPerformed(ActionEvent event)
   {
	   String command = event.getActionCommand();
	   
	   switch(command)
	   {
	   	case "Close":
	   		dialog = null;
	   		setVisible(false);
	   }
   }
}
