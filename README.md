# yelp_SIM
This project is Andrea Capuano and Gabriella Esposito submission for the yelp dataset challenge 2017. It is also the final project for the course of Big Data and business inteligence @ University Of Naples Federico II [2]. 

Our analysis is focused on social influence maximization, we attempt to find who is the most influencial user among the given dataset. To do so, we use the assumption that modern social networks tend to have community structure. We can identify such communities with different algorithms, we chose the Label Propagation Algorithm for its computational advantages and spark compatibility.

# Files description

 * Example.scala -> Working example with sample data (not from yelp).
 * LPAcalc.scala -> Run of the Label Propagation Algorithm on the yelp dataset, using mongodb and the cleaned user files.
 * Run.scala -> Run of the algorithm using yelp data.
 * Cleaner.java and Linkremoval.scala -> Utility scripts to clean up the datasets from unwanted parameters.
 
# Architecture

![Used architecture][arch]

[arch]: http://i.imgur.com/Yep1Zju.png "arch"

* Mongodb as NoSQL database.
* Databricks as computational platform.
* Spark as processional engine
* Scala as programming language
* GraphFrame + GraphX as graph abstraction libraries.
 
# Social Network Influence Analysis

The analysis we made on the yelp's dataset is a simple answer to the question "who are the most influencial users on Yelp". At this scope we worked only on the user's dataset. In particular an user has the following attributes:
  - User ID
  - Name
  - Number of "useful"
  - Number of "funny"
  - Number of "Cool"
  - Number of "followers"
  - Number of reviews
  - Number of each different compliment
  - List of friends (an array of user ids)
  - Years of elite

Using these attributes we modeled the computation of a coefficient that describes the impact of an user on the network and especially on its neighbors. Thanks to said coefficient, we are able to use Kempe's indipendent cascade model [1]. The basic idea is the following: given an user with impact K and N friends, if the user is active it has a chance to activate each of its N friends with a probability equal to K.  To do this a coin is flipped (a special coin with probability of each outcome equals to K and 1-K) on each of the user's friends who are yet to be activated. A positive outcome of the coin flip will result in the activation of the friend's node, a negative outcome will result in an inability to be furtherly activated from the same user.

The impact coefficient's formula is calculated as follows:
We started collecting the maximum value of each attribute in the whole data-set an then considered for each user the ratio between his attributes and the relative max value. After that we took a percentage of each of this ratio and added them together. The meaning of this percentage relies on the idea that each attribute has a different value in term of impact in a possible influence of an other user (for example an user will be more likely to be influenced if I have more "usefuls" than "friends").

Let's now look at the general idea of calculating the most influential users.
The graph that comes out of the user data set is particularly large (about a million users) and therefore Kempe's greedy approach for influence calculation each node would require computational resources and time to compute that we are unable to sustain. 
So let's make some assumptions:
- Every user with zero friends will be cut off the graph
- The algorithm will be runned only on a well-defined number of vertices established among those who have the higher pagerank
- We'll divide the initial network into communities and each of them will represent a subnetwork to be analyzed separately (this assumes that the likelihood that a user belonging to a community influences one belonging to a different community is really low to the point of neglecting it)


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

![example1][ex]

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

# Citations

1.  [Maximizing the Spread of Influence through a Social Network](https://www.cs.cornell.edu/home/kleinber/kdd03-inf.pdf)

2. [Big Data analytics and Business intelligence @ Federico II by Professor Antonio Picariello](https://www.docenti.unina.it/insegnamenti/programmaCompletoPub.do?nomeDocente=ANTONIO&cognomeDocente=PICARIELLO&idDocente=414e544f4e494f504943415249454c4c4f5043524e544e3634543038413530394a&codInse=U0603&nomeInsegnamento=BIG%20DATA%20ANALYTICS%20AND%20BUSINESS%20INTELIGENCE&codCorso=M63&nomeCorso=INGEGNERIA%20INFORMATICA%20&progInse=1)


