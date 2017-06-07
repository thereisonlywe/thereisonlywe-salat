### This is a complete, open-source and easy-to-use Quran recitation and Salah practice API for to be used in Java and Android programming. It has two versions with similar methods; one for desktop (classic Java) and another for Android. It is dependent on [thereisonlywe-quran](https://github.com/thereisonlywe/thereisonlywe-quran), and thus you need to have that project (or its binaries) on your path. Installation is very straight forward; just add .jar files inside "bin" directory as libraries to your project and you are all set. Below are some examples of usage:

**Verse/Ayah Recitation:**
1. Construct a Dhikr object (a group of verses)
```java
Dhikr d = new Dhikr(Quran.getVerses(1)); //every ayah from first section in order
```
2. Construct a DhikrImam object with two reciters (one for the original text, another for its translation)
```java
DhikrImam imam = new DhikrImam(d, QuranReciterList.AFASY, QuranReciterList.WALK, false); //last parameter determines if required files are downloaded before recitation (when false, files will be downloaded while playing)
```
3. Set properties
```java
imam.setSkipProstration(true); //enable this if you don't want pauses after special sujud/prostration verses
imam.setLoop(true); //enable this for continous playback
imam.setVolume(50); //set volume (0-100)
imam.setReciterSwitchAllowed(false); //disable this if you want to force the selected reciter and disallow switching on download fails
imam.setTextReciterSwitchAllowed(false); //same as above, except for the translation reciter
imam.setRequireTextRecitation(true); //enable if you wish to force translation recitation
imam.setForceUpdates(true); //enable if you want to check the integrity (or version) of downloaded audio files
```
4. Begin recitation
```java
imam.begin();
```
5. Control flow
```java
imam.pause(); //pauses recitation at the end of the currently recited verse and on revive continues recitation with the next verse 
imam.terminate(); //pauses recitation immediately and on revive continues from exactly where leftoff  (just like a normal music player pause)
imam.terminate(); imam.terminate();  //when called twice with no revive inbetween, stops recitation altogether
imam.hold(); //pauses recitation immediately and on revive continues recitation with the repetition of the current verse from start
imam.revive(); //resumes recitation
imam.proceed(); //used after a sujud verse to continue recitation (not needed if setSkipProstration is set to true)
imam.resetReciters(); //used to change reciters during playback
```
6. Check state
```java
imam.getVerse(); //returns the verse currently being recited
imam.isPaused();
imam.isHeld();
imam.isAlive(); //true if a recitation is currently in progress
imam.getStatus(); //feedback on what imam is currently doing
```
-----

**Check "doc" folder for a complete methods overview.**
