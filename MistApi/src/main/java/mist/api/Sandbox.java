package mist.api;

import java.util.Arrays;

/**
 * Created by jeppe on 1/11/17.
 */

public class Sandbox {

    private String name;
    private byte[] id;
    private boolean online;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null) {
            return false;
        }

        if (getClass() != object.getClass()) {
            return false;
        }

        Sandbox sandbox = (Sandbox)object;

        return Arrays.equals(id, sandbox.getId())
                && name.equals(sandbox.getName())
                && (online == sandbox.isOnline());
    }

}
