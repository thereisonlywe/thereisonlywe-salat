### This is a complete, open-source and easy-to-use Quran recitation and Salah practice API for to be used in Java and Android programming. It has two versions with similar methods; one for desktop (classic Java) and another for Android. It is dependent on [thereisonlywe-quran](https://github.com/thereisonlywe/thereisonlywe-quran), and thus you need to have that project (or its binaries) on your path. Installation is very straight forward; just add .jar files inside "bin" directory as libraries to your project and you are all set. Below are some examples of usage:
-----

**Verse/Ayah Recitation:**

0. Set context on Android
```java
RecitationManager.setContext(context);
```
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
imam.setRelentlessAudioFocus(true); //on android: allow other apps to stop ongoing recitation
imam.setGainFocus(true); //on android: whether to take over audio channel
```
4. Begin recitation
```java
imam.begin();
```
5. Control flow
```java
imam.pause(); //pauses recitation at the end of the currently recited verse and on revive continues recitation with the next verse 
imam.terminate(); //pauses recitation immediately and on revive continues from exactly where leftoff  (just like a normal music player pause)
imam.terminate(); imam.terminate();  //when called twice with no revive in-between, stops recitation altogether
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
7. Convenience
```java
Imam.getAliveImam(); //(static) to get a reference to ongoing recitation without passing the Imam object across classes
DhikrImam.propose(imam); //(static) to pass an Imam object with current state to another class (most likely to a service) 
DhikrImam imam = new DhikrImam(); //reinitialize state from proposition by calling the empty constructor 
```
-----

**Athan/Iqama Recitation:**

1. Select desired athan file (reciter, athan type)
```java
File athan = RecitationConstants.getAthanFile(RecitationConstants.getRandomReciterPath(), RecitationConstants.ATHAN_FAJR_PATH); //notice there are 5 different files for each reciter for athan to be recited with appropriate maqam and sound different
```
2. Create Imam object from file
```java
Imam imam = new Imam(athan);
Imam imam = new Imam(Imam.IQAMA);
```
3. Begin recitation
```java
imam.begin();
```
4. Control flow
```java
imam.terminate(); //pauses recitation immediately and on revive continues from exactly where leftoff  (just like a normal music player pause)
imam.revive(); //resume
imam.terminate(); imam.terminate();  //when called twice with no revive in-between, stops recitation altogether
```
5. Convenience
```java
athan = RecitationConstants.getExistingAthanFile(RecitationConstants.ATHAN_DHUHR_PATH); //use this when you don't know which reciters are available offline
```
-----

**Prayer/Salah Recitation:**

1. Create a Prayer object (length, number of rakaat, reciter, verse randomization method, recite Iqama before, recite a short prayer after, randomization of wait times in millis, wait times in millis)
```java
Prayer prayer = new Prayer(Prayer.VERSE_RANGE_NARROW, 2, QuranReciterList.RIFAI, Prayer.SALAT_RANDOMIZATION_IN_SECTION, true, false, 1500, new int[] { 7000, 2000, 3000, 12000, 5000,
			9000, 5000, 7000, 5000, 15000, 40000, 3000 });
```
2. Create a PrayerImam from Prayer
```java
PrayerImam imam = new PrayerImam(prayer);
```
3. Begin recitation
```java
imam.begin();
```
4. Notes

Think twice before using PrayerImam on Android; it is very easy to lose audio focus.
-----

**Required Permissions for Android:**
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```
-----

**Check "doc" folder for a complete methods overview.**
