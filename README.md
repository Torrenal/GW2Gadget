# GW2Gadget
Torrenal's GuildWars 2 Crafting Gadget

This is provided AS-IS.
It contains bugs.
It contains things it won't calculate correctly.
It contains old data for the Mystic Forge.

This was written in part as a practice project, and often
with tight deadlines on the delivery to guild-mates.
As such the code is sloppy.
Very sloppy in some places.
-- It needs a refactor.

You assume all responsibility for using this.  If it tells 
you that salvaging ectos is a good idea and you choose to
salvage all your ectos based on that advice, that was your
choice.

Some highlights of things you'll find inside:
*  All java code.

*  Java tables for handling massive tables (11k rows) and keeping
   a responsive UI.
   Configurable columns on those tables.
   Sortable, custom cell renders, etc.
*  A crude JSON parser.  It works "well nuff" for now, but may 
   need fixing (again).
*  Serialization.
*  Threading, good and bad (seriously, your own fault if you
   toggle ResourceManager.isReleaseVersion to true, that was a
   crude reverse-engineer preventative measure and it'll most
   likely crash your IDE if you change it.
*  Threading... seriosuly, I learned much of what NOT to do
   from this program, which is another way of saying "I did it
   the wrong way, then learned why that it was the wrong way to
   do it".
*  Managing/navigating arbitrarily structured data models
   - including recursive data.
*  Statistics.  Badly done, but better than I've seen elsewhere
   in MMORPG land.  Also, those statistics are about 10% done...
   assuming someone picks this up I may poke my head in and
   describe future plans for this.

Known bugs/concerns:
* The API is semi-spammy on the server, it's not supposed to talk to the
  api.guildwars2.com servers when its idle, but something in this keeps it polling them
  It's got anit-spam code, but this should probably be fixed ahead of anything else.
* The API for getting gold/gem price conversions is currently broken and needs updating.
* The Mystic Forge recipes may need updates, 
  They are static and not obtained from any API


Author requests:
* Seriously, do not pull in incomplete data into the statics (most of what I've looked
  at there omits little things like # of bags opened, or amount of gold obtained
  Pulling in incomplete stuff, or trying to infer what's missing will skew the results.
* Seriously do not merge drops/salvage data rows.  The more detail the better (to a
  point, but there's a trick I have for coping *correctly* with too much detail,
  which not yet implemented).
