package com.micronic.micron1;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.english.Realiser;

/**
 * Created by micronic on 14/7/15.
 */
public class Utils {
    private final static Lexicon lexicon = new XMLLexicon();
    private final static NLGFactory factory = new NLGFactory(lexicon);
    private final static Realiser realiser = new Realiser(lexicon);
    private static Dictionary dictionary;

    static {
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public static String getType(String word, POS pos) {
        try {
            Synset synset = dictionary.lookupIndexWord(pos, word).getSenses().get(0);
            String type = getLexName(synset);
            if (pos == POS.NOUN)
                try {
                    return gender(synset, type, pos);
                } catch (Exception e) {
                    e.printStackTrace();
                    return type;
                }
            else if (pos == POS.VERB)
                return type;
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String gender(Synset synset, String type, POS pos) throws JWNLException {
        if (synset == null)
            return type;
        PointerTargetTree tree = PointerUtils.getHypernymTree(synset);
        for (PointerTargetNodeList nodeList : tree.toList()) {
            for (PointerTargetNode node : nodeList) {
                Synset synset1 = node.getSynset();
                if (synset1 == null)
                    continue;
                if (synset1.getPOS() != pos)
                    continue;
                for (Word word : synset1.getWords()) {
                    if (word.getPOS() != pos)
                        continue;
                    String[] words = word.getLemma().split("[\\s-]");
                    if (words.length > 2)
                        continue;
                    for (String word1 : words) {
                        if (word1.equals("male") || word1.equals("man") || word1.equals("boy"))
                            return "male";
                        else if (word1.equals("female") || word1.equals("woman") || word1.equals("girl"))
                            return "female";
                    }
                }
            }
        }
        return type;
    }

    public static String getLexName(Synset synset) {
        if (synset == null)
            return null;
        String lexName = String.valueOf(synset.getLexFileName());
        return lexName.substring(lexName.indexOf(".") + 1);
    }

    public static String past(String verb, Person person) {
        VPPhraseSpec spec = factory.createVerbPhrase(verb);
        spec.setFeature(Feature.TENSE, Tense.PAST);
        spec.setFeature(Feature.PERSON, person);
        return realiser.realise(spec).getRealisation();
    }

    public static String present(String verb, Person person) {
        VPPhraseSpec spec = factory.createVerbPhrase(verb);
        spec.setFeature(Feature.TENSE, Tense.PRESENT);
        spec.setFeature(Feature.PERSON, person);
        return realiser.realise(spec).getRealisation();
    }

    public static String perfect(String verb, Person person) {
        VPPhraseSpec spec = factory.createVerbPhrase(verb);
        spec.setFeature(Feature.FORM, Form.PAST_PARTICIPLE);
        spec.setFeature(Feature.PERSON, person);
        return realiser.realise(spec).getRealisation();
    }

    public static String gerund(String verb, Person person) {
        VPPhraseSpec spec = factory.createVerbPhrase(verb);
        spec.setFeature(Feature.FORM, Form.GERUND);
        spec.setFeature(Feature.PERSON, person);
        return realiser.realise(spec).getRealisation();
    }

    public static String plural(String noun) {
        NPPhraseSpec spec = factory.createNounPhrase(noun);
        spec.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
        return realiser.realise(spec).getRealisation();
    }

    public static boolean toggle(MMod mod) {
        boolean success = mod.reln.startsWith("nsubj");
        String out;
        switch (mod.name.toLowerCase()) {
            case "i":
            case "me":
                out = "you";
                break;
            case "my":
                out = "your";
                break;
            case "myself":
                out = "yourself";
                break;
            case "mine":
                out = "yours";
                break;
            case "you":
                out = mod.reln.startsWith("nsubj") ? "I" : "me";
                break;
            case "your":
                out = "my";
                break;
            case "yourself":
                out = "myself";
                break;
            case "yours":
                out = "mine";
                break;
            default:
                out = mod.name;
                success = false;
                break;
        }
        mod.name = out;
        return success;
    }

    private static POS getPOS(MWord word) {
        if (word.tag.startsWith("VB"))
            return POS.VERB;
        else if (word.tag.startsWith("NN"))
            return POS.NOUN;
        else if (word.tag.startsWith("JJ"))
            return POS.ADJECTIVE;
        else if (word instanceof MMod) {
            MMod mod = (MMod) word;
            if (mod.reln.startsWith("adv"))
                return POS.ADVERB;
            else if (mod.reln.equals("amod"))
                return POS.ADJECTIVE;
        }
        return POS.NOUN;
    }

    public static String getMeaning(String word) {
        try {
            for (IndexWord word1 : dictionary.lookupAllIndexWords(word).getIndexWordArray()) {
                String[] means = word1.getSenses().get(0).getGloss().split(";");
                String out = means[0] + (means.length == 1 ? "." : ". For example:\n");
                for (int i = 1; i < means.length; i++) {
                    out += i + ". " + means[i];
                    if (i != means.length - 1)
                        out += "\n";
                }
                return Character.toUpperCase(out.charAt(0)) + out.substring(1);
            }
        } catch (Throwable e) {
            return e.toString();
        }
        return "Sorry. This word is not found in our dictionary.";
    }

    public static boolean relates(MWord word1, MWord word2) {
        try {
            System.out.println("Relating " + word1.name + " with " + word2.name);
            if (word1.name.equals(word2.name)) {
                boolean relates = true;

                //workaround #2
                if (word1.tag.startsWith("N")) {
                    for (MMod mMod : word1.mods) {
                        MMod mMod2 = word2.getMod(mMod.reln);
                        if (mMod2 == null || !mMod.matches(mMod2)) {
                            relates = false;
                            System.out.println("Relates fails for " + mMod + " and " + mMod2);
                            break;
                        }
                    }
                }
                //workaround #2 end

                System.out.println(!relates ? "Relate fails." : "Relates.");
                return relates;
            }
            IndexWord indexWord1 = dictionary.lookupIndexWord(getPOS(word1), word1.name);
            IndexWord indexWord2 = dictionary.lookupIndexWord(getPOS(word2), word2.name);
            int relation = relate(indexWord1, indexWord2);
            System.out.println(relation == 0 ? "Relate fails." : "Relates.");
            /*System.err.print(" relation: "+relation);
            System.err.println();*/
            return relation == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int relate(IndexWord word1, IndexWord word2) {
        try {
            System.out.println("Relating from dictionary");
            String[] ignore = {"be"};
            boolean inignore1 = false, inignore2 = false;
            for (String ig : ignore) {
                if (!inignore1 && word1.getLemma().equalsIgnoreCase(ig)) {
                    inignore1 = true;
                }
                if (!inignore2 && word2.getLemma().equalsIgnoreCase(ig)) {
                    inignore2 = true;
                }
                if (inignore1 && inignore2)
                    break;
            }
            if (inignore1 && inignore2)
                return 1;
            else if (inignore1 || inignore2)
                return 0;
            word1.sortSenses();
            word2.sortSenses();
            for (int i = 0; i < 3 && i < word1.getSenses().size(); i++) {
                Synset synset1 = word1.getSenses().get(i);
                for (int j = 0; j < 3 && j < word2.getSenses().size(); j++) {
                    Synset synset2 = word2.getSenses().get(j);
                    RelationshipList list = RelationshipFinder.findRelationships(synset1, synset2, PointerType.HYPERNYM);
                    if (list != null && !list.isEmpty() && inDepth(list)) {
                        return 1;
                    }
                    list = RelationshipFinder.findRelationships(synset1, synset2, PointerType.ANTONYM);
                    if (list != null && !list.isEmpty() && inDepth(list)) {
                        return -1;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static boolean inDepth(RelationshipList list) {
        for (Relationship relationship : list) {
            //System.out.println("Relationship: " + relationship.getDepth() + ": " + relationship);
            if (relationship.getDepth() > 3)
                return false;
            try {
                if (((AsymmetricRelationship) relationship).getCommonParentIndex() > 3) {
                    return false;
                }
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    public static <T> T random(T... args) {
        if (args.length == 0)
            return null;
        List<T> list = Arrays.asList(args);
        Collections.shuffle(list);
        return list.get(0);
    }
}
