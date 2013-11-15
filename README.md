LambTrackerMobile
=================

Android program for managing a sheep flock. Uses both EID and visual tagging to document sheep. 

We are shepherds, programming of this item is happening in modules as we need to do different tasks with the flock during the year.

First module that is working is ConvertToEID which allows the look up of a sheep based on official US federal or farm tags, adding and removing tags and adding an electronic tag.

Second module that is in works is EvaluateSheep to do customized evaluations of a sheep flock including scoring traits, weights and scrotal circumference and other measured characteristics. I am working to add some user defined scored traits where the options are specified by the user. 

LookUpSheep is in process, it works for an EID tag but is failing to look up sheep based on other identification types right now. 

Pieces that are mostly working but have some issues include reading alerts for sheep for items like send to butcher, which ram to put the ewe to and so on. We are using these alerts now as we sort and separate the flock into keep, sell, breed and slaughter groups. 

System is based on an SQLite database for all flock data.

Currently I set up the database on my desktop system by hand using various SQLite management tools. Then the database is moved to the Android and used there.

