This is a custom build of the JW Player 5.1 (SVN revision 898)
There is a bug fix for YouTube under https in the class YouTubeMediaProvider.as, line 86:

Branislav Balaz added 20.06.2014:
As solution for issue OLAT-6930 new player with name player.swf from 
external site http://developer.longtailvideo.com/trac/browser/tags/mediaplayer-5.10/player.swf
has been set on. This allow to play youtube videos for TinyMCE editor Add/edit video button, option "YouTube" (OlatMovieViewer).
It is possible but not supposed that it can also lead to another multimedia problems in OLAT. To use this player correctly 
Flash Player in Browser is needed. 