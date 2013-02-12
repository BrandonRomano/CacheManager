CacheManager
============

CacheManager simplifies writing to cache in an Android Application.  Currently, there is support for Caching Strings, Bitmaps, JSONObjects, and Byte Arrays.

There are plans to significantly expand the support for other data types, as this project is still young.

Writing to cache can now be executed in one line...
CacheManager.getInstance(applicationContext).write(stringToWrite, fileName);

Similarly, reading from cache can also be executed in one line.
String cacheData = CacheManager.getInstance(applicationContext).read(fileName);

This also simplifies an occurance of failure to read/write with a CacheTransactionException, making it easier to respond to a failure to read/write.

Included is the CacheManager library, as well as an example.



License
=======
Copyright 2013 Brandon Romano

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
