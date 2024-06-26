/*
 This file is part of Airsonic.

 Airsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Airsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Airsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2016 (C) Airsonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.airsonic.player.controller;

import org.airsonic.player.domain.*;
import org.airsonic.player.service.MediaFolderService;
import org.airsonic.player.service.PlayQueueService;
import org.airsonic.player.service.PlayerService;
import org.airsonic.player.service.SecurityService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Controller for the creating a random play queue.
 *
 * @author Sindre Mehus
 */
@Controller
@RequestMapping({"/randomPlayQueue.view", "/randomPlayQueue"})
public class RandomPlayQueueController {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private PlayQueueService playQueueService;
    @Autowired
    private MediaFolderService mediaFolderService;

    @PostMapping
    protected String handleRandomPlayQueue(
            ModelMap model,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("size") Integer size,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "songRating", required = false) String songRating,
            @RequestParam(value = "lastPlayedValue", required = false) String lastPlayedValue,
            @RequestParam(value = "lastPlayedComp", required = false) String lastPlayedComp,
            @RequestParam(value = "albumRatingValue", required = false) Integer albumRatingValue,
            @RequestParam(value = "albumRatingComp", required = false) String albumRatingComp,
            @RequestParam(value = "playCountValue", required = false) Integer playCountValue,
            @RequestParam(value = "playCountComp", required = false) String playCountComp,
            @RequestParam(value = "format", required = false) String format,
            @RequestParam(value = "autoRandom", required = false) String autoRandom
    ) throws Exception {

        Integer fromYear = null;
        Integer toYear = null;
        Integer minAlbumRating = null;
        Integer maxAlbumRating = null;
        Integer minPlayCount = null;
        Integer maxPlayCount = null;
        Instant minLastPlayedDate = null;
        Instant maxLastPlayedDate = null;
        boolean doesShowStarredSongs = false;
        boolean doesShowUnstarredSongs = false;

        if (size == null) {
            size = 24;
        }

        // Handle the genre filter
        if (StringUtils.equalsIgnoreCase("any", genre)) {
            genre = null;
        }

        // Handle the release year filter
        if (!StringUtils.equalsIgnoreCase("any", year)) {
            String[] tmp = StringUtils.split(year);
            fromYear = Integer.parseInt(tmp[0]);
            toYear = Integer.parseInt(tmp[1]);
        }

        // Handle the song rating filter
        if (StringUtils.equalsIgnoreCase("any", songRating)) {
            doesShowStarredSongs = true;
            doesShowUnstarredSongs = true;
        } else if (StringUtils.equalsIgnoreCase("starred", songRating)) {
            doesShowStarredSongs = true;
            doesShowUnstarredSongs = false;
        } else if (StringUtils.equalsIgnoreCase("unstarred", songRating)) {
            doesShowStarredSongs = false;
            doesShowUnstarredSongs = true;
        }

        // Handle the last played date filter
        Instant lastPlayed = null;
        switch (lastPlayedValue) {
            case "1day":
                lastPlayed = Instant.now().minus(ChronoUnit.DAYS.getDuration());
                break;
            case "1week":
                lastPlayed = Instant.now().minus(ChronoUnit.WEEKS.getDuration());
                break;
            case "1month":
                lastPlayed = Instant.now().minus(ChronoUnit.MONTHS.getDuration());
                break;
            case "3months":
                lastPlayed = Instant.now().minus(ChronoUnit.MONTHS.getDuration().multipliedBy(3));
                break;
            case "6months":
                lastPlayed = Instant.now().minus(ChronoUnit.MONTHS.getDuration().multipliedBy(6));
                break;
            case "1year":
                lastPlayed = Instant.now().minus(ChronoUnit.YEARS.getDuration());
                break;
            case "any":
            default:
                break;
        }

        if (lastPlayed != null) {
            switch (lastPlayedComp) {
                case "lt":
                    minLastPlayedDate = null;
                    maxLastPlayedDate = lastPlayed;
                    break;
                case "gt":
                    minLastPlayedDate = lastPlayed;
                    maxLastPlayedDate = null;
                    break;
            }
        }

        // Handle the album rating filter
        if (albumRatingValue != null) {
            switch (albumRatingComp) {
                case "lt":
                    minAlbumRating = null;
                    maxAlbumRating = albumRatingValue - 1;
                    break;
                case "gt":
                    minAlbumRating = albumRatingValue + 1;
                    maxAlbumRating = null;
                    break;
                case "le":
                    minAlbumRating = null;
                    maxAlbumRating = albumRatingValue;
                    break;
                case "ge":
                    minAlbumRating = albumRatingValue;
                    maxAlbumRating = null;
                    break;
                case "eq":
                    minAlbumRating = albumRatingValue;
                    maxAlbumRating = albumRatingValue;
                    break;
            }
        }

        // Handle the play count filter
        if (playCountValue != null) {
            switch (playCountComp) {
                case "lt":
                    minPlayCount = null;
                    maxPlayCount = playCountValue - 1;
                    break;
                case "gt":
                    minPlayCount = playCountValue + 1;
                    maxPlayCount = null;
                    break;
                case "le":
                    minPlayCount = null;
                    maxPlayCount = playCountValue;
                    break;
                case "ge":
                    minPlayCount = playCountValue;
                    maxPlayCount = null;
                    break;
                case "eq":
                    minPlayCount = playCountValue;
                    maxPlayCount = playCountValue;
                    break;
            }
        }

        // Handle the format filter
        if (StringUtils.equalsIgnoreCase(format, "any")) format = null;

        // Handle the music folder filter
        String username = securityService.getCurrentUsername(request);
        Integer selectedMusicFolderId = ServletRequestUtils.getRequiredIntParameter(request, "musicFolderId");
        List<MusicFolder> musicFolders = mediaFolderService.getMusicFoldersForUser(username, selectedMusicFolderId);

        // Do we add to the current playqueue or do we replace it?
        boolean shouldAddToPlaylist = request.getParameter("addToPlaylist") != null;

        // Search the database using these criteria
        RandomSearchCriteria criteria = new RandomSearchCriteria(
                size,
                genre,
                fromYear,
                toYear,
                musicFolders,
                minLastPlayedDate,
                maxLastPlayedDate,
                minAlbumRating,
                maxAlbumRating,
                minPlayCount,
                maxPlayCount,
                doesShowStarredSongs,
                doesShowUnstarredSongs,
                format
        );
        Player player = playerService.getPlayer(request, response, username);
        playQueueService.addRandomCriteria(player, shouldAddToPlaylist, criteria, autoRandom != null);

        return "redirect:more.view";
    }

}
