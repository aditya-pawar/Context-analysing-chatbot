package com.micronic.micron1;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import simplenlg.features.Person;

/**
 * Created by micronic on 13/7/15.
 */
public class Parser {
    private final static LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

    public static class Word {
        public int index;
        public String name;
        public String reln;
        public int relnIndex;
        public String tag;
        public List<Word> words = new ArrayList<Word>();
    }

    public static List<Word> getWords(String sent) {
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sent));
        List<CoreLabel> rawWords2 = tok.tokenize();
        Tree parse = lp.apply(rawWords2);
        List<Word> words = new ArrayList<Word>();
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = new ArrayList<TypedDependency>(gs.typedDependencies());
        for (TypedDependency td : tdl) {
            Word word = new Word();
            word.index = td.dep().index();
            String name = td.dep().value();
            if (name.equalsIgnoreCase("I"))
                name = "I";
            else name = name.toLowerCase();
            word.name = name;
            word.reln = td.reln().getShortName();
            word.relnIndex = td.gov().index();
            String tag = td.dep().toString(CoreLabel.OutputFormat.VALUE_TAG);
            word.tag = tag.substring(tag.lastIndexOf('/') + 1);
            words.add(word);
        }
        processWords(words);
        return words;
    }

    private static void processWords(List<Word> words) {
        for (Word word : words) {
            Word word1 = getWord(words, word.relnIndex);
            if (word1 != null)
                word1.words.add(word);
        }
    }

    private static Word getWord(List<Word> words, int index) {
        for (Word word : words) {
            if (word.index == index)
                return word;
        }
        return null;
    }

    private static MRoot fetchRoot(List<Word> words, int type) {
        for (Word word : words) {
            if (word.reln.equals("root")) {
                MRoot root = new MRoot(type);
                pWord(root, word);
                loopWord(root, word);
                return root;
            }
        }
        return null;
    }

    private static void pWord(MWord mWord, Word word) {
        mWord.name = word.name;
        mWord.tag = word.tag;
    }

    private static void loopWord(MWord mWord, Word word) {
        for (Word word1 : word.words) {
            MMod mod = new MMod();
            pWord(mod, word1);
            mod.reln = word1.reln;
            mWord.mods.add(mod);
            loopWord(mod, word1);
        }
    }

    private static void auxNormalize(MWord root) {
        List<MMod> mods = new ArrayList<MMod>(root.mods);
        for (MMod mod : mods) {
            String reln = mod.reln;
            if (reln.equals("aux") || reln.equals("dep")) {
                if (mod.name.equals("does")) {
                    root.name = Utils.present(root.name, Person.THIRD);
                    root.tag = "VBZ";
                    root.mods.remove(mod);
                } else if (mod.name.equals("did")) {
                    root.name = Utils.past(root.name, Person.THIRD);
                    root.tag = "VBD";
                    root.mods.remove(mod);
                } else if (mod.name.equals("do")) {
                    root.mods.remove(mod);
                }
            } else if (reln.equals("advcl") || reln.equals("xcomp") || reln.equals("ccomp")) {
                auxNormalize(mod);
            }
        }
    }

    private static void toggle(MWord root) {
        boolean toggled = false;
        for (MMod mod : root.mods) {
            if (Utils.toggle(mod) && !toggled) {
                MMod aux = root.getMod("aux");
                String name = aux == null ? root.name : aux.name;
                String out;
                switch (name.toLowerCase()) {
                    case "am":
                        out = "are";
                        break;
                    case "are":
                        out = "am";
                        break;
                    case "was":
                        out = "were";
                        break;
                    case "were":
                        out = "was";
                        break;
                    default:
                        out = name;
                        break;
                }
                if (aux == null) {
                    root.name = out;
                } else
                    aux.name = out;
                toggled = true;
            }
            toggle(mod);
        }
    }

    public static MRoot parse(List<Word> words, int type) {
        MRoot root = fetchRoot(words, type);
        assert root != null;
        copula(root);
        auxNormalize(root);
        toggle(root);
        return root;
    }

    public static int typeOf(List<Word> words, String sent) {
        if (words.isEmpty())
            return 0;
        Word first = words.get(0);
        String[] auxs = {"am", "is", "are", "was", "were", "have", "has", "had", "did", "do", "will", "shall",
                "would", "should", "might", "may"};
        String[] whos = {"what", "who", "where", "whom", "when", "which", "whose", "why", "how"};
        for (String aux: auxs) {
            if (first.name.equalsIgnoreCase(aux))
                return 1;
        }
        for (String who: whos) {
            if (first.name.equalsIgnoreCase(who))
                return 2;
        }
        if (first.reln.equals("aux") || first.reln.equals("cop") || first.reln.equals("dep"))
            return 1;
        else if (first.tag.equals("WP") || first.tag.equals("WRB") || sent.endsWith("?"))
            return 2;
        else if (first.tag.equals("VB"))
            return 3;
        else return 0;
    }

    private static void copula(MWord word) {
        MMod cop = word.getMod("cop");
        if (cop != null) {
            word.mods.remove(cop);
            MMod obj = new MMod("dobj", word.name, word.tag);
            word.name = cop.name;
            word.tag = cop.tag;
            List<MMod> mods = new ArrayList<MMod>(word.mods);
            for (MMod mod : mods) {
                String reln = mod.reln;
                if (reln.equals("advcl") || reln.equals("xcomp") || reln.equals("ccomp")) {
                    copula(mod);
                } else if (!(reln.startsWith("nsubj") || reln.startsWith("aux") || reln.startsWith("neg"))) {
                    word.mods.remove(mod);
                    obj.mods.add(mod);
                }
            }
            word.mods.add(obj);
        } else {
            for (MMod mod : word.mods) {
                String reln = mod.reln;
                if (reln.equals("advcl") || reln.equals("xcomp") || reln.equals("ccomp")) {
                    copula(mod);
                }
            }
        }
    }
}
