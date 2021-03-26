package org.kilocraft.essentials.util.settings;

import net.minecraft.SharedConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.NBTStorage;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.util.nbt.NBTStorageUtil;
import org.kilocraft.essentials.util.registry.RegistryKeyID;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.values.*;
import org.kilocraft.essentials.util.settings.values.util.RootSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerSettings implements NBTStorage {

    public static RootSetting root = new RootSetting();
    //TODO: Figure out a clever way to cache values
    //if we ever get more than 256 entities this will throw IndexOutOfBoundsException
    private static final int entities = 128;
    public static final boolean[] entityTickCache = new boolean[entities];
    public static final boolean[] entitySpawnCache = new boolean[entities];
    public static boolean perPlayerMobcap = false;
    public static int tickDistance = -1;
    public static int wither_check_distance = 2;
    public static double wither_tp_distance = 1;
    public static float[][] mobcap;
    public static boolean tickInactiveVillagers = false;
    public static int[] activationRange = new int[4];


    public ServerSettings() {
        NBTStorageUtil.addCallback(this);
    }

    public static boolean getBoolean(String id) {
        return ((BooleanSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static double getDouble(String id) {
        return ((DoubleSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static float getFloat(String id) {
        return ((FloatSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static int getInt(String id) {
        return ((IntegerSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static String getString(String id) {
        return ((StringSetting) Objects.requireNonNull(root.getSetting(id))).getValue();
    }

    public static void setBoolean(String id, boolean value) {
        ((BooleanSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setDouble(String id, double value) {
        ((DoubleSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setFloat(String id, float value) {
        ((FloatSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setInt(String id, int value) {
        ((IntegerSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public static void setString(String id, String value) {
        ((StringSetting) Objects.requireNonNull(root.getSetting(id))).setValue(value);
    }

    public void registerSettings() {
        //Custom settings
        IntegerSetting viewDistance = (IntegerSetting) new IntegerSetting(10, "view_distance").onChanged(distance -> KiloEssentials.getServer().getMinecraftServer().getPlayerManager().setViewDistance(distance));
        BooleanSetting debug = (BooleanSetting) new BooleanSetting(false, "debug").onChanged(b -> SharedConstants.isDevelopment = b);

        //Patches
        CategorySetting patch = new CategorySetting("patch");
        //Donkey dupe
        BooleanSetting donkeyDupe = new BooleanSetting(true, "donkey_dupe");
        //Stuck Wither
        CategorySetting wither = new CategorySetting("wither");
        IntegerSetting check_distance = (IntegerSetting) new IntegerSetting(2, "check_distance").range(-256, 256).onChanged(integer -> wither_check_distance = integer);
        DoubleSetting tp_distance = (DoubleSetting) new DoubleSetting(1D, "tp_distance").range(-256D, 256D).onChanged(d -> wither_tp_distance = d);
        wither.addChild(check_distance);
        wither.addChild(tp_distance);
        //per-player-mobcap
        BooleanSetting ppmobcap = (BooleanSetting) new BooleanSetting(false, "ppmobcap").onChanged(bool -> perPlayerMobcap = bool);

        //Enchanting
        CategorySetting enchanting = new CategorySetting("enchanting");
        BooleanSetting hasChanged = new BooleanSetting(false, "hasChanged");
        enchanting.addChild(hasChanged);


        //Entity merging
        CategorySetting item_merge = new CategorySetting("item_merge");
        DoubleSetting radius = new DoubleSetting(0.5D, "radius").range(0D, 256D);
        BooleanSetting adjust_movement = new BooleanSetting(true, "adjust_movement");
        item_merge.addChild(radius);
        item_merge.addChild(adjust_movement);
        //Shulker spawn chance
        DoubleSetting shulker_spawn_chance = new DoubleSetting(0D, "shulker_spawn_chance");

        //Global sound
        BooleanSetting global_sound = new BooleanSetting(true, "global_sound");

        //Activation range
        CategorySetting activation_range = new CategorySetting("activation_range");
        IntegerSetting misc = (IntegerSetting) new IntegerSetting(16, "misc").onChanged(integer -> activationRange[0] = integer);
        IntegerSetting raider = (IntegerSetting) new IntegerSetting(48, "raider").onChanged(integer -> activationRange[1] = integer);
        IntegerSetting animal = (IntegerSetting) new IntegerSetting(32, "animal").onChanged(integer -> activationRange[2] = integer);
        IntegerSetting monster = (IntegerSetting) new IntegerSetting(32, "monster").onChanged(integer -> activationRange[3] = integer);
        BooleanSetting inactiveVillagers = (BooleanSetting) new BooleanSetting(true, "inactiveVillagers").onChanged(bool -> tickInactiveVillagers = bool);
        activation_range.addChild(misc).addChild(raider).addChild(animal).addChild(monster).addChild(inactiveVillagers);


        patch.addChild(donkeyDupe);
        patch.addChild(wither);
        patch.addChild(ppmobcap);
        patch.addChild(enchanting);
        patch.addChild(item_merge);
        patch.addChild(shulker_spawn_chance);
        patch.addChild(global_sound);
        patch.addChild(activation_range);

        //Entity Limit
        CategorySetting entity_limit = new CategorySetting("entity_limit");
        List<String> limit_entries = new ArrayList<>();
        for (EntityType entityType : new EntityType[]{EntityType.GUARDIAN, EntityType.ITEM_FRAME, EntityType.CHICKEN, EntityType.VILLAGER}) {
            limit_entries.add(Registry.ENTITY_TYPE.getId(entityType).getPath());
        }
        limit_entries.add("animals");
        for (String limit_entry : limit_entries) {
            CategorySetting entity = new CategorySetting(limit_entry);
            IntegerSetting limit = new IntegerSetting(-1, "limit");
            IntegerSetting range = new IntegerSetting(-1, "range");
            entity.addChild(limit);
            entity.addChild(range);
            entity_limit.addChild(entity);
        }

        //Ticking
        CategorySetting tick = new CategorySetting("tick");
        IntegerSetting distance = (IntegerSetting) new IntegerSetting(-1, "distance").onChanged(integer -> tickDistance = integer);
        BooleanSetting entity = (BooleanSetting) new BooleanSetting(true, "entity").onChanged(bool -> entityTickCache[0] = bool);
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            BooleanSetting value = (BooleanSetting) new BooleanSetting(true, Registry.ENTITY_TYPE.getId(entityType).getPath()).onChanged(bool -> entityTickCache[Registry.ENTITY_TYPE.getRawId(entityType) + 1] = bool);
            entity.addChild(value);
        }
        tick.addChild(distance);
        tick.addChild(entity);

        //Spawning
        CategorySetting spawn = new CategorySetting("spawn");
        BooleanSetting spawnEntity = (BooleanSetting) new BooleanSetting(true, "entity").onChanged(bool -> entitySpawnCache[0] = bool);
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            BooleanSetting value = (BooleanSetting) new BooleanSetting(true, Registry.ENTITY_TYPE.getId(entityType).getPath()).onChanged(bool -> entitySpawnCache[Registry.ENTITY_TYPE.getRawId(entityType) + 1] = bool);
            spawnEntity.addChild(value);
        }

        spawn.addChild(spawnEntity);

        //Mobcap
        ServerSettings.mobcap = new float[RegistryUtils.getWorldsKeySet().size()][SpawnGroup.values().length];
        CategorySetting mobcap = new CategorySetting("mobcap");
        int worldID = 0;
        for (RegistryKey<World> registryKey : RegistryUtils.getWorldsKeySet()) {
            ((RegistryKeyID)registryKey).setID(worldID);
            FloatSetting world = (FloatSetting) new FloatSetting(1F, registryKey.getValue().getPath()).range(0F, 100F).onChanged(f -> ServerSettings.mobcap[((RegistryKeyID)registryKey).getID()][0] = f);
            for (SpawnGroup spawnGroup : SpawnGroup.values()) {
                FloatSetting group = (FloatSetting) new FloatSetting(1F, spawnGroup.getName().toLowerCase()).range(0F, 100F).onChanged(f -> ServerSettings.mobcap[((RegistryKeyID)registryKey).getID()][spawnGroup.ordinal()] = f);
                world.addChild(group);
            }
            worldID++;
            mobcap.addChild(world);
        }
        root.addChild(viewDistance);
        root.addChild(debug);
        root.addChild(entity_limit);
        root.addChild(tick);
        root.addChild(spawn);
        root.addChild(mobcap);
        root.addChild(patch);
        KiloCommands.getInstance().register(new SettingCommand());
    }

    @Override
    public KiloFile getSaveFile() {
        return new KiloFile("settings.dat", KiloEssentials.getDataDirPath());
    }

    @Override
    public CompoundTag serialize() {
        return root.toTag();
    }

    @Override
    public void deserialize(@NotNull CompoundTag tag) {
        root.fromTag(tag);
    }

}
