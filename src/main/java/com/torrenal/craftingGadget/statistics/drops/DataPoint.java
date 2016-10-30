package com.torrenal.craftingGadget.statistics.drops;


public class DataPoint<T extends Number>
{

   final T sumMeasurement;
   final int count;
   final double weight;
   
   /** Creates a data point consisting of a sample size of 'count'
    * and a measured value of 'sumMeasurement'.
    * 
    * That is: y = sumMeasurement
    *          n = count
    *
    * This makes no effort to track the deviation from the source sample - it's assumed the
    * source data lacks that information.
    * 
    * @param sumMeasurement
    * @param count
    */
   public DataPoint(T sumMeasurement, int count)
   {
      if(count <= 0)
      {
         throw new IllegalArgumentException("Data points must have a positive count of measurements");
      }
      this.sumMeasurement = sumMeasurement;
      this.count = count;
      this.weight = Math.sqrt(count); 
   }
   
   public T getSumMeasurement()
   {
      return sumMeasurement;
   }
   public int getCount()
   {
      return count;
   }

   /** Cached 'weight' based on the count.
    * Specifically, sqrt(count), used to give
    * different sized data points different weights.
    * @return
    */
   public double getWeight()
   {
      return weight;
   }
}
