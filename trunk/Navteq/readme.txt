= Locify in emulator =
We have tried to search an emulator to one of the devices for LBS Challenge. But unfortunatelly we didn't find any for mobile Java.

We decided to test Locify on generic Nokia S60 3rd Edition Feature Pack 2 emulator. It has same operating system as device Nokia N85.

= Emulator installation =
Download 'Nokia S60 3rd Edition Feature Pack 2' emulator from: http://developer.nokia.com/info/sw.nokia.com/id/ec866fab-4b76-49f6-b5a5-af0631419e9c/S60_All_in_One_SDKs.html

Install it and run 'Start->S60 Developer Tools->3rd Edition FP2 SDK->v1.1->Emulator'

This emulator does not have required certificates. You need to install UNSIGNED version of Locify in emulator. Select File->Open (in emulator window) and load unsigned JAD/JAR, complete installation of Locify.

Now you need to setup midlet permissions. Go to Tools->Preferences->MIDP Security (in emulator window) and setup: Run Midlet in folowing domain: Manufacturer

Now you can find Locify in Applications and run it.

= Locify on the real phone =
When you run Locify on the real phone, you need SIGNED version of application. Otherwise, you will be unable to set up right permissions. 

Locify is compatible with many phones, we recomend to use Nokia N85.

Howewer Locify should run on all devices for LBS Challenge excluding Samsung GT-i8510