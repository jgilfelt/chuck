Change Log
==========

Version 2.0.0 *(2018-xx-xx)*
----------------------------

 * New: Adds a class ChuckCollector that can be used out of ChuckInterceptor.
 * New: Chuck can now collect throwables.
 * New: Adds a notification for throwables.
 * New: Adds screens for throwables.
 * New: Transaction class offers a fluent writing.
 * New: Adds Chuck.registerDefaultCrashHanlder for **debugging purpose only**.
 * Breaking: Chuck.getLaunchIntent needs one more parameter.
 * Breaking: Built with Android plugin 3.1.x.
 * Breaking: Target SDK version 27.
 * Breaking: Support library 27.1.1.
 * Breaking: API classes are now in package `api`.
 * Misc: Move some internal classes.

Version 1.1.0 *(2017-08-06)*
----------------------------

 * Fix: Supports apps targeting Android O (API 26).

Version 1.0.4 *(2017-02-22)*
----------------------------

 * New: Displays uncompressed gzip encoded request/response bodies when used as a network interceptor.

Version 1.0.3 *(2017-02-14)*
----------------------------

 * New: Adds a maximum content length threshold, beyond which bodies are truncated.
 * New: Adds a data retention length property and cleanup task.
 * New: Adds a clear action to the notification.
 * Fix: Mitigates against CursorWindow blowout when transactions are large.

Version 1.0.2 *(2017-02-10)*
----------------------------

 * Fix: Added Proguard rule for compat SearchView.
 * Fix: Null search query displaying invalid results.

Version 1.0.1 *(2017-02-09)*
----------------------------

 * New: Adds a search action which filters on request path or response code.
 * New: Adds a transaction count to the notification.
 * Fix: Limits the size of the static transaction buffer correctly.

Version 1.0.0 *(2017-02-07)*
----------------------------

Initial release.