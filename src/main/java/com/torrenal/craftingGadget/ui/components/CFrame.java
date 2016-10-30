package com.torrenal.craftingGadget.ui.components;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

import com.torrenal.craftingGadget.ResourceManager;

public class CFrame extends JFrame
{

   private static final long serialVersionUID = 1L;

	public CFrame() throws HeadlessException
	{
		setIcon();
	}

	public CFrame(GraphicsConfiguration gc)
	{
		super(gc);
		setIcon();
	}

	public CFrame(String title) throws HeadlessException
	{
		super(title);
		setIcon();
	}

	public CFrame(String title, GraphicsConfiguration gc)
	{
		super(title, gc);
		setIcon();
	}

	private void setIcon()
   {
		 setIconImage(ResourceManager.getWindowIcon().getImage());
   }


}
