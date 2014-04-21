LambTrackerMobile
=================

Android program for managing a sheep flock. Uses both EID and visual tagging to document sheep. 

We are shepherds, programming of this item is happening in modules as we need to do different tasks with the flock during the year.

First module that is working is ConvertToEID which allows the look up of a sheep based on official US federal or farm tags, adding and removing tags and adding an electronic tag.

Second module that was finished is EvaluateSheep to do customized evaluations of a sheep flock including scoring traits, weights and scrotal circumference and other measured characteristics. 

LookUpSheep is now working but only find the first sheep if there are duplicate tags.

Sheep Managment handles toe trimming, wormer and vaccines but needs work for other typical management additions. 

Lambing and it's companion activity AddLamb is done, ready to test with lambing ewes in another few days. 

Alerts and notes are done as are all the breeding records needed to make the lambing module work properly. 

Development will be slow for the next month or so as we lamb out the flock. I'll probably only be doing bug fixes as we uncover isues during lambing. 

System is based on an SQLite database for all flock data.

Currently I set up the database on my desktop system by hand using various SQLite management tools. Then the database is moved to the Android and used there.

