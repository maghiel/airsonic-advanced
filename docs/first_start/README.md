# First start guide

This guide explains how to handle your first time starting up Airsonic-Advanced and will guide you through the process.

## Change Airsonic-Advanced process user

We strongly advice against running Airsonic-Advanced as root and recommend to create a separate, dedicated user and group instead.
By example by creating the user airsonic and adding it to the airsonic group. 
`groupadd airsonic`
`useradd -m -G airsonic airsonic`

To change this you can create a dedicated user that will run Airsonic-Advanced.

> NOTE: Please verify any permissions after you changed the process user.

## Set up user accounts

The default username and password for Airsonic is admin. You should absolutely change this to secure your server.

Go to Settings > Users to change this password.

In the same page you can also create new user accounts and specify which operations they are allowed to perform.

## Setting up media folders

You must tell Airsonic where you keep your music and videos.

Select `Settings` > `Media folders` to add folders. (If you don’t see this option you must first log on to Airsonic with a user that has administrative privileges, for instance the admin user).

Also note that Airsonic will organize your music according to how they are organized on your disk. Unlike many other music applications, Airsonic does not organize the music according to the tag information embedded in the files. (It does, however, also read the tags for presentation and search purposes.)

Consequently, it’s recommended that the music folders you add to Airsonic are organized in an **“artist/album/song”** manner. There are music managers, like MediaMonkey, that can help you achieve this.
