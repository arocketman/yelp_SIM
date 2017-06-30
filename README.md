# yelp_SIM
Social Influence maximization, a community based approach for yelp dataset Challenge 2017

# Files description

 * Example.scala -> Working example with sample data (not from yelp).
 * LPAcalc.scala -> Run of the Label Propagation Algorithm on the yelp dataset, using mongodb and the cleaned user files.
 * Run.scala -> Run of the algorithm using yelp data.
 * Cleaner.java and Linkremoval.scala -> Utility scripts to clean up the datasets from unwanted parameters.

# GraphX + Pregel Algorithm

The algorithm we created uses GraphX along with Pregel API. We want to measure how many nodes can be influenced starting from a single user. 

As we previously talked about we use an impact coefficient that is calculated by taking into account the history of the single user. 

Our algorithm is based on the IC model [1] works by Kempe et al. 
Here's an explanation of how the algorithm works:

1. In the superstep 0, every node receives a message containing the name of the node that starts the whole process. When the initiator node receives the message, it will be activated.

2. All the active nodes, will send messages to their inactive neighbours. The message payload contains the impact coefficient of the activator node. The coefficient itself is an 'activation probability'. The higher the coefficient, the more probable is the activation of the recipient's node. 

3. All inactive nodes will receive a message containing such activation probability. A random number is thrown and the node will have a chance to be activated. If the activation is unsuccessful, there won't be another attempt of activation by the same node. If the activation is successful, the node will be starting to send messages to its inactive neighbours as per step 2.

4. The process ends when no further nodes can be activated. This can happen if the whole network was activated or all the inactive nodes were unsuccessfully activated and no further attempts can be made as per step 3. 

If two or more nodes attempt to activate the same node, the merge function steps in. In this case the recipient node can be activated by any of the activators, the one who managed to activate the node is not important since we know which vertex the process was started from.

# An example:

* Start from the node 4, the node attempts to activate node 2 and 3 but successfuly activates only 2.
* 4 won't attempt to activate 3 again.
* 2 attempts to activate 1 and succeeds
* 1 attemtps to activate 5 and 3 but only actiavtes 3. 

![alt text][ex]

[ex]: https://thumbs.gfycat.com/ParallelBriskCapeghostfrog-size_restricted.gif "Example run"

# Execution example: 

![Example two][ex2]

[ex2]: http://i.imgur.com/zSLg8bl.png "Example two"

Given the following network, here's an execution output:

1. Vertex 1 , destination id: 2 , value of 0.7
2. Vertex 1 , destination id: 3 , value of 0.7
3. Vertex 1 , destination id: 5 , value of 0.7
4. Vertex 5 , influenced by : 1 , because I received 0.7 and got a value of 0.6831
5. Vertex 3 , influenced by : 1 , because I received 0.7 and got a value of 0.5820
6. Vertex 3 , destination id: 4 , value of 0.5

The script finishes since 3 didn't manage to influence two. In the end 1 influences three nodes (1,3,5). Multiple executions give more results that can be averaged:

1 -> influences 1,3,5 (3 nodes)
1 -> influences 1,2,3,4 (4 ndoes)
1 -> influences 1,2,3,4,5 (5 nodes)

1 influences an average of 5 nodes.




