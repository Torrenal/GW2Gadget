package com.torrenal.craftingGadget.ui;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.torrenal.craftingGadget.dataModel.value.ValueType;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.CLabel;

public class CurrenciesDialog extends CFrame implements ActionListener
{
   private static final long serialVersionUID = 1L;
	private static CurrenciesDialog dialog = null; 

	public CurrenciesDialog()
	{
		super("Relative Currency Values");
		buildContent();
		dialog = this;
	}
	private void buildContent()
   {
	   Container cp = this.getContentPane();
	   cp.setLayout(new GridBagLayout());
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.gridx = 1;
	   gc.gridy=1;
	   gc.gridwidth = 1;
	   gc.gridheight = 2;
	   CLabel label = new CLabel("Currency");
	   label.setFont(label.getFont().deriveFont(15F));
	   cp.add(label, gc);
	   
	   gc.gridx = 2;
	   gc.gridheight = 1;
	   gc.anchor = GridBagConstraints.WEST;
	   label = new CLabel("High Value");
	   cp.add(label, gc);
	   
	   gc.gridx = 4;
		gc.anchor = GridBagConstraints.EAST;
	   label = new CLabel("Low Value");
	   cp.add(label, gc);

	   gc.gridy++;
	   gc.gridx = 2;
	   gc.anchor = GridBagConstraints.WEST;
	   label = new CLabel("(conserve)");
	   {
	   	Font font = label.getFont();
		   label.setFont(font.deriveFont((float) (font.getSize() - 2)));
	   }
	   cp.add(label, gc);
	   

	   gc.gridx = 4;
		gc.anchor = GridBagConstraints.EAST;
	   label = new CLabel("(spend)");
	   {
	   	Font font = label.getFont();
		   label.setFont(font.deriveFont((float) (font.getSize() - 2)));
	   }
	   cp.add(label, gc);
	   
	   
	   gc.gridy++;
	   gc.gridx = 1;
	   gc.gridwidth = 4;
	   gc.anchor = GridBagConstraints.CENTER;
	   JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	   cp.add(separator, gc);
	   
	   gc.gridwidth = GridBagConstraints.REMAINDER;
	   
	   for(ValueType currency : ValueType.values())
	   {
		   gc.gridy++;
		   gc.gridx = 1;
		   gc.gridwidth = 1;
		   cp.add(new CLabel(currency.toString()), gc);

		   gc.gridx = 2;
		   gc.gridwidth = 3;
	   	cp.add(new CurrencySlider(currency), gc);
	   }

	   gc.gridy++;
	   cp.add(Box.createVerticalStrut(15), gc);

	   gc.gridx = 1;
	   gc.gridy++;
	   gc.gridwidth = GridBagConstraints.REMAINDER;
	   JButton closeMe = new JButton("Close");
	   closeMe.addActionListener(this);
	   cp.add(closeMe,gc);
	   
	   pack();
   }
	public synchronized static void presentDialog()
   {
		if(dialog != null && !dialog.isVisible())
		{
			dialog = null;
		}
	   if(dialog == null)
	   {
	   	new CurrenciesDialog().setVisible(true);
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
	   		dispose();
	   }
   }
}
