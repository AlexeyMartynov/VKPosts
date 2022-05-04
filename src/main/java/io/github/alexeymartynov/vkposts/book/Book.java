package io.github.alexeymartynov.vkposts.book;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Book {

    public Book(String post, Player player) {

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, getLastPostBook(post));

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte)0);
        buf.writerIndex(1);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.getInventory().setItem(slot, old);
    }

    private ItemStack getLastPostBook(String post)
    {
        Iterable<String> result = Splitter.fixedLength(255).split(post);
        String[] pages = Iterables.toArray(result, String.class);
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound bd = new NBTTagCompound();
        bd.setString("title", "Blank");
        bd.setString("author", "MoonStudio");
        NBTTagList tags = new NBTTagList();
        for(String text : pages)
           tags.add(new NBTTagString(text));

        bd.set("pages", tags);
        nms.setTag(bd);
        item = CraftItemStack.asBukkitCopy(nms);

        return item;
    }
}
