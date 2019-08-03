package com.micronic.micron1;

import java.util.ArrayList;

/**
 * Created by micronic on 13/7/15.
 */
public class MMod extends MWord {

    public MMod() {
    }

    public MMod(String reln, String name, String tag) {
        this.reln = reln;
        this.name = name;
        this.tag = tag;
    }

    public String reln;

    @Override
    protected int getI(String mod) {
        if (reln.equals("ccomp") || reln.equals("advcl") || reln.equals("xcomp")) {
            return super.getI(mod);
        } else {
            switch (mod) {
                case "mark":
                    return 1;
                case "det":
                case "poss":
                case "amod":
                    return 2;
                case "nsubj":
                case "nsubjpass":
                    return 3;
                case "aux":
                case "auxpass":
                    return 4;
                case "cop":
                    return 5;
                case "neg":
                    return 6;
                case "advmod":
                    return 7;
                case "root":
                    return 8;
                case "iobj":
                case "dobj":
                case "dobjpass":
                    return 9;
                case "prep":
                    return 10;
                case "xcomp":
                case "ccomp":
                    return 11;
                case "advcl":
                    return 12;
                default:
                    return 13;
            }
        }
    }

    @Override
    protected String toString(int s) {
        String out = super.toString(s);
        return "(" + reln + ", " + out.substring(1);
    }

    @Override
    public boolean equals(MWord word) {
        if (word instanceof MMod) {
            return Utils.relates(this, word) && reln.equals(((MMod) word).reln);
        }
        return super.equals(word);
    }

    public static boolean isIgnored(String reln) {
        return !(reln.startsWith("nsubj") || reln.startsWith("dobj") || reln.startsWith("aux") || reln.equals("neg"));
    }

    public boolean matches(MMod mMod) {
        System.out.println("Matching " + this + " with " + mMod);
        if (mMod == null)
            return false;
        switch (reln) {
            case "neg":
                return true;
            case "prep":
                return getMod("pobj").matches(mMod.getMod("pobj"));
            default:
                return Utils.relates(this, mMod);
        }
    }

    @Override
    protected MMod clone() {
        MMod mod = new MMod(reln, name, tag);
        mod.mods = new ArrayList<MMod>();
        for (MMod mod1 : mods) {
            mod.mods.add(mod1.clone());
        }
        return mod;
    }
}
