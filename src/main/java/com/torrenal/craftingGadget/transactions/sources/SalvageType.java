package com.torrenal.craftingGadget.transactions.sources;

public enum SalvageType
{
   BASIC,
   FINE,
   JOURNEYMAN,
   MASTER,
   BLACK_LION;
   
   public String toString()
   {
      switch(this)
      {
         case BASIC:
            return "Basic";
         case FINE:
            return "Fine";
         case JOURNEYMAN:
            return "Journeyman";
         case MASTER:
            return "Master";
         case BLACK_LION:
            return "Black Lion";
         default:
            return "UNK-SALVAGE";
      }
   };
}