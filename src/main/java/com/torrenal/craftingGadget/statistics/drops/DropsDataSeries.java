package com.torrenal.craftingGadget.statistics.drops;

import java.util.List;
import java.util.Vector;

/**
 * This represents a series of DataPoints.
 * The datapoints themselves represent a combined sample.
 * 
 *  Example:
 *  I flip a coin 10 times and get 7 heads, that's a count of 10 and measurement of 7 for one DataPoint.
 *  I flip a coin 25 times and get 10 heads, thats a count of 25 and a measurement of 10 for a second DataPoint.
 *  I flip a coin 50 times and get 32 heads, thats a count of 50 and a measurement of 32 for a third DataPoint.
 *  
 *  This cumulates all those DataPoints into one, weighted set, with a weighted mean and weighted standard Deviation attached.
 *  The weights used here represent 'n'.
 *
 * This is not treated as a binomial problem, as we may see:
 * "I opened 50 bags, and got 5115 copper out" 
 * or "I salvaged 50 scraps and got 212 cloth"
 * 
 * The series is assumed to be static - it may not be modified once created.
 * All returned values are initialized to null, calculated as needed, and then cached.
 * 
 * Supported types are: Double
 * @author Eric
 *
 */
public class DropsDataSeries<T extends Number>
{

   Vector<DataPoint<T>> backingData;
   
   private Double mean = null;
   private Double variance = null;
   private Double standardDeviation = null;
   
   /** Create a drop data series.
    * It's a parallel list of data and weights
    * @param data
    * @param weights
    */
   public DropsDataSeries(List<DataPoint<T>> data)
   {
      if(data.isEmpty())
      {
         throw new IllegalArgumentException("Series cannot be empty");
      }
      backingData = new Vector<>(data);
      
   }
   
   public double getMean()
   {
      if(mean == null)
      {
         double sumWeightedMeasurement = 0;
         double sumWeight = 0;
         for(DataPoint<T> point : backingData)
         {
            double weight = point.getWeight();
            sumWeightedMeasurement += point.getSumMeasurement().doubleValue() * weight / point.getCount();
            sumWeight += weight;
         }
         mean = sumWeightedMeasurement / sumWeight;
      }
      return mean;
   }
   
   public Double getVariance()
   {
      if(variance == null)
      {
         double size = backingData.size();
         if(size <= 1)
         {
            // Need at least 2 sets of samples to figure out a variance.
            return null;
         }
            
        double mean = getMean();
        double sumWeights = 0;
        double squaredDeviations = 0;
        for(DataPoint<T> point : backingData)
        {
           double weight = point.getWeight();
           double deviation = (point.getSumMeasurement().doubleValue() / point.getCount()) - mean; 
           squaredDeviations += deviation*deviation * weight;
           sumWeights += weight;
        }
        
        double sampleAdjustment = 1 + 1/(size - 1) - 1/size; 
        variance = squaredDeviations / sumWeights * sampleAdjustment;
      }
      return variance;
   }
   
   public Double getStandardDeviation()
   {
      if(standardDeviation == null)
      {
         Double variance = getVariance();
         if(variance == null)
         {
            return null;
         }
         standardDeviation =  Math.sqrt(getVariance());
      }
      return standardDeviation;
   }
}
