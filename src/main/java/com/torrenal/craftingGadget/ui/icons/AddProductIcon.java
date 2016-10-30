package com.torrenal.craftingGadget.ui.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class AddProductIcon implements Icon
{

	int height;
	int width;
	
	public AddProductIcon(int size)
   {
	   height = width = size;
   }

	@Override
	public int getIconHeight()
	{
		return height;
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public void paintIcon(Component comp, Graphics graphics, int x, int y)
	{
		Graphics2D g = (Graphics2D) graphics;
		
		g.setStroke(new BasicStroke(4));
		if(comp != null && !comp.isEnabled())
		{
			g.setColor(Color.GRAY);
		} else
		{
			g.setColor(Color.GREEN.darker());
		}
		
		g.drawLine(x+width/2, y, x+width/2, y+height);
		g.drawLine(x, y+height/2, x+width, y+height/2);
	}

}
