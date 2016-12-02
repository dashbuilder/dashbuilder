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
