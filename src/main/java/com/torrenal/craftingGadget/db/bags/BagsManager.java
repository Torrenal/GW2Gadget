package com.torrenal.craftingGadget.db.bags;

import java.io.File;
import java.util.Scanner;
import java.util.Vector;

import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.statistics.drops.DropDataRow;
import com.torrenal.craftingGadget.statistics.drops.DropsDataSet;
import com.torrenal.craftingGadget.transactions.sources.Bag;
import com.torrenal.craftingGadget.transactions.sources.Salvage;
import com.torrenal.craftingGadget.transactions.sources.SalvageType;

public class BagsManager
{

   public static void loadBags()
   {
      { // Consumables
         Vector<File> bagsFiles = ResourceManager.getDropsFiles();
         for(File file : bagsFiles)
         {
            parseBagsFile(file);
         }
      }
      { // Salvages
         Vector<File> salvageFiles = ResourceManager.getSalvageFiles();
         for(File file : salvageFiles)
         {
            parseSalvageFile(file);
         }
      }
   }

   private static void parseBagsFile(File bagsFile)
   {
      Scanner scanner;
      try
      {
         scanner = ResourceManager.getTextInputStream(bagsFile);
      } catch (Throwable err)
      {
	 err = new Error("Error parsing bags file " + bagsFile, err);
         err.printStackTrace();
         return;
      }

      String line = null;
      int lineNum = 0;
      
      try
      {
         String resourceLine = scanner.nextLine();
         lineNum++;

         Item resource = CookingCore.findItemByName(resourceLine.split("=")[1]);
         ItemQuantitySet[] resources = {new ItemQuantitySet(resource, 1)};
         DropsDataSet dataSet = new DropsDataSet();

         while(scanner.hasNext())
         {
            line = scanner.nextLine();
            lineNum++;
            // Strip leading blanks
            while(line.startsWith(" ") || line.startsWith("\t"))
            {
               line = line.substring(1);
            }
            
            // Skip comments
            if(line.startsWith("#"))
            {
               continue;
            }
            if(line.isEmpty())
            {
               continue;
            }
            String[] bits = line.split("\\|");
            int count = Integer.parseInt(bits[0]);
            Vector<ItemQuantitySet> rowDrops = new Vector<>();
            for(int i = 1; i < bits.length; i+=2)
            {
               String productName = bits[i].trim();
               String productCountString = bits[i+1].trim();
               Item product = Value.getCurrencyByName(productName);
               if(product == null)
               {
                  product = CookingCore.findItemByName(productName);
               }
               int productCount = Integer.parseInt(productCountString);
               rowDrops.add(new ItemQuantitySet(product, productCount));
            }
            dataSet.addRow(new DropDataRow(count, rowDrops));
         }
         
         Bag bag = new Bag(resources, dataSet);
       
      } catch(Throwable err)
      {
         new Error("Error parsing " + bagsFile + ":" + lineNum, err).printStackTrace();
         return;
      }
   }
   
   private static void parseSalvageFile(File bagsFile)
   {
      Scanner scanner;
      try
      {
         scanner = ResourceManager.getTextInputStream(bagsFile);
      } catch (Throwable err)
      {
         err.printStackTrace();
         return;
      }

      String line = null;
      int lineNum = 0;
      SalvageType salvageType = null;
      
      try
      {
         String resourceLine = scanner.nextLine();
         lineNum++;
         String[] resourceLineBits = resourceLine.split("=");
         Item resource = CookingCore.findItemByName(resourceLineBits[1]);
         if(resourceLineBits[0].toLowerCase().trim().endsWith("basic"))
         {
            salvageType = SalvageType.BASIC;
         }
         if(resourceLineBits[0].toLowerCase().trim().endsWith("master"))
         {
            salvageType = SalvageType.MASTER;
         }
         ItemQuantitySet[] resources = {new ItemQuantitySet(resource, 1)};
         DropsDataSet dataSet = new DropsDataSet();

         while(scanner.hasNext())
         {
            line = scanner.nextLine();
            lineNum++;
            // Strip leading blanks
            while(line.startsWith(" ") || line.startsWith("\t"))
            {
               line = line.substring(1);
            }
            
            // Skip comments
            if(line.startsWith("#"))
            {
               continue;
            }
            if(line.isEmpty())
            {
               continue;
            }
            String[] bits = line.split("\\|");
            int count = Integer.parseInt(bits[0]);
            Vector<ItemQuantitySet> rowDrops = new Vector<>();
            for(int i = 1; i < bits.length; i+=2)
            {
               String productName = bits[i].trim();
               String productCountString = bits[i+1].trim();
               Item product = Value.getCurrencyByName(productName);
               if(product == null)
               {
                  product = CookingCore.findItemByName(productName);
               }
               int productCount = Integer.parseInt(productCountString);
               rowDrops.add(new ItemQuantitySet(product, productCount));
            }
            dataSet.addRow(new DropDataRow(count, rowDrops));
         }
         
         Salvage salvage = new Salvage(salvageType, resources, dataSet);
       
      } catch(Throwable err)
      {
         new Error("Error parsing " + bagsFile + ":" + lineNum, err).printStackTrace();
         return;
      }
      
   }


}
