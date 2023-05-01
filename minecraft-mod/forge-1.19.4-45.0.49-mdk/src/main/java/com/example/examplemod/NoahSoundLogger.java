package com.example.examplemod;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.gui.StatsComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
// import net.minecraft.util.SoundEvents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.world.entity.player.Player;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NoahSoundLogger.MODID)
public class NoahSoundLogger {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "noah_sound_logger";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under
    // the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under
    // the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the
    // namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the
    // namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    Path logFilePath;
    boolean synced = false;

    public NoahSoundLogger() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Object[] objects = getPublicStaticObjects(SoundEvents.class);

        // for (Object obj : objects) {
        // if (obj instanceof SoundEvent) {
        // SoundEvent item = (SoundEvent) obj;
        // // item.getLocation().toString();
        // // INTERESTING_SOUNDS.add(item.getLocation().toString());
        // // System.out.println(item.getLocation().toString());
        // }
        // }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

    private void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");

        // for (Player player : event.getLevel().players()) {
        // player.sendSystemMessage();
        // }

        if (event.getServer().isSingleplayer()) {
            try {
                logFilePath = createLogFile();
                String headers = "timestamp,name,volume,pitch,delay,attenuation,soundX,soundY,soundZ,looping\n";
                appendData(logFilePath, headers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods
    // in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    private static final String[] ADJECTIVES = {
            "happy", "jolly", "dreamy", "sad", "angry", "adventurous", "brave"
    };

    private static final String[] NOUNS = {
            "apple", "banana", "orange", "lemon", "peach", "cherry", "strawberry"
    };

    private static final Random RANDOM = new Random();

    // private static void displayMessage(String message) {
    // Minecraft minecraft = Minecraft.getInstance();
    // if (minecraft.player != null) {
    // Player player = minecraft.player;
    // // player.sendMessage(new StatsComponent(message), player.getUUID());
    // player.sendMessage("This is sendmessage example string.");
    // }
    // }

    public static String generateFriendlyName() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        return adjective + "-" + noun;
    }

    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    // private static final DateTimeFormatter CSV_DATE_FORMATTER =
    // DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Path createLogFile() throws IOException {
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        String fileName = "sounds_for_Noah_" + LocalDateTime.now().format(FILE_NAME_FORMATTER) + ".csv";
        Path logFilePath = Paths.get(desktopPath, fileName);
        Files.createFile(logFilePath);
        return logFilePath;
    }

    private static void appendData(Path logFilePath, String contents) throws IOException {
        executor.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath.toFile(), true))) {
                writer.write(contents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // int getDelay();

    // float getVolume();

    // float getPitch();

    // double getX();

    // double getY();

    // double getZ();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSoundEvent(PlaySoundSourceEvent event) {
        if (event.getSound() == null) {
            return;
        }

        if (!synced) {
            try {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                String timestamp = now.format(formatter);
                String csvLine = String.format("%s,%s,%f,%f,%d,%s,%f,%f,%f,%b\n", timestamp, "SYNC", -12345.0f, -12345.0f,
                        -12345,
                        "N/A", -12345.0f, -12345.0f, -12345.0f, false);
                Minecraft.getInstance().player
                        .sendSystemMessage(Component.literal("VIDEO-AUDIO SYNCHRONIZATION MESSAGE"));
                try {
                    appendData(logFilePath, csvLine);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synced = true;

            } finally {}
        }

        try {
            System.out.println(event.getName());
            String name = event.getName();
            SoundInstance sound = event.getSound();
            int delay = sound != null ? sound.getDelay() : -12345;
            float volume = sound != null ? sound.getVolume() : -12345f;
            // float volume = 0.0f;
            String attenuation = sound.getAttenuation().toString();
            double soundX = sound.getX();
            double soundY = sound.getY();
            double soundZ = sound.getZ();
            float pitch = sound.getPitch();
            boolean isLooping = sound.isLooping();
            // float pitch = 0.0f;

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = now.format(formatter);

            // timestamp,name,volume,pitch,delay,attenuation,soundX,soundY,soundZ%n
            String csvLine = String.format("%s,%s,%f,%f,%d,%s,%f,%f,%f,%b\n", timestamp, name, volume, pitch, delay,
                    attenuation, soundX, soundY, soundZ, isLooping);
            appendData(logFilePath, csvLine);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException np) {
            np.printStackTrace();
        } finally {

        }
    }

    // Create a Set containing the sounds you're interested in
    // private static final Set<String> INTERESTING_SOUNDS = new HashSet<String>() {
    // {
    // Object[] objects = getPublicStaticObjects( SoundEvents.class);

    // for (Object obj : objects) {
    // if (obj instanceof SoundEvent) {
    // SoundEvent item = (SoundEvent) obj;
    // // item.getLocation().toString();
    // INTERESTING_SOUNDS.add(item.getLocation().toString());
    // System.out.println(item.getLocation().toString());
    // }
    // }
    // }
    // };

    public static Object[] getPublicStaticObjects(Class<?> clazz) {
        List<Object> objects = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();

            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                try {
                    objects.add(field.get(null));
                } catch (IllegalAccessException e) {
                    // Handle exception
                    e.printStackTrace();
                }
            }
        }

        return objects.toArray();
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
