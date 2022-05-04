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

public final class VKPostsPlugin extends JavaPlugin implements Listener {

    private final TransportClient transportClient = new HttpTransportClient();
    private final VkApiClient vk = new VkApiClient(transportClient);
    private final File file = new File(getDataFolder() + File.separator + "config.yml");

    private FileConfiguration config;
    private ServiceActor actor;
    private long lastPostDate;

    private static VKPostsPlugin instance;

    public static VKPostsPlugin getInstance() { return instance; }

    @Override
    public void onEnable()
    {
        instance = this;

        Bukkit.getLogger().severe("************************************");
        Bukkit.getLogger().severe("**** VKPostPlugin by bybyzyanka ****");
        Bukkit.getLogger().severe("************************************");

        if(!connectVK())
        {
            Bukkit.getLogger().severe("Type VK Service Actor Information to config.yml");
            Bukkit.getPluginManager().disablePlugin(getInstance());
            return;
        }

        Bukkit.getPluginManager().registerEvents(getInstance(), getInstance());
        checkNewPosts();
    }

    private boolean syncLastPostDate(long date)
    {
        if(this.lastPostDate == date)
             return false;

        this.lastPostDate = date;
        config.set("last_post_date", lastPostDate);

        try { config.save(file); }
        catch(Exception exception) {}

        return true;
    }

    private boolean connectVK()
    {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        if(!file.exists())
        {
            saveResource("config.yml", false);
            return false;
        }

        config = YamlConfiguration.loadConfiguration(file);
        if(config.getLong("last_post_date") != 0)
            this.lastPostDate = config.getLong("last_post_date");

        try
        {
            actor = new ServiceActor(config.getInt("vk_service_actor.app_id"),
                config.getString("vk_service_actor.access_token"));
        }
        catch(Exception exception) { return false; }

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
        if(!event.getPlayer().hasPlayedBefore())
            openPost(event.getPlayer(), false);
    }
}
