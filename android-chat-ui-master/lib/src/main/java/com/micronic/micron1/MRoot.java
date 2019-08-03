package com.micronic.micron1;

import java.util.ArrayList;

/**
 * Created by micronic on 13/7/15.
 */
public class MRoot extends MWord {

    public void setType(int type) {
        this.type = type;
    }

    public MRoot(int type) {
        this.type = type;
    }

    private int type = 0;

    public int getType() {
        return type;
    }

    @Override
    public String state() {
        String out = super.state();
        if (out.endsWith(" "))
            out = out.substring(0, out.length() - 1);
        out += type == 1 || type == 2 ? "?" : ".";
        out = out.substring(0, 1).toUpperCase() + out.substring(1);
        return out;
    }

    @Override
    protected MRoot clone() {
        MRoot root = new MRoot(type);
        root.name = name;
        root.tag = tag;
        root.mods = new ArrayList<MMod>();
        for (MMod mod : mods) {
            root.mods.add(mod.clone());
        }
        return root;
    }
}
