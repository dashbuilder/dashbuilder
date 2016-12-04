# Dashbuilder Software Verification and Validation

## Contents
* [Dashbuilder Software Verification and Validation](#dashbuilder-software-verification-and-validation)
    * [Introduction](#introduction)
    * [Degree of Testability](#degree-of-testability)
        * [Controllability](#controllability)
        * [Observability](#observability)
        * [Isolateability](#isolateability)
        * [Separation of concerns](#separation-of-concerns)
        * [Understandability](#understandability)
        * [Heterogeneity](#heterogeneity)
    * [Test Statistics](#test-statistics)
    * [Bug Report](#bug-report)
	* [Members and Contribution](#members-and-contribution)

	
## Introduction

Verifying a piece of software is to ensure that both the intermediate and final product conform to their specification and requirements.
On the other hand, to validate software is to make sure that the final product will fulfill its intended environment, mainly through the use of tests.

The main techniques used for verifying and validating (V&V) are **static techniques** and **dynamic techniques**:

- **Static techniques**: analyze the static system representations to both find problems/bugs and evaluate quality. This includes reviews, inspections, formal verification, etc.
- **Dynamic techniques**: execute the system and observe its behavior (software testing and simulation).

In Dashbuilder the software is constantly and consistently validated through the use of tools such as [Jenkins CI](https://jenkins.io/) which runs the test suite when code is pushed to the repository.

Contributions to the project are strict, as it is required to include tests for code/features in order to be accepted upstream.


## Degree of Testability

### Controllability

As described below in [Isolateability](#isolateability) the packages are very well organized and isolated which makes it very easy to control the state of a component as required for testing.
Dashbuilder also makes use of some Dummy objects, like the widgets and backend, to make keep complexity low.


### Observability

Dashbuilder tests can be ran locally making use of [Apache Maven](https://maven.apache.org/) which gives us a complete view of the results as well as code coverage.
It also can be ran when doing a pull request, making use of [Jenkins CI](https://jenkins.io/) and its [Pipeline](https://jenkins.io/projects/blueocean/) to analyze test results and code coverage, which provides a graphical interface to analyze data.


### Isolateability

Dashbuilder has unit tests for most modules, mostly because each contribution must include tests in order to be accepted, which means that the isolateability is achieved because everything is confined in its module.



## Test Statistics

Running `mvn clean install` on the project's root directory results in an error halfway through, therefore indivudual packages had to be built independently. Luckily, intelliJ's Maven integration allows us to create a custom configuration so that unit testing and code coverage can be done on all successful packages at once. And these are the end results:

<img src="./images/tests/coverage.png"/>

Test Results:<br><br>
<img src="./images/tests/testResults.png"/ width="400">
<br><br>
Most of these are unit tests, however, and solely located in package `org.dashbuilder.dashbuilder-backend.dashbuilder-dataset-sql-tests` are this project's [Integration Tests](https://en.wikipedia.org/wiki/Integration_testing)
<br>

For a full report visit:
<br>
[Unit Test Report](./tests/testResults.html)
<br>
[Code Coverage Report](./tests/CodeCoverage/index.html)

<a href="./tests/CodeCoverage/index.html">


## Bug Report


## Members and Contribution

- Gustavo Faria		33,3%		
	
- João Duarte		33,3%
	
- Nuno Pinto		33,3%
