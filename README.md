# SMArtOp
SMArtOp -- Sparse Matrix library for ARiThmetic Operations

SMArtOp is licenced under the Apache License V2.0.
Copyright 2016 - ISISTAN - UNICEN - CONICET

SMArtOp is a Java library for dividing the processing of large-scale sparse-matrix arithmetic operations on distributed environments.The software is designed for dividing and balancing the processing of large-scale sparse-matrix arithmetic operations into simpler and independent tasks to be executed in a distributed environment.


This software was used in:

* A. Tommasel, D. Godoy, A. Zunino, and C. Mateos. A Distributed Approach for Accelerating Sparse-matrix Arithmetic Operations for High-dimensional Feature Selection. Knowledge and Information Systems, pages 1-39, 2016. [DOI: 10.1007/s10115-016-0981-5](http://dx.doi.org/10.1007/s10115-016-0981-5).  

* A. Tommasel, C. Mateos, D. Godoy, and A. Zunino. Sparse-matrix arithmetic operations in computer clusters: A text feature selection application. In Proceedings of the 2nd IEEE ARGENCON, pages 458–463, June 2014. [DOI: 10.1109/ARGENCON.2014.6868535](http://dx.doi.org/10.1109/ARGENCON.2014.6868535)


=== Dependencies ===

SMArtOp depends of two libraries:

* The Trove library is licensed under the Lesser GNU Public License.
* The JPPF library is licensed under the Apache License V2.0.


=== Usage ===

Usage examples are included in the software distribution in the package test. Two alternative examples of use are provided. First, a stand-alone example in which two matrices are created and then multiplied. Second, an example in which the software is used in the context of a feature selection example. An example of the configuration file needed for executing the feature selection application is provided.

Additionally, a description of the software's architecture, supported matrix representations, supported matrix operations, code examples, and a performance evaluation can be found in the project's [Wiki](https://github.com/tommantonela/SMArtOp/wiki).


=== Data ===

The software was tested using the Digg dataset created by Yu-Ru Lin:
Yu-Ru Lin, Jimeng Sun, Paul Castro, Ravi Konuru, Hari Sundaram and Aisling Kelliher, MetaFac: Commmunity Discovery via Relational Hypergraph Factorization, in Proceedings of the 15th ACM SIGKDD Conference On Knowledge Discovery and Data Mining (KDD 2009)

The dataset can also be downloaded from:
http://ame2.asu.edu/students/lin/code/kdd09sup.zip
