package com.torrenal.craftingGadget.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;

import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.CLabel;

public class LegalDialog extends CFrame implements ActionListener
{
   private static final long serialVersionUID = 1L;
	private static Object lockObject = new Object();
	private static LegalDialog dialog = null;
   
	public LegalDialog()
	{
		super("Terms of use " + ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);
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

	   gc.gridy++;
	   CLabel label = new CLabel();
	   {
	   	String areaText;
	   	areaText = "<html><br/>Apologies folks, just a brief bit caution...<br/><br/>";
	   	
	   	areaText += " ##INSERT TERMS OF USE HERE ##<br/<br/><br/>";

	   	areaText += "</html>";
	   	label.setText(areaText);
	   	label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
	   }
	   cp.add(label, gc);

	   gc.gridy++;
	   cp.add(Box.createVerticalStrut(30), gc);

	   gc.gridy++;
	   gc.gridwidth = 1;
	   gc.weightx = 0;
	   gc.anchor = GridBagConstraints.CENTER;
	   JButton declineMe = new JButton("Decline");
	   JButton acceptMe = new JButton("Accept");
	   
	   acceptMe.addActionListener(this);
	   declineMe.addActionListener(this);

	   gc.weightx = 1;
	   cp.add(Box.createHorizontalStrut(50), gc);
	   gc.gridx++;
	   gc.weightx = 0;
	   cp.add(declineMe,gc);
	   gc.gridx++;
	   gc.weightx = 1;
	   cp.add(Box.createHorizontalStrut(50), gc);
	   gc.gridx++;
	   gc.weightx = 0;
	   cp.add(acceptMe,gc);
	   gc.gridx++;
	   gc.weightx = 1;
	   cp.add(Box.createHorizontalStrut(50), gc);
	   
	   pack();
   }
	public synchronized static void presentDialog()
   {
	   if(dialog  == null)
	   {
	   	new LegalDialog().setVisible(true);
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
	   	case "Accept":
	   	   File preferencesFile = ResourceManager.getPreferencesFile();
	   	   ObjectOutputStream objectOutputStream = ResourceManager.getObjectOutputStream(preferencesFile);
	   	   try
            {
	            objectOutputStream.writeObject(Boolean.TRUE);
		   	   objectOutputStream.close();
            } catch (IOException e)
            {
	            e.printStackTrace();
            }
	   	   
	   		setVisible(false);
	   		dispose();
	   		synchronized(lockObject)
	   		{
	   			lockObject.notifyAll();
	   		}
	   		break;
	   	case "Decline":
	   		System.exit(-99);
	   }
   }
	public static void getConfirmation()
   {
	   File preferencesFile = ResourceManager.getPreferencesFile();
	   if(!preferencesFile.exists())
	   {
	   	presentDialog();
	   }
	   synchronized(lockObject)
	   {
		   while(!preferencesFile.exists())
		   {
		   	try
            {
	            lockObject.wait();
            } catch (InterruptedException e)
            { }
		   }
	   }
   }

}
