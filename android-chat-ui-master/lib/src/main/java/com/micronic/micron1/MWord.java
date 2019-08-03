package com.micronic.micron1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import simplenlg.features.Person;

/**
 * Created by micronic on 13/7/15.
 */
public abstract class MWord {

    private final static List<String> cRelns = new ArrayList<String>();

    static {
        cRelns.add("advmod");
        cRelns.add("advcl");
        cRelns.add("xcomp");
        cRelns.add("ccomp");
        cRelns.add("prep");
    }

    public String name;
    public String tag;
    public List<MMod> mods = new ArrayList<MMod>();

    public MMod getMod(String reln) {
        for (MMod mod : mods) {
            if (mod.reln.equals(reln))
                return mod;
        }
        return null;
    }

    public MMod tryMod(String reln) {
        List<String> relns = getListOfRelns(reln);
        for (String reln1 : relns) {
            MMod mod = getMod(reln1);
            if (mod != null) {
                return mod;
            }
        }
        return null;
    }

    private List<String> getListOfRelns(String reln) {
        List<String> relns = new ArrayList<String>();
        relns.add(reln);
        List<String> sRelns = new ArrayList<String>(cRelns);
        if (sRelns.remove(reln)) {
            relns.addAll(sRelns);
        }
        return relns;
    }

    protected int getI(String mod) {
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
            case "root":
                return 7;
            case "iobj":
            case "dobj":
            case "dobjpass":
                return 8;
            case "prep":
                return 9;
            case "xcomp":
            case "ccomp":
                return 10;
            case "advmod":
                return 11;
            case "advcl":
                return 12;
            default:
                return 13;
        }
    }

    public String state() {
        String out = "";
        List<MMod> mods = new ArrayList<MMod>(this.mods);
        MMod root = new MMod();
        boolean cop = isCopule(name);
        root.reln = cop ? "aux" : "root";
        root.name = name;
        root.tag = tag;
        mods.add(root);
        Collections.sort(mods, new Comparator<MMod>() {
            @Override
            public int compare(MMod o1, MMod o2) {
                return getI(o1.reln) - getI(o2.reln);
            }
        });
        for (MMod mod : mods) {
            switch (mod.reln) {
                case "neg":
                    String mName = mod.name;
                    if (mName.contains("'")) {
                        mName = "not";
                    }
                    if (!cop && getMod("aux") == null && !mod.name.equals("never")) {
                        switch (root.tag) {
                            case "VBZ":
                                root.name = Utils.present(name, Person.FIRST);
                                mName = "doesn't";
                                break;
                            case "VBD":
                                root.name = Utils.present(name, Person.FIRST);
                                mName = "didn't";
                                break;
                            case "VB":
                            case "VBP":
                                mName = "don't";
                                break;
                        }
                    }
                    out += mName + " ";
                    break;
                case "root":
                case "aux":
                    out += mod.name + " ";
                    break;
                default:
                    out += mod.state() + " ";
                    break;
            }
        }
        if (out.endsWith(" "))
            out = out.substring(0, out.length() - 1);
        return out;
    }

    private boolean isCopule(String name) {
        return name.equals("am") || name.equals("is") || name.equals("are")
                || name.equals("was") || name.equals("were");
    }

    @Override
    public String toString() {
        return toString(2);
    }

    protected String toString(int s) {
        return "(" + name + ", " + tag + ", " + printMods(s) + ")";
    }

    private String printMods(int s) {
        String out = "";
        for (MMod mod : mods) {
            out += "\n" + spaces(s) + mod.toString(s + 2);
        }
        return out;
    }

    private String spaces(int s) {
        String out = "";
        for (int i = 0; i < s; i++) {
            out += "\t";
        }
        return out;
    }

    public boolean equals(MWord word) {
        return Utils.relates(this, word);
    }

    @Override
    protected MWord clone() {
        return null;
    }
}
