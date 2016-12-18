# Dashbuilder Software Evolution

## Contents
* [Dashbuilder Software Evolution](#dashbuilder-software-evolution)
    * [Introduction](#introduction)
    * [Feature](#feature)
        * [Feature Identification](#feature-identification)
        * [Feature Implementation](#implementaton)
		* [Pull requesting](#pull-requesting)
	* [Conclusion](#conclusion)
    * [Members and Contribution](#members-and-contribution)
	
## Introduction

As new requirements are laid upon the existing software there is the need for the product to evolve and respond to remain useful . As software products can be extensively complex , this evolution can be extremely expensive and very difficult, so there are specific processes to make it happen with ease. As a closure to our semester research our group was assigned to identify a part of this open-source project and develop/create it.

It's important to say that it was extremely difficult to implement new features or fix any bugs - after working on this project more than 2 months we only found one bug. The dimension of this project and the fact that has been growing up for almost 4 years makes it even harder to find any gaps.
Besides this not so positive points, the Dashbuilder compilation takes more than 3 minutes so debugging is very hard.


## Feature

### Feature Identification

After becoming more familiar with this web application and its interface we felt that some options weren't located very intuitively and so we decided to relocate one of them.
<br>
The search and its focus were aimed at the menu bar because of our familiarity with it, seeing as it was also there that we found the bug we reported.


### Feature Implementation

The effort of this change can be a bit underestimated if the only metric is lines of code changed/added, which in this case is 3.
<br>
There isn't a lot of 'creative' freedom because Dashbuilder relies heavily on the Uberfire framework. Even when trying to correct the aforementioned bug, and after many unsuccessful attempts with only little wiggle space we believe the error is caused by the Uberfire API itself.
<br>
<br>
All code changes were performed within the class `ShowcaseEntryPoint`:

* In function `getAdministrationMenuItems()` :
 - Initialized and empty `ArrayList result` with 3 elements instead of 4;
 - Deleted/commented the entry for the Security Menu Item.

* In function `getRoles` :
 - Add Security Menu Item to the Roles Menu;
 - Associate Security Menu Item to `SECURITY` perspective in order to make it clickable.
 
 Below you can check the before and after the changes.
 
 

	
### Pull requesting


##	Conclusion

The whole experience of reverse engineering an open-source project was very enlightening on what may possibly be our future in the IT market. Dealing with poorly documented projects, unreadable code, and projects that are bottlenecked at its conception, might very well be our daily basis. The concepts we learned and acquired while going through this project are very important and this was a great experience to understand their reach.


## Members and Contribution

- Gustavo Faria		33,3%		
	
- João Duarte		33,3%
	
- Nuno Pinto		33,3%
