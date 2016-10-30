package com.torrenal.craftingGadget.statistics.drops;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.ObjectQuantitySet;
import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.dataModel.value.Value;


/** This represents a colleciton of DropDataRows, and 
 * accumulates the data into DataSeries for producing averages.
 *  
 * @author Eric
 */
public class DropsDataSet
{
   private Vector<DropDataRow<Item>> rows = new Vector<>();
   private ItemQuantitySet[] outputsMinimum = null;
   private Hashtable<Item, DropsDataSeries<Double>> parsedOutputs = null;
   private ItemQuantitySet[] outputsAverage = null;
   private ItemQuantitySet[] outputsMaximum = null;
   private ItemQuantitySet[] outputsErrorBars = null;

   /* Store our row drops in one vector.
    * Store the value of our row drops in another vector.
    * Because an increase in one drop implies a decrease in another drop, we can assume 
    */
   public void addRow(DropDataRow<Item> row)
   {
      rows.add(row);
   }

   private DropDataRow<Item> getValueRowFor(DropDataRow<Item> row)
   {
      int sampleSize = row.getSampleSize();
      Value value = row.getValueOfDrops();
      List<ItemQuantitySet> currencies = value.explodeCurrencies();
      
      DropDataRow<Item> ret = new DropDataRow<>(sampleSize,currencies);
      
      return ret;
   }

   public ItemQuantitySet[] getAverageDrops()
   {
      if(parsedOutputs == null)
      {
         parseOutputs();
      }
      return outputsAverage;
   }

   private synchronized void parseOutputs()
   {
      if(parsedOutputs != null)
      {
         return;
      }
      // Identify all possible drops.
      Hashtable<Item, DropsDataSeries<Double>> outputs = parseRowsIntoOutputs(rows);
      ItemQuantitySet[][] results = calculateMinMaxAverage(outputs);
      outputsAverage = results[0];
      outputsMinimum = results[1];
      outputsMaximum = results[2];
      outputsErrorBars = results[3];
      this.parsedOutputs = outputs;
      linkItemsToAPI();
   }

   private Hashtable<Item, DropsDataSeries<Double>> parseRowsIntoOutputs(Vector<DropDataRow<Item>> rows)
   {
      HashSet<Item> products = new HashSet<>();
      for(DropDataRow<Item> row : rows)
      {
         Vector<ObjectQuantitySet<Item>> drops = row.getDrops();
         for(ObjectQuantitySet<Item> drop : drops)
         {
            products.add(drop.getItem());
         }
      }
      Hashtable<Item, DropsDataSeries<Double>> outputs = new Hashtable<>();
      for(Item product : products)
      {
         Vector<DataPoint<Double>> seriesData = new Vector<>();
         for(DropDataRow<Item> row : rows)
         {
            seriesData.add(row.getDropsForProduct(product));
         }
         DropsDataSeries<Double> dropsSeries = new DropsDataSeries<Double>(seriesData);
         outputs.put(product, dropsSeries);
      }
      return outputs;
   }

   private ItemQuantitySet[][] calculateMinMaxAverage(
         Hashtable<Item, DropsDataSeries<Double>> outputs)
   {
      Vector<ItemQuantitySet> outputsMinimum    = new Vector<>();
      Vector<ItemQuantitySet> outputsAverage    = new Vector<>();
      Vector<ItemQuantitySet> outputsMaximum    = new Vector<>();
      Vector<ItemQuantitySet> outputsErrorBars  = new Vector<>();
      for(Item product : outputs.keySet())
      {
         DropsDataSeries<Double> dropsDataSeries = outputs.get(product);
         double average = dropsDataSeries.getMean();
         Double deviation = dropsDataSeries.getStandardDeviation();
         outputsAverage.add(new ItemQuantitySet(product, average));
         if(deviation != null)
         {
            double err = ResourceManager.dropRateBoundsZScore * deviation;
            double min = average - err;
            if(min < 0)
            {
               min = 0;
            }
            double max = average + err;
            
            outputsMinimum.add(new ItemQuantitySet(product, min));
            outputsMaximum.add(new ItemQuantitySet(product, max));
            outputsErrorBars.add(new ItemQuantitySet(product, err));
         }
      }
      
      Vector<ItemQuantitySet[]> ret = new Vector<>();
      ret.add(outputsAverage.toArray(new ItemQuantitySet[0]));
      if(outputsMaximum.size() == outputsAverage.size())
      {
         ret.add(outputsMinimum.toArray(new ItemQuantitySet[0]));
         ret.add(outputsMaximum.toArray(new ItemQuantitySet[0]));
         ret.add(outputsErrorBars.toArray(new ItemQuantitySet[0]));
      } else
      {
         ret.add(null);
         ret.add(null);
         ret.add(null);
      }
      return ret.toArray(new ItemQuantitySet[0][]);
   }
   
   /** Returns an array with two elements
    * The first element is the value.
    * The second is the error bar value, or null if error
    * bars cannot be determined.
    * 
    * @return
    */
   public Value[] getValueAndError()
   {
      // Don't cache values, since the values can change on the fly, and caching should properly occur 
      // in the Source. 
      Vector<DropDataRow<Item>> valueRows = new Vector<>();
      
      for(DropDataRow<Item> row : rows)
      {
        valueRows.add(getValueRowFor(row));
      }
         
      Hashtable<Item, DropsDataSeries<Double>> outputs = parseRowsIntoOutputs(valueRows);
      ItemQuantitySet[][] results = calculateMinMaxAverage(outputs);

      ItemQuantitySet[] averages = results[0];
      ItemQuantitySet[] errors = results[3];
      
      Value average = new Value(false);
      for(ItemQuantitySet averageCurrency:averages)
      {
         average = average.add(averageCurrency.getSalePriceBest());
      }
      Value error = null;
      if(errors != null)
      {
         error = new Value(false);
         for(ItemQuantitySet errorCurrency:errors)
         {
            error = error.add(errorCurrency.getSalePriceBest());
         }
      }

      Value[] ret = { average, error };
      return ret;
   }


   public ItemQuantitySet[] getMinimumDrops()
   {
      if(parsedOutputs == null)
      {
         parseOutputs();
      }
      return outputsMinimum;
   }

   public ItemQuantitySet[] getMaximumDrops()
   {
      if(parsedOutputs == null)
      {
         parseOutputs();
      }
      return outputsMaximum;
   }


   public ItemQuantitySet[] getErrorBars()
   {
      if(parsedOutputs == null)
      {
         parseOutputs();
      }
      return outputsErrorBars;
   }


   public synchronized boolean linkItemsToAPI()
   {
      Hashtable<Item, DropsDataSeries<Double>> parsedOutputs = this.parsedOutputs;
      
      Item[] items = parsedOutputs.keySet().toArray(new Item[0]);
      for(Item item : items)
      {
         if(item.getClass() != Item.class)
         {
            continue;
         }
      }
      
      return false;
   }
}
