package modpacktweaks.event;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import modpacktweaks.ModpackTweaks;
import modpacktweaks.client.gui.GuiHelper;
import modpacktweaks.client.gui.UpdateGui;
import modpacktweaks.config.ConfigurationHandler;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.ForgeSubscribe;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModEventHandler
{
	private String name, version, acronym;

	public boolean shouldLoadGUI;

	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onGui(GuiOpenEvent event)
	{
		if (event.gui instanceof GuiMainMenu)
		{
			if (ConfigurationHandler.shouldLoadGUI && ConfigurationHandler.showDownloadGUI)
			{
				event.gui = new UpdateGui(event.gui, true);
				GuiHelper.updateGui = (UpdateGui) event.gui;
				shouldLoadGUI = false;

				ConfigurationHandler.manuallyChangeConfigValue("B:showDownloadGUI", "true", "false");
			}
			else
			{
				name = ConfigurationHandler.packName;
				version = ConfigurationHandler.packVersion;
				acronym = ConfigurationHandler.packAcronym;

				Field f = null;
				try
				{
					f = FMLCommonHandler.class.getDeclaredField("brandings");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					ModpackTweaks.logger.log(Level.WARNING, "Reflection error, " + acronym + " watermark will not be shown");
					return;
				}

				f.setAccessible(true);
				try
				{
					if (f.get(FMLCommonHandler.instance()) == null)
					{
						FMLCommonHandler.instance().computeBranding();
						doThisAgain();
					}
					else
					{
						addStuff(f);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					ModpackTweaks.logger.log(Level.WARNING, "Reflection error, " + acronym + " watermark will not be shown");
				}
			}
		}
	}

	private void doThisAgain()
	{
		Field f = null;
		try
		{
			f = FMLCommonHandler.class.getDeclaredField("brandings");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ModpackTweaks.logger.log(Level.WARNING, "Reflection error, " + acronym + " watermark will not be shown");
		}

		addStuff(f);
	}

	@SuppressWarnings("unchecked")
	private void addStuff(Field f)
	{
		f.setAccessible(true);
		try
		{
			ImmutableList<String> list = (ImmutableList<String>) f.get(FMLCommonHandler.instance());
			List<String> newList = new ArrayList<String>();

			for (String s : list)
			{
				if (s.contains("Feed"))
				{
					// Do nothing
				}
				else if (s.equals(name + " " + version))
				{
					// Do nothing
				}
				else if (s.contains("Forge") && !s.contains(":"))
				{
					String[] sa = s.split(" ");
					String firstLine = sa[0] + " " + sa[1];
					String secondLine = sa[2];
					newList.add(firstLine + ":");
					newList.add("    " + secondLine);
				}
				else
					newList.add(s);
			}

			newList.add(name + " " + version);

			f.set(FMLCommonHandler.instance(), ImmutableList.copyOf(newList));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ModpackTweaks.logger.log(Level.WARNING, "Reflection error, " + acronym + " watermark will not be shown");
		}
	}
}