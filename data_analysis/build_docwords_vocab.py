import json
import glob
import collections
import os
import nltk.tokenize
import sys

MAX_DOCS = 100000


def get_word_index(w, word_index, vocab_words):
    global n_words
    if w not in word_index:
        word_index[w] = n_words
        vocab_words[n_words] = w
        n_words += 1
    return word_index[w]
        
def read_posts(token_path):
    posts = []  
    with open(token_path, 'r') as post_output:
        for line in post_output:
            if len(line.split(' ')) >= 2:
                posts.append(line.split(' ')[1])
    return posts
	
def build_docwords(docwords_tail_path, docwords_head_path, posts):
    n_words = 0
    n_entries = 0
    vocab_words = {}
    with open(docwords_tail_path, 'w') as f_docwords_tail:
	for n_doc, post in enumerate(posts):    

		if n_doc % 1000 == 0:
			print u'%d post processed' % (n_doc)

		bag_of_words = collections.Counter()

		for word in post.split(',')[:-1]:
			#not necessary when preprocessing was done
			#word = word.lower().replace(u'¸', u'å')
			if word != '':
				word_idx = get_word_index(word, word_index, vocab_words)
				bag_of_words[word_idx] += 1

		for word_idx, count in sorted(bag_of_words.iteritems(), key=lambda r: r[0]):
			print >>f_docwords_tail, n_doc+1, word_idx+1, count
			n_entries += 1

		if n_doc >= MAX_DOCS:
			break

    with open(docwords_head_path, 'w') as f:
	print >>f, n_doc, n_words, n_entries
	
    return vocab_words
		
def build_vocab(vocab_path, vocab_words):
    with open(vocab_path, 'w') as f:
		for word_idx in xrange(n_words):
			print >>f, vocab_words[word_idx]
	
	
if __name__ == "__main__":
    token_path = sys.argv[1] #input path 
    docwords_tail_path = sys.argv[2]     
    docwords_head_path = sys.argv[3] 
    vocab_path = sys.argv[4]
	
    word_index = {}
    n_words = 0
    n_entries = 0
    vocab_words = {}

    posts = read_posts(token_path)

    vocab_words = build_docwords(docwords_tail_path, docwords_head_path, posts)
    build_vocab(vocab_path, vocab_words)
