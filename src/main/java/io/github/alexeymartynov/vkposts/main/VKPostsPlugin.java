package io.github.alexeymartynov.vkposts.main;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.WallpostFull;
import io.github.alexeymartynov.vkposts.book.Book;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class VKPostsPlugin extends JavaPlugin implements Listener {

    private final TransportClient transportClient = new HttpTransportClient();
    private final VkApiClient vk = new VkApiClient(transportClient);
    private final FileConfiguration config = getConfig();

    private ServiceActor actor;
    private long lastPostDate;

    private static VKPostsPlugin instance;

    public static VKPostsPlugin getInstance() { return instance; }

    @Override
    public void onEnable()
    {
        instance = this;

        Logger logger = Bukkit.getLogger();
        logger.info("************************************");
        logger.info("**** VKPostPlugin by bybyzyanka ****");
        logger.info("************************************");

        if(!connectVK())
        {
            logger.info("Type VK Service Actor Information to config.yml");
            Bukkit.getPluginManager().disablePlugin(getInstance());
            return;
        }

        Bukkit.getPluginManager().registerEvents(getInstance(), getInstance());
        Bukkit.getPluginCommand("news").setExecutor(new NewsCommand());
        checkNewPosts();
    }

    private boolean syncLastPostDate(long date)
    {
        if(this.lastPostDate == date)
             return false;

        this.lastPostDate = date;
        config.set("last_post_date", lastPostDate);
        saveConfig();

        return true;
    }

    private boolean connectVK()
    {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        saveDefaultConfig();
        if(config.getLong("last_post_date") != 0)
            this.lastPostDate = config.getLong("last_post_date");

        int appId = config.getInt("vk_service_actor.app_id");
        String token = config.getString("vk_service_actor.access_token");
        if(token == null)
            return false;

        actor = new ServiceActor(appId, token);

        return true;
    }

    public void openPost(Player player, boolean check)
    {
        try
        {
            WallpostFull post = vk.wall().get(actor)
                    .offset(5)
                    .ownerId(-170461709)
                    .count(5)
                    .extended(true)
                    .execute().getItems().get(0);

            if(check)
            {
                if(!syncLastPostDate(post.getDate()))
                    return;
            }

            new Book(post.getText(), player);
        }
        catch(Exception exception) { exception.printStackTrace(); }
    }

    private void checkNewPosts()
    {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
        {
            for(Player player : Bukkit.getOnlinePlayers())
                openPost(player, true);

        }, 0L, 60L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Bukkit.getScheduler().runTaskAsynchronously(VKPostsPlugin.getInstance(), () ->
        {
            if(!event.getPlayer().hasPlayedBefore())
                openPost(event.getPlayer(), false);
        });
    }
}
