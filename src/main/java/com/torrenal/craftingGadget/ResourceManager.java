package com.torrenal.craftingGadget;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.torrenal.craftingGadget.statistics.StatMath;


public class ResourceManager
{

	public final static Charset ENCODING = StandardCharsets.UTF_8;
	public final static String VERSION = "2.15";
	public final static String PROGRAM_NAME = "Torrenal's GW2 Crafting Gadget";
	public static final boolean isReleaseVersion = false; // Enabling this tweaks threading to grief debuggers.
	public static final double dropRateBoundsPercent = 0.975; /* Two Tail, so 95% = half the error */
	public static final double dropRateBoundsZScore = StatMath.percentileToZScore(dropRateBoundsPercent);

	public static ObjectInputStream getObjectInputStream(File file)
	{
		try
		{
			FileInputStream fileStream = new FileInputStream(file);
			BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);
			ObjectInputStream ois;
			ois = new ObjectInputStream(bufferedStream);
			return ois;
		} catch (IOException e)
		{
			/* Could not read for any of a million reasons, assume corrupt and nuke it */
			e.printStackTrace();
			file.delete();
			return null;
		}
	}

	public static Scanner getTextInputStream(File file)
	{
		Scanner scanner = null;
		try
		{
			if(!file.exists())
			{
				String resourceName = file.toString();
				while(resourceName.startsWith("\\") || resourceName.startsWith("/") || resourceName.startsWith("."))
				{
					resourceName = resourceName.substring(1);
				}
				resourceName = resourceName.replaceAll("\\\\","/");
				InputStream resourceStream = ResourceManager.class.getClassLoader().getResourceAsStream(resourceName);
				return new Scanner(resourceStream);
			}
			Path path = file.toPath();
			scanner = new Scanner(path, ENCODING.name());
		} catch (IOException e)
		{
			throw new Error("Unable to open resource file: "+ file, e);
		}
		return scanner;
	}


	public static File getFixedDataFile()
	{
		return new File(".\\FixedData.txt");
	}

	public static File getItemDBDatFile()
	{
		return new File(".\\ItemDB.dat");
	}

	public static File getGuildUpgradeDBDatFile()
	{
		return new File(".\\GuildUpgradeDB.dat");
	}


	public static File getRecipeDBDatScratchFile()
	{
		return new File(".\\RecipeDB.tmp");
	}

	public static File getRecipeDBDatFile()
	{
		return new File(".\\RecipeDB.dat");
	}


	public static File getItemDBDatScratchFile()
	{
		return new File(".\\ItemDB.tmp");
	}

	public static ObjectOutputStream getObjectOutputStream(File file)
	{
		try
		{
			file.delete();
			FileOutputStream fileStream = new FileOutputStream(file);
			BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream);
			ObjectOutputStream oos;
			oos = new ObjectOutputStream(bufferedStream);
			return oos;
		} catch (IOException e)
		{
			/* Could not read for any of a million reasons, assume corrupt and nuke it */
			new Error("Unable to open save file");
			e.printStackTrace();
			System.exit(-1);
			return null; /* Dead Code */
		}
	}

	public static ImageIcon getWindowIcon()
	{
		return new ImageIcon(ClassLoader.getSystemResource("ApplicationIcon.png"));
	}

	public static File getQuickLoadFile()
	{
		return new File("quickLoadList.txt");
	}

	public static File getPreferencesFile()
	{
		return new File(".\\Preferences.dat");
	}

	public static Vector<File> getDropsFiles()
	{
		Vector<File> files = new Vector<>();
		files.add(new File("bags/BagofAlchemicalMaterials.txt"));
		files.add(new File("bags\\BagOfStolenGoods.txt"));
		files.add(new File("bags\\HeavyBagOfBooty.txt"));
		files.add(new File("bags\\HeavyBagOfTrinkets.txt"));
		files.add(new File("bags\\HeavyLootBag.txt"));
		files.add(new File("bags\\HeavyMoldyBag.txt"));
		files.add(new File("bags\\HeavyRitualBag.txt"));
		files.add(new File("bags\\HeavyThornedBagMasterwork.txt"));
		files.add(new File("bags\\LargeBagOfTrinkets.txt"));
		files.add(new File("bags\\LargeMinersBag.txt"));
		files.add(new File("bags\\LargeMoldyBag.txt"));
		return files;
	}
	public static Vector<File> getSalvageFiles()
	{
		Vector<File> files = new Vector<>();
		files.add(new File("salvage\\BitOfMetalScrap-Basic.txt"));
		files.add(new File("salvage\\CoarceLeatherStrap-Basic.txt"));
		files.add(new File("salvage\\DiscardedGarment-Basic.txt"));
		files.add(new File("salvage\\DiscardedGarment-Master.txt"));
		files.add(new File("salvage\\Ectos-Basic.txt"));
		files.add(new File("salvage\\Ectos-Master.txt"));
		files.add(new File("salvage\\FilthyPelt-Basic.txt"));
		files.add(new File("salvage\\FrayedPelt-Basic.txt"));
		files.add(new File("salvage\\HardLeatherStrap-Basic.txt"));
		files.add(new File("salvage\\MetalScrap-Basic.txt"));
		files.add(new File("salvage\\PileOfMetalScrap-Basic.txt"));
		files.add(new File("salvage\\Rag-Basic.txt"));
		files.add(new File("salvage\\RippedPelt-Basic.txt"));
		files.add(new File("salvage\\RuggedLeatherStrap-Basic.txt"));
		files.add(new File("salvage\\SalvageableFusedMetalScrap-Master.txt"));
		files.add(new File("salvage\\SalvageableHide-Basic.txt"));
		files.add(new File("salvage\\SalvageableMetalScrap-Basic.txt"));
		files.add(new File("salvage\\SalvageablePelt-Basic.txt"));
		files.add(new File("salvage\\ShreddedGarment-Basic.txt"));
		files.add(new File("salvage\\ShreddedRag-Basic.txt"));
		files.add(new File("salvage\\TornGarment-Basic.txt"));
		files.add(new File("salvage\\TornPelt-Basic.txt"));
		files.add(new File("salvage\\ValuableMetalScrap-Basic.txt"));
		files.add(new File("salvage\\ValuableMetalScrap-Master.txt"));
		files.add(new File("salvage\\WornGarment-Basic.txt"));
		files.add(new File("salvage\\WornRag-Basic.txt"));
		return files;
	}
}
