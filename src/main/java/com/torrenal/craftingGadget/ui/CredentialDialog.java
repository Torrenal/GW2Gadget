package com.torrenal.craftingGadget.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.torrenal.craftingGadget.priceFetcher.PriceTool;
import com.torrenal.craftingGadget.ui.components.CFrame;

public class CredentialDialog extends CFrame implements ActionListener, DocumentListener, WindowListener 
{
	

   private static final long serialVersionUID = 1L;
	private JTextField userField;
	private JPasswordField passwordField;
	private JButton acceptButton;
	private JButton cancelButton;
	private Object syncObject = new Object();
	private boolean abort = false;
	private boolean accept = false;
	
   
   public CredentialDialog()
   {
   	super("Enter Credentials");
   	
   	buildContent();
   	pack();
   	getRootPane().setDefaultButton(acceptButton);
   	addWindowListener(this);
   }
   
   public String[] getCredentials()
   {
   	setVisible(true);
   	
		synchronized (syncObject)
      {
   		while(userField.getText().isEmpty() || passwordField.getPassword().length == 0)
   		{
   			try
            {
	            syncObject.wait();
            } catch (InterruptedException e)
            { }
   			if(abort)
   			{
   	   		userField.setText("");
   				return null;
   			}
   		}
      }
		char[] password = passwordField.getPassword();
   	if(accept)
   	{
   		String[] ret = new String[2];
   		ret[0] = userField.getText();
   		ret[1] = new String(password);
   		userField.setText("");
   		for(int i = 0; i < password.length;i++)
   		{
   			password[i] = 0;
   		}
   		return ret;
   	}
		for(int i = 0; i < password.length;i++)
		{
			password[i] = 0;
		}
		userField.setText("");
   	return null;
   }
   	

	private void buildContent()
   {
	   Container cp = getContentPane();
	   
	   cp.setLayout(new GridBagLayout());
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   gc.gridx = 1;
	   gc.gridy = 1;
	   gc.fill=GridBagConstraints.BOTH;
	
	   
	   {
	   	cp.add(new JLabel("GW2 Username:"), gc);
	   	gc.gridwidth = 1;
	   }
	   
	   gc.gridx++;
	   {
	   	userField = new JTextField(35);
	   	userField.getDocument().addDocumentListener(this);
	   	cp.add(userField, gc);
	   }

	   gc.gridx = 1;
	   gc.gridy++;
	   {
	   	cp.add(new JLabel("GW2 Password:"), gc);
	   	gc.gridwidth = 1;
	   }
	   
	   gc.gridx++;
	   {
	   	passwordField = new JPasswordField(20);
	   	passwordField.getDocument().addDocumentListener(this);
	   	cp.add(passwordField, gc);
	   }

	   gc.gridx=1;
	   gc.gridy++;
	   {
	   	gc.gridwidth = GridBagConstraints.REMAINDER;
	   	cp.add(getButtonPanel(), gc);
	   }
	   
	   gc.gridy++;
	   {
		   gc.fill=GridBagConstraints.NONE;

	   	cp.add(getBlurbPanel(),gc);
	   }
   }

	private Component getButtonPanel()
   {
	   JPanel buttonPanel = new JPanel( new GridBagLayout());
	   GridBagConstraints gc = new GridBagConstraints();
	   gc.gridx = 1;
	   gc.gridy = 1;
	   {
	   	acceptButton = new JButton("OK");
	   	buttonPanel.add(acceptButton,gc);
	   	acceptButton.setEnabled(false);
	   	acceptButton.setDefaultCapable(true);
	   	acceptButton.addActionListener(this);
	   }
	   
	   gc.gridx++;
	   {
	   	cancelButton = new JButton("Cancel");
	   	buttonPanel.add(cancelButton,gc);
	   	cancelButton.setDefaultCapable(false);
	   	cancelButton.addActionListener(this);
	   }
	   return buttonPanel;
   }

	
	private Component getBlurbPanel()
   {
		JLabel blurbPanel = new JLabel();
		blurbPanel.setText("<html>This tool requires access to trading post data<br/>"+
		"and it uses a GuildWars 2 account for that access.<br/>"+
		"<br/>"+
		"YOUR Guildwars 2 account.<br/>"+
		"<br/>"+
		"The author of this tool does <I>NOT</I> endorse giving<br/>" +
		"account information to 3rd party programs.<br/>" +
		"<br/>" +
		"This is a 3rd party program.<br/>" +
		"<br/>" +
		"This tool also requires you to do exactly that.<br/>" +
		"Provide them here at your own discretion.</html>");
		return blurbPanel;
   }


	@Override
   public void actionPerformed(ActionEvent e)
   {
		String command = e.getActionCommand();
		if("Cancel".equals(command))
		{
			abort = true;
			PriceTool.setInterfaceEnabled(false);
			synchronized(syncObject)
			{
				syncObject.notifyAll();
			}
			setVisible(false);
			dispose();
		}
		if("OK".equals(command))
		{
			accept = true;
			synchronized(syncObject)
			{
				syncObject.notifyAll();
			}
			setVisible(false);
			dispose();
		}
   }

	@Override
   public void changedUpdate(DocumentEvent e)
   {
		if(userField.getText().isEmpty() || passwordField.getPassword().length == 0)
		{
			acceptButton.setEnabled(false);
		}
		else 
		{
			acceptButton.setEnabled(true);
		}
   }

	@Override
   public void insertUpdate(DocumentEvent e)
   {
		if(userField.getText().isEmpty() || passwordField.getPassword().length == 0)
		{
			acceptButton.setEnabled(false);
		}
		else 
		{
			acceptButton.setEnabled(true);
		}
   }

	@Override
   public void removeUpdate(DocumentEvent e)
   {
		if(userField.getText().isEmpty() || passwordField.getPassword().length == 0)
		{
			acceptButton.setEnabled(false);
		}
		else 
		{
			acceptButton.setEnabled(true);
		}
   }

	@Override
   public void windowActivated(WindowEvent e)
   {}

	@Override
   public void windowClosed(WindowEvent e)
   {
		if(!accept)
		{
			abort = true;
		}
		synchronized(syncObject)
		{
			syncObject.notifyAll();
		}
   }

	@Override
   public void windowClosing(WindowEvent e)
   {}

	@Override
   public void windowDeactivated(WindowEvent e)
   {}

	@Override
   public void windowDeiconified(WindowEvent e)
   {}

	@Override
   public void windowIconified(WindowEvent e)
   { }

	@Override
   public void windowOpened(WindowEvent e)
   { }
}
