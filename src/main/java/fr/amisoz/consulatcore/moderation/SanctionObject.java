package fr.amisoz.consulatcore.moderation;

public class SanctionObject {

    private SanctionType sanctionType;
    private String sanctionName;
    private String sanctionAt;
    private String expire;
    private String mod_name;
    private boolean isActive;
    private boolean isCancelled;

    public SanctionObject(SanctionType sanctionType, String sanctionName, String sanctionAt, String expire, String mod_name, boolean isActive, boolean isCancelled) {
        this.sanctionType = sanctionType;
        this.sanctionName = sanctionName;
        this.sanctionAt = sanctionAt;
        this.expire = expire;
        this.mod_name = mod_name;
        this.isActive = isActive;
        this.isCancelled = isCancelled;
    }

    public SanctionType getSanctionType() {
        return sanctionType;
    }

    public void setSanctionType(SanctionType sanctionType) {
        this.sanctionType = sanctionType;
    }

    public String getSanctionName() {
        return sanctionName;
    }

    public void setSanctionName(String sanctionName) {
        this.sanctionName = sanctionName;
    }

    public String getSanctionAt() {
        return sanctionAt;
    }

    public void setSanctionAt(String sanctionAt) {
        this.sanctionAt = sanctionAt;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public String getMod_name() {
        return mod_name;
    }

    public void setMod_name(String mod_name) {
        this.mod_name = mod_name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
