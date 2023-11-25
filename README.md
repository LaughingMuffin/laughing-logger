![Icon](./app/src/main/res/drawable/logo_01_nobg_2k.png)
=========
It's CatLog, but with material goodness.

Graphical log reader for Android.
Based on Daniel Ciao's
Matlog [Google Play](https://play.google.com/store/apps/details?id=com.pluscubed.matlog),
[Github](https://github.com/pluscubed/matlog) <br> which is based on Nolan Lawson's
CatLog: [Google Play][1], [GitHub][2]


[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Google Play" height="90">](https://play.google.com/store/apps/details?id=org.laughing.logger)

Overview
---------
Laughing Logger is a free and open-source material-style log reader for Android based on CatLog.

It shows a scrolling (tailed) view of the Android "logcat" system log,
hence the goofy name. <br> It also allows you to record logs in real time, send logs via email,
and filter using a variety of criteria.

FAQs
-------------
Taken from CatLog's FAQ:

#### Why I can't save logs or start recording ?

The applications might be missing "Storage" permission if you are on an older Android version (Android 10 or less)
and/or READ_LOGS permission has not been granted.

#### Where are the logs saved?

On the internal storage, under ```/internal storage/Laughing Logger/saved_logs/``` if <= A10, <br>
under ```/internal storage/Android/media/org.laughing.logger/Laughing Logger/saved_logs/``` if >= A11.

#### How to I access saved logs ?

A simple "file manager" application you can us is [Files by Google](https://play.google.com/store/apps/details?id=com.google.android.apps.nbu.files).

#### I can't see any logs!

This problem typically shows up on custom ROMs. First off, try an alternative logging app, to verify
that the problem is with your ROM and not MatLog.

Next, see if your ROM offers system-wide settings to disable logging. Be sure to reboot after you
change anything.

If that still doesn't work, you can contact the creator of your ROM to file a bug/RFE.

Development
-------------

- Select `fdroid` build variants to build and run immediately
- For `play` variants:
    - Put `google-services.json` from Firebase in app/src/main/play/
    - Put signing keys in local.properties

License
---------

```
Copyright (C) 2023  Laughing Muffin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

```

[1]: https://play.google.com/store/apps/details?id=com.nolanlawson.logcat

[2]: https://github.com/nolanlawson/Catlog

[3]: https://plus.google.com/u/0/communities/108705871773878445106
