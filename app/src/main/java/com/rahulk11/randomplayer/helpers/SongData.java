package com.rahulk11.randomplayer.helpers;

/**
 * Created by absolutezero on 31/8/17.
 */

public class SongData {

    public static final String SONG_TITLE = "songTitle";
    public static final String DISPLAY_NAME = "displayName";
    public static final String SONG_ID = "songID";
    public static final String SONG_PATH = "songPath";
    public static final String ALBUM_NAME = "albumName";
    public static final String ARTIST_NAME = "artistName";
    public static final String SONG_DURATION = "songDuration";
    public static final String SONG_POS = "songPosInList";
    public static final String SONG_PROGRESS = "songProgress";

    private String songName,
            songTitle,
            songArtist,
            songAlbum,
            songPath;

    private int songId,
            songDur,
            songProg,
            songPos;

    public SongData(){

    }

    public SongData(int id, String name, String title, String artist, String album, int dur, String path){
        this.songId = id;
        this.songTitle = title;
        this.songArtist = artist;
        this.songAlbum = album;
        this.songDur = dur;
        this.songPath = path;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongTitle() {
        return songTitle;
    }


    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongAlbum(String songAlbum) {
        this.songAlbum = songAlbum;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public void setSongDur(int songDur) {
        this.songDur = songDur;
    }

    public int getSongDur() {
        return songDur;
    }

    public void setSongProg(int songProg) {
        this.songProg = songProg;
    }

    public int getSongProg() {
        return songProg;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public String getSongPath() {
        return songPath;
    }

    public int getSongPos() {
        return songPos;
    }
    public void setSongPos(int pos) {
        this.songPos = pos;
    }
}
