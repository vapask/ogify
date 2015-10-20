import artm.messages_pb2, artm.library
import sys, glob
import numpy as np


OBJECTIVE_TOPICS_NUM = 50
BACKGROUND_TOPICS_NUM = 20
ITER_NUM = 3


def build_batches(batches_path, docwords_path, vocab_path):
   #parse collection
   target_folder = batches_path
   if not glob.glob(batches_path+'/*.batch'):
	artm.library.Library().ParseCollection(
		docword_file_path=docwords_path,
		vocab_file_path=vocab_path,
		target_folder=batches_path)

   # Find file names of all batches in target folder
   batches = glob.glob(batches_path+'/*.batch')
	
   return batches

def get_theta(batches, model):
    theta_set = []
    for batch_index, batch_filename in enumerate(batches):
	# select the first batch for demo purpose
        test_batch = artm.library.Library().LoadBatch(batch_filename)
        theta_matrix, numpy_matrix = master.GetThetaMatrix(model=model, batch=test_batch)
        theta_set.append(numpy_matrix)
        print numpy_matrix.shape        
    theta_matrix = np.concatenate(theta_set, axis=0)
    return theta_matrix

def create_model(batches):
    # Create master component
    with artm.library.MasterComponent() as master:
        master.config().cache_theta = True
	master.config().processors_count = 2
	master.Reconfigure()
	master.ImportDictionary('dictionary', os.path.join(batches_path, 'dictionary'))

	background_topics = []
	objective_topics = []
	all_topics = []

	for i in range(0, OBJECTIVE_TOPICS_NUM + BACKGROUND_TOPICS_NUM):
		topic_name = "topic" + str(i)
		all_topics.append(topic_name)
		if i < OBJECTIVE_TOPICS_NUM:
			objective_topics.append(topic_name)
		else:
			background_topics.append(topic_name)

	perplexity_score = master.CreatePerplexityScore()
	sparsity_theta_objective = master.CreateSparsityThetaScore(topic_names=objective_topics)
	sparsity_phi_objective = master.CreateSparsityPhiScore(topic_names=objective_topics)
	top_tokens_score = master.CreateTopTokensScore()
	theta_snippet_score = master.CreateThetaSnippetScore()

	# Configure basic regularizers
	theta_objective = master.CreateSmoothSparseThetaRegularizer(topic_names=objective_topics)
	theta_background = master.CreateSmoothSparseThetaRegularizer(topic_names=background_topics)
	phi_objective = master.CreateSmoothSparsePhiRegularizer(topic_names=objective_topics)
	phi_background = master.CreateSmoothSparsePhiRegularizer(topic_names=background_topics)
	decorrelator_regularizer = master.CreateDecorrelatorPhiRegularizer(topic_names=objective_topics)

	# Configure the model
	model = master.CreateModel(topics_count = len(all_topics), inner_iterations_count = 15, topic_names = all_topics)
	model.EnableRegularizer(theta_objective, -0.2)
	model.EnableRegularizer(theta_background, 0.2)
	model.EnableRegularizer(phi_objective, -0.002)
	model.EnableRegularizer(phi_background, 0.002)
	model.EnableRegularizer(decorrelator_regularizer, 100000)
	model.Initialize('dictionary')  # Setup initial approximation for Phi matrix.

	# Online algorithm with AddBatch()
	update_every = master.config().processors_count
 
	for iteration in range(0, ITER_NUM):
		for batch_index, batch_filename in enumerate(batches):
			master.AddBatch(batch_filename=batch_filename)
			if ((batch_index + 1) % update_every == 0) or ((batch_index + 1) == len(batches)):
				master.WaitIdle()  # wait for all batches are processed
				model.Synchronize(decay_weight=0.9, apply_weight=0.1)  # synchronize model
				
				print "Perplexity = %.3f" % perplexity_score.GetValue(model).value,
				print ", Phi objective sparsity = %.3f" % sparsity_phi_objective.GetValue(model).value,
				print ", Theta objective sparsity = %.3f" % sparsity_theta_objective.GetValue(model).value

	artm.library.Visualizers.PrintTopTokensScore(top_tokens_score.GetValue(model))
	artm.library.Visualizers.PrintThetaSnippetScore(theta_snippet_score.GetValue(model))
	
	
if __name__ == "__main__":
    batches_path = sys.argv[1]
    docwords_path = sys.argv[2]
    vocab_path = sys.argv[3]
	
    batches = build_batches(batches_path, docwords_path, vocab_path)	
    model = create_model(batches)	
    theta_matrix = get_theta(batches, model)
