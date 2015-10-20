import json
import bz2
import pymorphy2
import nltk
import sys


class SuperTokenizer(object):

    #removing punctuation, prepositions, etc.
    PYMORPH_BAD_GRAMMEMES = ('PNCT', 'PRCL', 'CONJ', 'PREP', 'NPRO', )
    
    def __init__(self):
        self.morph = pymorphy2.MorphAnalyzer()
        self.snowball = nltk.stem.snowball.SnowballStemmer('english')
        self.english_stopwords = set(nltk.corpus.stopwords.words('english'))
        
    def normalize_word(self, word):
        lem = self.morph.parse(word)[0]
        if 'LATN' in lem.tag:
            # latin word: use snowball stemmer
            token = self.snowball.stem(word)
            if token in self.english_stopwords:
                token = None
        elif not any(((g in lem.tag) for g in SuperTokenizer.PYMORPH_BAD_GRAMMEMES)):
            # good russian word
            token = lem.normal_form
        else:
            token = None
        return token
        
    def tokenize_text(self, text):
        words = nltk.wordpunct_tokenize(text)
        for word in words:
            token = self.normalize_word(word)
            if token is not None:
                yield token
           
    def tokenize_html(self, html):
        text = BeautifulSoup.BeautifulSoup(html).getText(' ')
        return self.tokenize_text(text)

		
class TextProcessor:
	def __init__(self, input_path, output_path):
		self.input_path = input_path
		self.output_path = output_path
		
	def read(self):
	    with open(self.input_path) as f:
	        for line in f:
			    text = line[:(line.rindex(']') + 1)]
			self.posts = json.loads(text)
			
	def make_tokens(self):
		#build tokenizer
		tokenizer = SuperTokenizer()

		#open file for writing tokens
		with open(self.output_path, 'w') as post_output:
			for post_index, post in enumerate(self.posts):
				#tokenizing current post
				tokens = tokenizer.tokenize_text(post['text'])

				#writing tokens frim current post to file
				print >> post_output, post_index, ','.join(tokens).encode('utf-8') 
				
				if post_index % 1000 == 0:
					print '{0} post processed'.format(post_index)
		
		
if __name__ == "__main__":
    input_path = sys.argv[1]
	output_path = sys.argv[2]
	processor = TextProcessor(input_path, output_path)
	processor.read()
	processor.make_tokens()
    
	