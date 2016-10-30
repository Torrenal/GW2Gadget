package com.torrenal.craftingGadget.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.AbstractCollection;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.ui.components.CLabel;
import com.torrenal.craftingGadget.ui.components.CTextField;

public class TextFilter extends JPanel implements RecipeFilter
{
	private static final long serialVersionUID = 1L;
	private Vector<FilterListener> listeners;
	private CTextField textField;

	public TextFilter()
   {
	   super( new GridBagLayout() );
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
	   gc.weightx = 0;
	   gc.weighty = 0;
	   
	   gc.gridx = 1;
	   gc.gridy = 1;
	   CLabel filterIcon = new CLabel(new FilterIcon(18));
		add(filterIcon,gc);
		filterIcon.addMouseListener(new FilterIconListener());
	   
	   gc.gridx++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1;
	   textField = new CTextField();
	   add(textField, gc);
	   textField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				notifyListeners();
			}
		});
	   
		textField.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e)
			{
				notifyListeners();
			}
			public void removeUpdate(DocumentEvent e)
			{
				notifyListeners();
			}
			public void insertUpdate(DocumentEvent e)
			{
				notifyListeners();
			}
		});
   }

	protected void notifyListeners()
   {
	   if(listeners != null)
	   {
	   	for(FilterListener listener : listeners)
	   	{
	   		listener.filterUpdated();
	   	}
	   }
	   
   }

	@Override
   public void addFilterListener(FilterListener listener)
   {
		if(listeners == null)
		{
			listeners = new Vector<FilterListener>();
		}
		listeners.add(listener);
   }	

	@Override
   public AbstractCollection<Recipe> filterRecipes(AbstractCollection<Recipe> dataToFilter)
   {
		Vector<Recipe> result = new Vector<Recipe>();
		
		String searchText = textField.getText().toLowerCase().replaceAll("ö", "o").replaceAll("é", "e");
		if(searchText.isEmpty())
		{
			return dataToFilter;
		}
		if(!searchText.startsWith("^"))
		{
			searchText = "^.*" + searchText;
		}
		if(!searchText.endsWith("$"))
		{
			searchText = searchText + ".*$";
		}

		for(Recipe recipe : dataToFilter)
		{
			String recipeName = recipe.getFullMethodName().toLowerCase().replaceAll("ö", "o").replaceAll("é", "e");
			try
			{
			if(recipeName.matches(searchText))
			{
				result.add(recipe);
			}
			} catch (Throwable err)
			{
				/* Search string may be incomplete.  Silenetly ignore, and return nothing */
				return new Vector<Recipe>();
			}
		}
		return result;
   }
	
	public class FilterIcon implements Icon
   {

		int size;
		public FilterIcon(int size)
      {
	      this.size = size;
      }

		@Override
      public void paintIcon(Component c, Graphics graphics, int x, int y)
      {
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			Color color = c.getForeground();
	      if(!c.isEnabled())
	      {
	      	color = Color.gray;
	      }
	      g.setColor(color);
	      g.setStroke(new BasicStroke(1.3F));
	      
	      int centerX = x+(size/2);
	      int centerY = y+(size/2);
	      
	      int glassCenterX = centerX - (size/3);
	      int glassCenterY = centerY - (size/3);
	      int handleEndX = centerX + (size/3);
	      int handleEndY = centerY + (size/3);
	      g.drawArc(glassCenterX, glassCenterY, size*2/5, size*2/5, 0, 361);
	      g.drawLine(centerX, centerY, handleEndX, handleEndY);
	      
      }

		@Override
      public int getIconWidth()
      {
	      return size;
      }

		@Override
      public int getIconHeight()
      {
	      return size;
      }

   }
	public class FilterIconListener implements MouseListener
   {

	   @Override
	   public void mouseClicked(MouseEvent e)
	   {
	   	if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
	   	{
	   		ItemDB.reportMatchesWith(textField.getText());
	   	}
	   }

	   @Override
	   public void mouseEntered(MouseEvent e)
	   {}

	   @Override
	   public void mouseExited(MouseEvent e)
	   {}

	   @Override
	   public void mousePressed(MouseEvent e)
	   {}

	   @Override
	   public void mouseReleased(MouseEvent e)
	   {}

   }


}
