package com.github.desktopgame.excmobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

@Mod(modid = ModMain.MODID, name=ModMain.MODNAME, version = ModMain.VERSION)
public class ModMain {
  public static final String MODNAME = "ExcludeMobs";
    public static final String MODID = "com.github.desktopgame.excmobs";
    public static final String VERSION = "1.0";
    private List<Class<?>> excludeClasses;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
      this.excludeClasses = new ArrayList<Class<?>>();
      java.io.File file = new java.io.File("exclude_mobs.txt");
      createConfigFile(file);
      loadConfigFile(file);
      MinecraftForge.EVENT_BUS.register(this);
    }

    private void createConfigFile(java.io.File file) {
      if(file.exists()) {
        return;
      }
      try {
        file.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("# remove # from line, if do not want spawn a specific mob");
        out.newLine();
        for(Object entityClass : EntityList.classToStringMapping.keySet()) {
          Class<?> c = (Class<?>)entityClass;
          out.write("#");
          out.write(c.getName());
          out.newLine();
        }
        out.close();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }

    private void loadConfigFile(java.io.File file) {
      try {
        BufferedReader in = new BufferedReader(new FileReader(file));
        while(in.ready()) {
            String line = in.readLine();
            if(line.startsWith("#")) {
                continue;
            }
            try {
              Class<?> entityClass = Class.forName(line);
              this.excludeClasses.add(entityClass);
            } catch(ClassNotFoundException e) {
              e.printStackTrace();
            }
        }
        in.close();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }

    private boolean classIsTargetEntity(Object obj) {
        Class<?> cls = obj.getClass();
        for(int i=0; i<this.excludeClasses.size(); i++) {
            if(cls.equals(this.excludeClasses.get(i))) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
      if (classIsTargetEntity(event.entity)) {
        event.setCanceled(true);
      }
    }

    @SubscribeEvent
    public void onLivingCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
      if (classIsTargetEntity(event.entity)) {
        event.setResult(Event.Result.DENY);
      }
    }

    @SubscribeEvent
    public void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (classIsTargetEntity(event.entity)) {
        event.setCanceled(true);
      }
    }

    @SubscribeEvent
    public void onLivingDespawn(LivingSpawnEvent.AllowDespawn event) {
        if (classIsTargetEntity(event.entity)) {
        event.setResult(Event.Result.ALLOW);
      }
    }
}
