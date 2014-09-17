Remote-Control
==============

A Client-Server remote control system, with the server being remotely controlled.
It was originally designed to control a music player on my "server" computer via global hotkeys.

Server
------
The server runs and executes certain commands via the Java Robot class. 
The commands arrive via a standard Java Socket.

Currently only supports command that start with "music", but is relatively easy to extend.

Client
------
The client is a launch-and-fire type, with a new Client instance being started for every command. 
This is purely because it made my setup easy to work with: I had separate scripts to launch a Client for each different command.
The Client only receives a response when the server is monitoring or has to identify the current song.
It uses Growl to notify the user via a popup when this happens.

Music commands
--------------
**Control**
- monitor: Start monitoring for song changes (when new song starts playing, notification is sent back to client)
- stopmonitor: Stop monitoring for song changes

**Playback**
- start: Starts playback (or resumes from pause)
- stop: Stops playback (goes back to start of song)
- pause: Pauses playback (does not change position in song)
- restart: stop + start
- next: Skip to next song
- previous: Go back to previous song

**Metadata**
- identify: Gets song metadata (Artist, Title, Album, ...) and sends back to the client
- rate X: Gives the current song a rating of X
