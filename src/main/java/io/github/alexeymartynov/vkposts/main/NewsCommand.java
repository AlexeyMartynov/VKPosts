package io.github.alexeymartynov.vkposts.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NewsCommand implements CommandExecutor  {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(sender instanceof Player)
            VKPostsPlugin.getInstance().openPost((Player) sender, false);

        return true;
    }
}
