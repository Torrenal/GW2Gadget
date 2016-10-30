package com.torrenal.craftingGadget.ui.recipeDetails;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.recipes.RecipeDB;
import com.torrenal.craftingGadget.transactions.sources.DropsSource;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.ui.ItemDetailWindow;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.CLabel;
import com.torrenal.craftingGadget.ui.components.CTextArea;

public class SourceDetails extends CFrame implements EvaluateListener
{
   private static final long serialVersionUID = 1L;

   private final static Hashtable<Source, SourceDetails> windowList = new Hashtable<>();

   private Source source;
   private CTextArea textArea;
   private CLabel m_unitValueLabel;
   private String textInstructions = "";

   private SourceDetails(Source source)
   {
      super();
      setTitle(source.getSourceType() + " Details");
      this.source = source;
      Container cp = getContentPane();
      cp.setLayout(new GridBagLayout());

      GridBagConstraints gc = new GridBagConstraints();
      gc.gridwidth = GridBagConstraints.REMAINDER;
      gc.weightx = 1;
      gc.weighty = 0;
      gc.gridx = 1;
      gc.gridy = 1;
      gc.fill = GridBagConstraints.BOTH;

      cp.add(new CLabel(source.getFullMethodName()),gc);
      gc.gridy++;

      m_unitValueLabel = new CLabel("Source Use Cost: " + source.getSourceUseCost().convertCurrency() ); 
      cp.add(m_unitValueLabel,gc);

      gc.gridy++;
      gc.weightx=1;
      gc.weighty=1;
      textInstructions = getSourceDetails();
      textArea = new CTextArea(textInstructions);
      textArea.setEditable(false);

      cp.add(new JScrollPane(textArea),gc);
      ContextUpdateNotifier.addContentUpdateListener(this);

      gc.gridy++;
      gc.gridwidth = 1;
      gc.gridwidth = 1;
      gc.weightx = 1;
      gc.weighty = 0;
      gc.fill = GridBagConstraints.NONE;

      JButton button = new JButton("Details");
      button.setToolTipText("Show this item in the Recipe Details Window.");
      button.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            onShowDetails();
         }
      });
      cp.add(button, gc);

      gc.gridx++;
      button = new JButton("Update Now");
      button.setToolTipText("Immedately update Pricing Detail from the Marketplace");
      button.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            if(SourceDetails.this.source instanceof DropsSource)
            {
               for(ItemQuantitySet input : ((DropsSource) SourceDetails.this.source).getInputs())
               {
                  Item item = input.getItem();
                  CookingCore.getPriceTool().performPriorityPricingTreeUpdate(item, true);
                  RecipeDB.updateDetailsFor(item);
                  ItemDB.updateDetailsFor(item);
               }
               for(ItemQuantitySet output : ((DropsSource) SourceDetails.this.source).getOutputs())
               {
                  Item item = output.getItem();
                  CookingCore.getPriceTool().performPriorityPricingTreeUpdate(item, true);
                  RecipeDB.updateDetailsFor(item);
                  ItemDB.updateDetailsFor(item);
               }

            } else
            {
               ItemQuantitySet[] outputs = SourceDetails.this.source.getOutputs();
               for(ItemQuantitySet output : outputs)
               {
                  CookingCore.getPriceTool().performPriorityPricingTreeUpdate(output.getItem(), true);
               }


               RecipeDB.updateDetailsFor(SourceDetails.this.source);
               ItemDB.updateDetailsFor(SourceDetails.this.source);
            }
         }
      });
      cp.add(button, gc);

      gc.gridx++;
      button = new JButton("Close");
      button.setToolTipText("Close this window");
      button.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            setVisible(false);
         }
      });
      cp.add(button, gc);
      pack();
   }

   @Override
   public void setVisible(boolean visible)
   {
      if(visible != isVisible())
      {
         if(visible)
         {
            synchronized(windowList)
            {
               windowList.put(source, this);
            }
         } else
         {
            synchronized(windowList)
            {
               windowList.remove(source);
            }
         }

         super.setVisible(visible);
      }


   }

   private String getSourceDetails()
   {
      if(source instanceof Recipe)
      { 
         Recipe recipe = (Recipe) source;
         StringBuilder sb = new StringBuilder();
         ItemQuantitySet[] outputs = recipe.getOutputs();

         if(outputs != null && outputs.length == 1)
         {

            Item product = outputs[0].getItem();
            sb.append("Ping Code: " + product.getPingCode()).append("\n");
         }

         sb.append("Profit/ea - High: " + source.getSalePriceLessCostBest().subtract(source.getSourceUseCost())).append("\n");
         sb.append("Profit/ea - Low: " + source.getSalePriceLessCostFast().subtract(source.getSourceUseCost())).append("\n");

         if(outputs != null && outputs.length > 1)
         {
            sb.append("Produces: \n");
            for(ItemQuantitySet output : outputs)
            {
               sb.append(output).append('\n');
            }
            sb.append('\n');
         }
         sb.append(source.toString());
         return sb.toString();
      } else if(source instanceof DropsSource)
      {
         StringBuilder sb = new StringBuilder();



         DropsSource dropsSource = (DropsSource)source;
         Value profitError = dropsSource.getSalePriceBestError();
         Value profitErrorLessSaleCost = dropsSource.getSalePriceLessCostBest();
         Value goodsPriceBest = dropsSource.getSalePriceBest();
         Value goodsPriceLessSaleCost = dropsSource.getSalePriceLessCostBest();
         if(profitError == null)
         {
            sb.append("Goods Value: ~" + goodsPriceBest).append("\n");
            sb.append("Market Cut : ~" + goodsPriceBest.subtract(goodsPriceLessSaleCost)).append('\n');

         } else
         {
            sb.append("Goods Value: " + goodsPriceBest).append(" \u00B1").append(profitError).append("\n"); 
            sb.append("Market Cut : " + goodsPriceBest.subtract(goodsPriceLessSaleCost)).append(" \u00B1").append(profitError.subtract(profitErrorLessSaleCost)).append("\n");
         }

         {
            sb.append("Profit/ea, Avg: " + source.getSalePriceLessCostBest().subtract(source.getSourceUseCost()));
            Value errValue = dropsSource.getSalePriceLessSaleCostBestError();
            if(errValue != null)
            {
               sb.append('\u00B1').append(errValue);
            }
            sb.append('\n');
         }

         {
            sb.append("Profit/ea, Low: " + source.getSalePriceLessCostFast().subtract(source.getSourceUseCost()));
            Value errValue = dropsSource.getSalePriceLessSaleCostFastError();
            if(errValue != null)
            {
               sb.append('\u00B1').append(errValue);
            }
            sb.append('\n');
         }
         sb.append('\n');
         sb.append(source.toString());
         return sb.toString();
      }
      {
         StringBuilder sb = new StringBuilder();
         ItemQuantitySet[] outputs = source.getOutputs();
         if(outputs.length == 1)
         {
            sb.append("Ping Code: " + outputs[0].getItem().getPingCode()).append("\n");
         }
         sb.append("\n");
         sb.append(source.toString());
         return sb.toString();
      }
   }

   protected void onShowDetails()
   {
      if(source instanceof DropsSource)
      {
         ItemDetailWindow.showDetailsFor(((DropsSource) source).getInputs()[0].getItem());
         ItemDetailWindow.bringToFront();

      } else
      {
         ItemDetailWindow.showDetailsFor(source.getOutputs()[0].getItem());
         ItemDetailWindow.bringToFront();
      }
   }

   @Override
   public void contentUpdateEvent()
   {

      final String sourceDirections = getSourceDetails();
      if(sourceDirections.equals(textInstructions))
      {
         return;
      }
      textInstructions = sourceDirections;

      Runnable doRun = new Runnable()
      {
         @Override
         public void run()
         {
            Runnable doDoRun = new Runnable()
            {
               @Override
               public void run()
               {
                  m_unitValueLabel.setText("Source Use Cost: " + source.getSourceUseCost() ); 
                  textArea.setText(sourceDirections);
                  pack();
               }
            };
            SwingUtilities.invokeLater(doDoRun);
         }
      };
      new Thread(doRun, "updateSourcDetails").start();
   }

   @Override
   public void structureUpdateEvent()
   {
   }

   static public void showDetailsFor(Source source)
   {
      SourceDetails sourceWindow;
      synchronized(windowList)
      {
         sourceWindow = windowList.get(source);
         if(sourceWindow != null && !sourceWindow.isVisible())
         {
            windowList.remove(source);
            sourceWindow = null;
         }
      }
      if(sourceWindow == null)
      {
         sourceWindow = new SourceDetails(source);
         sourceWindow.setVisible(true);
      } else
      {
         /* Bump to top */
         sourceWindow.setAlwaysOnTop(true);
         sourceWindow.setAlwaysOnTop(false);
      }

   }

   public void pack()
   {
      super.pack();
      Dimension size = getSize();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      boolean resize = false;
      if(size.height + 80 > screenSize.height)
      {
         size.height = screenSize.height - 80;
         resize = true;
      }
      if(size.width + 80 > screenSize.width)
      {
         size.width = screenSize.width - 80;
         resize = true;
      }
      if(resize)
      {
         setSize(size);
      }

   }
}
