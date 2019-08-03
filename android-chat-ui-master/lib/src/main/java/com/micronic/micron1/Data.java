package com.micronic.micron1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by micronic on 14/7/15.
 */
public class Data {
    private final List<MRoot> data = new ArrayList<MRoot>();

    private MWord matches(MRoot root1, MRoot root2) {
        System.out.println("Beginning match: ");
        System.out.println("root: " + root1 + "\nwith root: " + root2);
        boolean ok = root1.equals(root2);
        boolean neg = false;
        MWord lastAns = null;
        System.out.println("ok1: " + ok);
        if (ok) {
            for (MMod mod1 : root1.mods) {
                if (!neg && mod1.reln.equals("neg"))
                    neg = true;
                MMod mod2 = root2.tryMod(mod1.reln);

                //workaround #1
                if (mod2 == null && mod1.reln.equals("advmod"))
                    mod2 = root2.tryMod("dobj");
                else if (mod2 == null && mod1.reln.equals("dobj"))
                    mod2 = root2.tryMod("xcomp");
                //workaround #1 end

                ok = mod1.matches(mod2);
                System.out.println("ok2: " + ok);
                if (!ok) {
                    if ((mod1.tag.equals("WP") || mod1.tag.equals("WRB")) && lastAns == null) {
                        System.out.println("In LastAns. An answer is found.");
                        lastAns = mod2 != null ? mod2 : root2;
                        ok = true;
                    } else
                        break;
                }
            }
            if (!neg)
                ok = ok && root2.getMod("neg") == null;
            System.out.println("ok3: " + ok);
        }
        if (!ok)
            System.out.println("Match failed.");
        return ok ? lastAns == null ? root2 : Utils.random(lastAns, root2) : null;
    }

    private MWord query(MRoot root) {
        boolean has = data.contains(root);
        if (!has) {
            for (MRoot root1 : data) {
                MWord mWord = matches(root, root1);
                if (mWord != null)
                    return mWord;
            }
        }
        return has ? root : null;
    }

    public MWord has(MRoot root) {
        return query(root);
    }

    public MWord ask(MRoot root) {
        return query(root);
    }

    public void add(MRoot root, boolean intro) {
        if (intro)
            data.add(root);
        else data.add(0, root);
    }
}
