
===========================================================

   Documentation for OpinionFinder 2

===========================================================

Contents:

  1.  Introduction

      1.1 Background
      1.3 Steps OpinionFinder Goes Through for Processing

  2.  System Requirements and Programs Used by OpinionFinder

  3.  Running OpinionFinder

  4.  Database Directory Structure

  5.  Subjectivity Classifiers

  6. Polarity Classifier

  7.  Acknowledgements

  8.  Contact Information

  9.  Citing Use of OpinionFinder

  10.  References

  11. List of Contributors

-----------------------------------------------------------

1. Introduction

OpinionFinder is a system that processes documents and automatically
identifies subjective sentences and sentiment expressions.
It outputs files using inline SGML markup. The "Background" section 
gives a brief description of subjectivity and sentiment expressions. 


1.1 Background

Subjective sentences express private states. Private states are
internal mental or emotional states, including speculations, beliefs,
emotions, evaluations, goals, and judgments. Below are a few examples
of subjective sentences:

   (1) Jill said, "I hate Bill."
   (2) John thought he won the race.
   (3) Mary hoped her presentation would go well.

Sentiment expressions are a type of subjective expression.  Specifically,
they are expressions of positive and negative emotions, judgments, 
evaluations, and stances. In the examples above, "hate" is a negative 
sentiment expression and "hope" is a positive sentiment expression.

For more information on subjectivity, subjective expressions, and 
the ways in which private states may be expressed in language, see 
Wiebe (2002) and Wiebe, Wilson, Cardie (2005).


1.2 Steps OpinionFinder Goes Through for Processing

OpinionFinder goes through the following steps:

1) Preprocessing

   Stanford Part Of Speech Tagger is used to sentence split and part-of-speech tag the
   documents and also for stemming.
   Output: gate_default

2) Feature Finder

   Clues useful for identifying subjective sentences and sentiment
   expressions are found in the text document.
   Output: subjclueslen1polar, subjcluesSentenceClassifiersOpinionFinerJune06, valenceshifters, intensifiers

3) Rule-based Subjectivity Classifier
   
   The rule-based subjectivity classifier relies on manually crafted rules to tag sentences 
   in the document as subjective or objective with high precision and low recall.
   Output: sent_rule.txt

4) Subjectivity Classifier
   
   The subjectivity classifier tags sentences in the document as
   subjective or objective based on a model trained on the MPQA Corpus. 
   Output: sent_subj.txt

5) Polarity Classifier

   The polarity classifier tags the words in the document with their
   contextual polarity based on a model trained on the MPQA Corpus. 
   Output: exp_polarity.txt

6) SGML markup

   The original document is re-writtern with inline SGML markup holding the output of the classifiers.
   Output: markup.txt

-----------------------------------------------------------

2. System Requirements and Programs Used by OpinionFinder

OpinionFinder is written completely in java and requires Java 1.6+. It has been on Linux, Windows and OS X systems with Java SE 6.

