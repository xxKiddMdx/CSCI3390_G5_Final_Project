# Large Scale Data Processing: Final Project Report
## Graph matching Result from Using the LubyMIS & Greedy

## Approach

### Blossom Algorithm

* At first, based on the research, we was planning to implement the Blossom Algorithm for the matching. The Blossom algorithm effectively computes maximum matchings for graphs represented in input files, where each line details an undirected edge between two vertices. Upon reading the graph data, the algorithm operates by identifying "blossoms," which are odd cycles within the graph that need to be contracted to simplify the structure. Through iterative processing, it searches for augmenting paths that can increase the size of the existing matching. By repeatedly applying these steps—contracting blossoms and finding augmenting paths—the algorithm ensures the matching is as large as possible, leveraging advanced graph theory techniques to handle complex graph structures efficiently.However, as we learned and tested it out, the whole process consumes a huge amount of time, which it has the O(n^3). 

### LubyMIS (O(logE))

* The LubyMIS algorithm operates by having each node in the graph randomly select itself with a certain probability, while simultaneously checking if its neighbors have also chosen themselves. If a node selects itself and none of its neighbors do, it joins the independent set. This decision is communicated to all neighboring nodes to prevent them from joining the set if they haven’t already made the same decision. This process repeats in synchronous rounds across the entire graph, with nodes that have not yet decided continuing to select themselves randomly. The algorithm typically converges quickly, within O(logn) rounds, producing a maximal independent set efficiently.

* We are still working with it based on the foudation of project 3. 

### Maximal Matching (O(E+V)):
1. ```maximalMatching(graph: Graph[Int, Int]): List[(Long, Long)]```

**Purpose**: Finds a maximal matching in a graph.
**Method**: Collects all edges, filters by unmatched vertex pairs, and maintains a set of matched vertices.
**Output**: List of edges that form the maximal matching.

2. ```saveMatching(matching: List[(Long, Long)], outputFile: String)```
**Purpose**: Saves the matching to a CSV file.
**Method**: Converts matching list to DataFrame, then writes to file using DataFrame operations.
**Output**: CSV file containing the matching pairs.

3. ```verifyMatching(graph: Graph[Int, Int]): Boolean```
**Purpose**: Verifies if the matching is independent and maximal.
**Method**: Uses flatMap to check for independent edges and join operations to ensure all unmatched vertices are adjacent to matched vertices.
**Output**: Boolean indicating if the matching meets criteria.

### Greedy Algorithm (O(ElogE)) : 
* The Greedy Matching algorithm works as follows:
* Initialization
1. **Vertex Tracking**: A data structure is initialized to keep track of which vertices have been matched. This structure is crucial for ensuring that no vertex is part of more than one matching edge.
2. **Edge Sorting**: The input graph is preprocessed to sort its edges in a randomized order. This preprocessing ensures each execution of the algorithm can yield different results and prevents any inherent bias in the order of the edges.

* Processing the Edges
1. The algorithm processes each edge from the sorted list one by one.
2. For each edge:
   - **Vertex Check**: The source and destination vertices of the edge are checked to see if they have been matched.
   - **Edge Matching**: If both vertices are unmatched, the edge is eligible to be part of the matching:
     - The edge is added to the final matching result.
     - Both vertices are marked as matched so they cannot be part of any subsequent edges.

* Result Compilation
1. **Collecting Results**: The matched edges are collected from the algorithm's execution.
2. **Output**: The matched edges, which now form the maximal matching set, are returned or saved as the output.

## Code Compile

### Run commands:

 *Maximal Matching:
```
spark-submit --master "local[*]" --driver-memory 20G --executor-memory 20G --conf "spark.executor.memoryOverhead=1024" --conf "spark.driver.maxResultSize=4g" --conf "spark.network.timeout=600s" --conf "spark.executor.heartbeatInterval=120s" --class "final_project.main" target/scala-2.12/project_3_2.12-1.0.jar mm [path_to_graph] [path_to_output]
```

 *Greedy:
 ```
 spark-submit --master "local[*]" --driver-memory 20G --executor-memory 20G --conf "spark.executor.memoryOverhead=1024" --conf "spark.driver.maxResultSize=4g" --conf "spark.network.timeout=600s" --conf "spark.executor.heartbeatInterval=120s" --class "final_project.main" target/scala-2.12/project_3_2.12-1.0.jar greedy [path_to_graph] [path_to_output]
 ```
 
 *Verify:
 ```
spark-submit --master "local[*]" --driver-memory 20G --executor-memory 20G --conf "spark.executor.memoryOverhead=1024" --conf "spark.driver.maxResultSize=4g" --conf "spark.network.timeout=600s" --conf "spark.executor.heartbeatInterval=120s" --class "final_project.main" target/scala-2.12/project_3_2.12-1.0.jar verify [path_to_graph] [path_to_output]
 ```
 
## Result




## Advantages

### Greedy Algorithm

 **Simplicity**

* The greedy matching algorithm is straightforward and easy to understand. It iteratively selects edges for matching by checking if neither vertex is already matched, making it conceptually simple.
**Efficiency**

* The algorithm works efficiently by processing each edge only once. The use of a HashSet for tracking matched vertices ensures constant-time checks, allowing for rapid selection.

**Randomization**

* The inclusion of random shuffling within each partition helps reduce bias in edge selection, ensuring a fair distribution of matches across partitions.
 
**Parallel Processing**

* The algorithm is designed to work seamlessly with distributed computing frameworks like Apache Spark, leveraging partitions and caching to handle large datasets efficiently.

### Maximal Matching

**Comprehensive**

* The maximal matching algorithm aims to produce a valid and maximal matching by ensuring that all selected edges form a subset of the input graph and that no vertex has a degree greater than 1.

**Direct Approach**

* The algorithm directly iterates over edges and vertices, filtering them to produce a matching. This eliminates the need for random shuffling or additional processing steps, making it more streamlined.

**Explicit Checks**

* The algorithm includes explicit checks for independent and maximal properties, ensuring that each vertex is part of the matching or has a neighbor that is. This provides strong guarantees for the output.


## Comparison

**Greedy Matching**
* Offers simplicity, efficiency, and adaptability, making it suitable for general graph matching tasks. However, its time complexity can increase significantly with the logarithmic factor.

**Maximal Matching**

* Provides comprehensive and balanced processing, ensuring valid and maximal matchings while maintaining linear time complexity, particularly suitable for balanced or edge-dominated graphs.

**Use Cases**

* Greedy Matching: Works well for general, distributed tasks or when simplicity is prioritized.
* Maximal Matching: Suited for applications requiring valid and maximal matchings with explicit checks, such as scheduling or optimization.

## What if new dataset


### General Maximal Matching / Greedy Matching

* For "balanced" datasets, a general maximal matching algorithm is preferred. This ensures accurate coverage, allowing for more comprehensive results without compromising integrity. This approach ensures that each matching solution is valid and maximal, meaning no further edges can be added without violating the properties of a matching.

### Luby's Algorithm

* For larger datasets, particularly those in the realm of big data, Luby's algorithm can be used. This allows for more efficient processing by compromising some accuracy to provide a raw estimate.

**Efficient Convergence**

* The algorithm converges quickly because it iteratively reduces the set of active vertices, making the graph sparser with each iteration. This leads to fewer vertices to process in subsequent iterations, accelerating convergence.

