package net.zyuiop.HangoverGames.Network;

import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 */
public class JoinMessage {
    protected UUID playerId;
    protected String reason;
    protected UUID targetArena;

    public JoinMessage(UUID playerId, String reason, UUID targetArena) {
        this.playerId = playerId;
        this.reason = reason;
        this.targetArena = targetArena;
    }

    public JoinMessage() {
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getTargetArena() {
        return targetArena;
    }

    public void setTargetArena(UUID targetArena) {
        this.targetArena = targetArena;
    }
}
