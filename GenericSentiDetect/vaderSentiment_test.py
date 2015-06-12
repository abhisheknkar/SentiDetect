import sys
#from vaderSentiment import sentiment as vaderSentiment
from vaderSentiment.vaderSentiment import sentiment as vaderSentiment

Sentence = sys.argv[1]
out = vaderSentiment(Sentence)
print out
