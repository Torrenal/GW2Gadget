package com.torrenal.craftingGadget.statistics;

public class StatMath
{

   static public double zScoreToPercentile(double percentile)
   {
      return phi(percentile);
   }
   
   static public double percentileToZScore(double zScore)
   {
      return (phiInverse(zScore));
   }

   // fractional error in math formula less than 1.2 * 10 ^ -7.
   // although subject to catastrophic cancellation when z in very close to 0
   // from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
   public static double errorFunction(double z) {
      double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

      // use Horner's method
      double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
            t * ( 1.00002368 +
            t * ( 0.37409196 + 
            t * ( 0.09678418 + 
            t * (-0.18628806 + 
            t * ( 0.27886807 + 
            t * (-1.13520398 + 
            t * ( 1.48851587 + 
            t * (-0.82215223 + 
            t * ( 0.17087277
                ))))))))));
      if (z >= 0) return  ans;
      else        return -ans;
   }

   public static double errorFunction2(double z) {
      double t = 1.0 / (1.0 + 0.47047 * Math.abs(z));
      double poly = t * (0.3480242 + t * (-0.0958798 + t * (0.7478556)));
      double ans = 1.0 - poly * Math.exp(-z*z);
      if (z >= 0) return  ans;
      else        return -ans;
  }
   

   // Compute z such that Phi(z) = y via bisection search
   public static double phiInverse(double y) {
       return phiInverse(y, .00000001, -8, 8);
   } 

   // bisection search
   private static double phiInverse(double y, double delta, double lo, double hi) {
       double mid = lo + (hi - lo) / 2;
       if (hi - lo < delta) return mid;
       if (phi(mid) > y) return phiInverse(y, delta, lo, mid);
       else              return phiInverse(y, delta, mid, hi);
   }
   // cumulative normal distribution
   public static double phi(double z)
   {
      return 0.5 * (1.0 + errorFunction(z / (Math.sqrt(2.0))));
   }
   
   public static void main(String[] args) { 
      double x = Double.parseDouble(args[0]);

      System.out.println("erf(" + x + ")  = " + errorFunction(x));
      System.out.println("erf2(" + x + ") = " + errorFunction2(x));
      System.out.println("Phi(" + x + ")  = " + phi(x));
      System.out.println("Phi(" + x + ")  = " + phi(x));
      System.out.println("percentileToZScore(0.975) = " + percentileToZScore(0.975));
  }
}