OpinionFinder is licensed under the GNU General Public License (http://www.gnu.org/licenses/gpl.html).

OpinonFinder relies on two external software: (1) Stanford Part of Speech Tagger (http://nlp.stanford.edu/software/tagger.shtml) and (2) Weka Datamining Software 
(http://www.cs.waikato.ac.nz/ml/weka/). The jar files for both systems are included in the download. Please see corresponding homepages for documentation and licensing information. 

-----------------------------------------------------------

3. Running OpinionFinder

OpinionFinder can be called from the command line. stanford and weka jars, which can be found in the lib directory, should be in the classpath. The following sample call will list the input options: 

Linux: java -classpath ./lib/weka.jar:./lib/stanford-postagger.jar:opinionfinder.jar opin.main.RunOpinionFinder
Windows: java -classpath lib\weka.jar;lib\stanford-postagger.jar;opinionfinder.jar opin.main.RunOpinionFinder

-d ... the input file holds a list of documents to be processed (default: the input file is a single document)
-s ... use the database structure for processed documents (default: the annotations are created in the same folder as the original file)
-r ... the modules to run, a comma seperated list (default: all modules --> preprocessor,cluefinder,rulebasedclass,subjclass,polarityclass,sgml)
-m ... the folder holding of the opinionfinder classifier models (default: 'models' directory)
-l ... the folder holding of the opinionfinder lexicons (default: 'lexicons' directory)
-e ... character set of the processed documents (default: UTF-8)
-w ... swsd support (default: false)

Below is a sample run of OpinionFinder on the provided test list of documents.

Linux: java -Xmx1g -classpath ./lib/weka.jar:./lib/stanford-postagger.jar:opinionfinder.jar opin.main.RunOpinionFinder test.doclist -d
Windows: java -Xmx1g -classpath lib\weka.jar;lib\stanford-postagger.jar;opinionfinder.jar opin.main.RunOpinionFinder test.doclist -d

The sample run will create a directory with intermediate and required output files for each document in the list. sent_rule.txt, sent_subj.txt and exp_polarity.txt hold the output of the classifiers. The first column is the id of the tagged entity and the second column is the label. The id has following format: documentid_spanstart_spanend. The documentid consists of the document name and its parent directory. spanstart and spanend give the character span of the tagged entity. For example, marktwain_letters16_7_174 represents an entity (a sentence in this case) in the document letters16 in the directory marktwain starting at the character position 7 and ending at the character position 174. The same span format is used in the output of the preprocessing and featurefinder steps.

There is also a SGML output to enhance readibility.

-----------------------------------------------------------

4. Database Directory Structure

The database directory contains two subdirectories: docs, auto_anns. Each subdirectory has the following structure:
	
		        subdir
		       /      \
		  parent  ..  parent
		 /     \          
	  docleaf  ...  docleaf

Within each subdirectory, each document is uniquely identified 
by its parent/docleaf.  For example, 20010927/23.18.15-25073, 
identifies one document.  20010927 is the parent; 23.18.15-25073 
is the docleaf.

4.1 database/docs

    The docs subdirectory contains the document collection to be
    processed. In this subdirectory, each docleaf (e.g.,
    23.18.15-25073) is a text file containing one document.
    Documents in this directory should not be edited, since the MPQA
    files used internally by OpinionFinder refer to spans in these
    documents. If you do edit a set of documents in the documents
    directory, you will need to rerun OpinionFinder on those
    documents.

4.2 database/auto_anns

    This subdirectory contains the automatic annotations for 
    the documents.  In this subdirectory, each docleaf 
    (23.18.15-25073) is a directory that contains several 
    intermediate and output files.

-----------------------------------------------------------

5. Subjectivity Classifiers

There are two classifiers included with OpinionFinder for identifying subjective and objective sentences (Riloff and Wiebe, 2004; Wiebe and Riloff, 2005). 
 
The first classifier tags each sentence as either subjective or objective based on a model trained on the MPQA Corpus. We do not use sundance patterns in this release. Based 10 fold-cross validation on 11168 sentences extracted from the MPQA Opinion Corpus, this classifier has an accuracy of 76%, subjective precision of 79%, subjective recall of 76%, and subjective F-measure of 77.5%.  The baseline accuracy is 54.3%.
 
The second classifier is a rule-based classifier. It optimizes precision at the expense of recall. That is, it classifies a sentence as subjective or objective only if it can do so with confidence.  Otherwise, it labels the sentence as "unknown.". This rule-based classifier is reported to have about 91.7% subjective precision  (91.7% of the sentences the system classifies as subjective are indeed subjective, according to the manual annotations) and 30.9% subjective recall (of all the subjective sentences, 30.9% are automatically classified as subjective, rather than objective, or unknown). Objective precision is 83.0% and objective recall is 32.8%).

For more information of the sentence subjectivity classifiers, please see Riloff and Wiebe (2004) and Wiebe and Riloff (2005).

-----------------------------------------------------------

6. Polarity Classifier

The polarity classifier takes clues consisting of words with a prior polarity of "positive", "negative" or "neutral" (for example, "love", "hate", "think", and "brag", respectively) and 
then uses a modified version of the classifier described in Wilson et al. (2005) to determine the contextual polarity of the clues. Heuristics were used to improve the speed of the classifier so 
it no longer needs the dependency parse output. The contextual polarity of the clues is then written to files in the auto_anns. When evaluated on the MPQA opinion corpus, the overall accuracy is 73.4%.

For more information of the polarity classifiers, please see Theresa Wilson, Janyce Wiebe and Paul Hoffmann (2005).

The user has also the option to do sense-aware polarity classification via Subjectivity Word Sense Disambiguation (SWSD). It can be activated with "-w" flag. In this version, the SWSD component can disambiguate only 134 lexicon clues (Akkaya, Wiebe and Rada 2009; Akkaya, Wiebe, Conrad and Rada 2011). Since it needs to be run for each clue instance separately, the classification will run slower. Thus, please use this component with caution. The integration of SWSD decisions is done in a similar way as described in Akkaya, Wiebe and Rada 2009.    

-----------------------------------------------------------

7. Acknowledgements

This work was supported by the Advanced Research and Development
Activity (ARDA), by the NSF under grants IIS-0208028, IIS-0208798 
and IIS-0208985, and by the Xerox Foundation.

-----------------------------------------------------------

8. Contact Information

Please direct any questions or problems that you have to the following
email address:

opin@cs.pitt.edu

-----------------------------------------------------------

9. Citing Use of OpinionFinder

Please cite the use of the various components of OpinionFinder
individually.

a. Subjective Sentence Classifiers:

Ellen Riloff and Janyce Wiebe (2003). Learning Extraction Patterns for
Subjective Expressions.  Conference on Empirical Methods in Natural
Language Processing (EMNLP-03). ACL SIGDAT. Pages 105-112.

Janyce Wiebe and Ellen Riloff (2005). Creating subjective and objective 
sentence classifiers from unannotated texts. Sixth International Conference 
on Intelligent Text Processing and Computational Linguistics (CICLing-2005).
 
b. Polarity Classifier

Theresa Wilson, Janyce Wiebe and Paul Hoffmann (2005). Recognizing
Contextual Polarity in Phrase-Level Sentiment Analysis. Proceedings of
Human Language Technologies Conference/Conference on Empirical Methods
in Natural Language Processing (HLT/EMNLP 2005), Vancouver, Canada.

c. SWSD Component

Cem Akkaya, Janyce Wiebe and Rada Mihalcea. (2009). Subjectivity 
Word Sense Disambiguation. (EMNLP 2009).

Cem Akkaya, Janyce Wiebe, Alexander Conrad and Rada Mihalcea (2011). 
Improving the Impact of Subjectivity Word Sense Disambiguation on Contextual 
Opinion Analysis. (CoNNL 2011).


d. Features and Clues of Subjectivity

subjcluesSentenceClassifiersOpinionFinderJune06.tff:

Ellen Riloff, Janyce Wiebe, and Theresa Wilson (2003).  Learning
Subjective Nouns Using Extraction Pattern Bootstrapping.  Seventh
Conference on Natural Language Learning (CoNLL-03). ACL SIGNLL.

Ellen Riloff and Janyce Wiebe (2003). Learning Extraction Patterns for
Subjective Expressions.  Conference on Empirical Methods in Natural
Language Processing (EMNLP-03). ACL SIGDAT. Pages 105-112.

subjclueslen1polar.tff:

Ellen Riloff, Janyce Wiebe, and Theresa Wilson (2003).  Learning
Subjective Nouns Using Extraction Pattern Bootstrapping.  Seventh
Conference on Natural Language Learning (CoNLL-03). ACL SIGNLL.

Ellen Riloff and Janyce Wiebe (2003). Learning Extraction Patterns for
Subjective Expressions.  Conference on Empirical Methods in Natural
Language Processing (EMNLP-03). ACL SIGDAT. Pages 105-112.
 
Theresa Wilson, Janyce Wiebe, and Paul Hoffmann (2005). Recognizing
Contextual Polarity in Phrase-Level Sentiment Analysis. Proceedings of
Human Language Technologies Conference/Conference on Empirical Methods
in Natural Language Processing (HLT/EMNLP 2005), Vancouver, Canada.

valenceshifters.tff and intensifiers2.tff:

Theresa Wilson, Janyce Wiebe, and Paul Hoffmann (2005). Recognizing
Contextual Polarity in Phrase-Level Sentiment Analysis. Proceedings of
Human Language Technologies Conference/Conference on Empirical Methods
in Natural Language Processing (HLT/EMNLP 2005), Vancouver, Canada.

-----------------------------------------------------------

10. References

Yejin Choi, Eric Breck, and Claire Cardie (2006).  Joint Extraction
of Entities and Relations for Opinion Recognition.  Conference on
Empirical Methods in Natural Language Processiong (EMNLP-2006).
 
Yejin Choi, Claire Cardie, Ellen Riloff, and Siddharth Patwardhan
(2005). Identifying Sources of Opinions with Conditional Random Fields
and Extraction Patterns.  Proceedings of Human Language Technology 
Conference/Conference on Empirical Methods in Natural Language 
Processing (HLT/EMNLP 2005), Vancouver, Canada.

Ellen Riloff (1996). Automatically Generating Extraction Patterns from
Untagged Text. Proceedings of the Thirteenth National Conference on
Artificial Intelligence (AAAI-96). Pages 1044-1049.

Ellen Riloff and Janyce Wiebe (2003). Learning Extraction Patterns for
Subjective Expressions.  Conference on Empirical Methods in Natural
Language Processing (EMNLP-03). ACL SIGDAT. Pages 105-112.

Ellen Riloff, Janyce Wiebe, and Theresa Wilson (2003).  Learning
Subjective Nouns Using Extraction Pattern Bootstrapping.  Seventh
Conference on Natural Language Learning (CoNLL-03). ACL SIGNLL.

Robert E. Schapire and Yoram Singer. BoosTexter: A boosting-based
system for text categorization. Machine Learning, 39(2/3):135-168,
2000.

Janyce Wiebe (2002). Instructions for Annotating Opinions in Newspaper
Articles. Department of Computer Science Technical Report TR-02-101,
University of Pittsburgh, Pittsburgh, PA.

Janyce Wiebe and Ellen Riloff (2005). Creating subjective and objective 
sentence classifiers from unannotated texts. Sixth International Conference 
on Intelligent Text Processing and Computational Linguistics (CICLing-2005).

Janyce Wiebe, Theresa Wilson, and Claire Cardie (2005). Annotating
expressions of opinions and emotions in language.  Language Resources
and Evaluation, volume 39, issue 2-3, pp. 165-210.

Theresa Wilson, Janyce Wiebe and Paul Hoffmann (2005). Recognizing
Contextual Polarity in Phrase-Level Sentiment Analysis. Proceedings of
Human Language Technologies Conference/Conference on Empirical Methods
in Natural Language Processing (HLT/EMNLP 2005), Vancouver, Canada.

Cem Akkaya, Janyce Wiebe and Rada Mihalcea. (2009). Subjectivity 
Word Sense Disambiguation. (EMNLP 2009).

Cem Akkaya, Janyce Wiebe, Alexander Conrad and Rada Mihalcea (2011). 
Improving the Impact of Subjectivity Word Sense Disambiguation on Contextual 
Opinion Analysis. (CoNNL 2011).

-----------------------------------------------------------

11. List of Contributors

University of Pittsburgh:
Janyce Wiebe
Cem Akkaya
Alexander Conrad
Yoonjung Choi
Paul Hoffmann
Colin Ihrig
Jason Kessler
Swapna Somasundaran
Theresa Wilson

University of Utah:
Ellen Riloff
Siddharth Patwardhan

Cornell University:
Claire Cardie
Eric Breck
Yejin Choi
