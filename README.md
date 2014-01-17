map-window-recorder
=======

Simple app demonstrating how to record and replay Google Maps Android API v2 windows for use for data generation for testing apps that use the map. Map windows can be saved and loaded from the device using a serializable LatLngBounds wrapper. 

Instructions
=====
To begin, run the app and select the Record menu item which will record up to 50 map window movements. The number of markers recorded can be modified by changing NUM_WINDOWS. 

The current map windows can be replayed by selecting the Play menu item. This will go through each map window with a 1.5 second delay between each map window update. The delay can be modified by changing PLAY_DELAY. 

To save the current map windows to your device for loading between instances, select the Save menu item. To load saved map windows select the Load menu item.


About
=====
A similar approach was used in data generation for the experiments in SAC: Semantic Adaptive Caching for Spatial Mobile Applications. http://www.umiacs.umd.edu/~bfruin/papers/sigspatial13-sac-paper.pdf. 
