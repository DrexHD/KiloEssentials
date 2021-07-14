package org.kilocraft.essentials.user;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.PunishmentManager;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.MutedPlayerList;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class ServerPunishmentManager implements PunishmentManager {

    public ServerPunishmentManager() {
    }

    @Override
    public boolean isMuted(EntityIdentifiable user) {
        MutedPlayerList mutedPlayerList = KiloServer.getServer().getUserManager().getMutedPlayerList();
        MinecraftServer server = KiloServer.getServer().getMinecraftServer();
        GameProfile profile = server.getUserCache().method_14512(user.getId());
        if (profile == null) {
            return false;
        }

        if (mutedPlayerList.contains(profile) && mutedPlayerList.get(profile) != null) {
            Date expiry = Objects.requireNonNull(mutedPlayerList.get(profile)).getExpiryDate();
            if (expiry != null) {
                return new Date().before(expiry);
            }
        }
        return false;
    }
}
