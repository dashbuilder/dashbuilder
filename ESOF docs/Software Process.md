# Dashbuilder

## Contents
* [Dashbuilder](#dashbuilder)
	* [What is Dashbuilder?](#intro)
	* [Development process](#development-process)
		* [Methodologies](#methodologies)
		* [Contributing](#contributing)
		
		* [Releases](#releases)
		
	* [Sponsors](#sponsors)
	* [Critics](#critics)
	*[Members](#members)
	
<div id ='intro'/>
## What is Dashbuilder?

Dashbuilder is an open source platform for building business dashboards and reports, such as:

	* Interactive report tables 
	* Data export to Excel and CSV format 
	* Integration with the jBPM (Business Process Manager) platform


## Development Process

### Methodologies

We tried to get in contact with one of the owners and the main contributor of the project [David Gutierrez](https://github.com/dgutierr) in order to get some insight into the development process and methods used during the development of the application.
Unfortunately, he didn't reply to our email yet.

A mix of [FDD(Feature-Driven Development)](https://en.wikipedia.org/wiki/Feature-driven_development) and [FLOSS(Free and Open-Source Software)] (https://en.wikipedia.org/wiki/Free_and_open-source_software) looks to be used as a framework to develop the application.
From the documentation available, it seems that progress is made whenever a certain feature is needed or wanted, or whenever a contributor has fixed a bug or created a new feature. This is all acomplished with the help of Github tools such as the [issue tracker](https://github.com/dashbuilder/dashbuilder/issues) for discussing features and [pull requests](https://github.com/dashbuilder/dashbuilder/pulls) for reviewing and accepting the implementation of those features.


### Contributing

#### Help Users

Considered as the best way to contribute to Dashbuilder, helping other users is highly appreciated as it frees up development time from contributors.

A great place to start is by joining both [dashbuilder's IRC server](http://dashbuilder.org/help/chat.html) and the [discussion forum](http://dashbuilder.org/help/forum.html) and answering questions from users in need of help.


#### Issue Guidelines

Another valuable contribution is filing an issue in the [issue tracker](https://github.com/dashbuilder/dashbuilder/issues) for any bugs found or features wanted. If the issue already exists, helping reproduce the bugs or flesh out the new feature is also welcomed.


#### Pull Requests

Code contributions are made mainly by [Github's Pull Request feature](https://help.github.com/articles/using-pull-requests) and they should follow a couple guidelines:

1. Documentation must also be included for any new feature;
2. Send a pull request to `master` or to the respective branch.


### Releases

#### Schedule

New features are intended to be released every month with bug fixes being released as soon as they're spotted and corrected.

Bug fixes are only released for the latest features releases, which means that the oldest versions aren't supported as to not spread the limited resources.


#### Procedure

The news about a new release are spread through the Dashbuilder's IRC channel, [Twitter account](https://twitter.com/@dashbuilder) and at the [website](http://dashbuilder.org/learn/documentation.html).


## Sponsors

#### Apache
<a href="http://www.apache.org/licenses/">
<img src="./images/sponsors/apache.png" height="80"/>
</a>

Gives free license and supports the development of the project.


#### Redhat

<a href="https://www.redhat.com/en">
<img src="./images/sponsors/redhat.png" height="80"/>
</a>

The main company.


## Critics

Starting from the fact that this project is open-source with 17 contributors, with only 10 of them being part of the official team, it's fairly easy to assume they took a very incremental development approach. A big voluntary team is not very easy to coordinate and therefore the incremental aproach provides the most benefits.
Even the obvious drawbacks of this type of development, such as the constant need for code refactoring, should be overlooked. Of course it's one of the simplest methodologies but the simplicity is also a strength. It avoids the next step to be strict and brings many ideas and improvements and doesn't overwhelm and scare away potential contributors. The "skill ceiling" becomes lower.
It also allows two or more distinct features to be implemented at the same time and this type of flexibility further improves the case in defense of the incremental development methodology because it does not pose a limit on the number of features being added, because even though it is incremental, it is not necessarily successive.

