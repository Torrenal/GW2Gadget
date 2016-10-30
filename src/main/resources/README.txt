Breakdown of the resource files:
FixedData.txt - contains Mystic Forge, Food bags (Lemons in Bulk -> Has Lemons), and
                basic vendor data (Lemons in Bulk can be bought from...)...

quickloadList.txt - Used to prioritize some items in the initialize of the item data 
                    file.

bags/ folder: Used to contain drop rate data for bags.  Additions to this folder 
              must added to ResourceManager.java
             
salvage/ folder: Used to contain drop rate data for Salvages.  Additions to this 
                 folder must be added to ResourceManager.java
                 
A brief note on drop rates.  Yes, I'm doing it wrong, but I think I'm doing it 
better than anything I've seen anyone else doing.  It's also unfinished.  I need
to get a proper measure of entropy into the calculations, and refigure them to 
estimate what the population is, not what some sample group X will get.
Please don't summarize existing or new drop rate data.  Keep it as it appears in
my initial files.  I have sound reasons for this - for which my work has not been
completed (see prior comment: 'It's also unfinished' - meaning incomplete).  
You're looking at about 10% of what I've planned for drop rates logic to tackle
 - there's a wide realm for the other 90% to tackle.
