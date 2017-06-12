package net.trellisys.audioplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.trellisys.audioplayer.slidinguppanelhelper.PlayPauseView;
import net.trellisys.audioplayer.slidinguppanelhelper.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static Context mContext;

    public static final String SONG_TITLE = "songTitle";
    public static final String DISPLAY_NAME = "displayName";
    public static final String SONG_ID = "songID";
    public static final String SONG_PATH = "songPath";
    public static final String ALBUM_NAME = "albumName";
    public static final String ARTIST_NAME = "artistName";
    public static final String SONG_DURATION = "songDuration";
    public static final String SONG_POS = "songPosition";

    private ListView recycler_songslist;
    private AllSongListAdapter mAllSongsListAdapter;
    private SlidingUpPanelLayout mLayout;
    private ImageView songAlbumbg, img_bottom_slideone, img_bottom_slidetwo,
            imgbtn_backward, imgbtn_forward;
    private PlayPauseView btn_playpause, btn_playpausePanel;
    private TextView txt_timeprogress, txt_timetotal, txt_playesongname,
            txt_songartistname, txt_playesongname_slidetoptwo, txt_songartistname_slidetoptwo;

    private RelativeLayout slidepanelchildtwo_topviewone, slidepanelchildtwo_topviewtwo;
    private boolean isExpand = false;
    private SharedPreferences sharedPref;
    private PlaybackManager playbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
        initListeners();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            playbackManager = new PlaybackManager(mContext);
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 0);
        }
    }

    public static Context getInstance() {
        return mContext;
    }

    private void init() {
        recycler_songslist = (ListView) findViewById(R.id.recycler_allSongs);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        songAlbumbg = (ImageView) findViewById(R.id.image_songAlbumbg_mid);
        img_bottom_slideone = (ImageView) findViewById(R.id.img_bottom_slideone);
        img_bottom_slidetwo = (ImageView) findViewById(R.id.img_bottom_slidetwo);

        txt_timeprogress = (TextView) findViewById(R.id.slidepanel_time_progress);
        txt_timetotal = (TextView) findViewById(R.id.slidepanel_time_total);
        imgbtn_backward = (ImageView) findViewById(R.id.btn_backward);
        imgbtn_forward = (ImageView) findViewById(R.id.btn_forward);

        btn_playpause = (PlayPauseView) findViewById(R.id.btn_play);
        btn_playpausePanel = (PlayPauseView) findViewById(R.id.bottombar_play);

        btn_playpausePanel.Pause();
        btn_playpause.Pause();
        btn_playpausePanel.setOnClickListener(this);
        btn_playpause.setOnClickListener(this);

        TypedValue typedvaluecoloraccent = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedvaluecoloraccent, true);
        imgbtn_backward.setOnClickListener(this);
        imgbtn_forward.setOnClickListener(this);


        txt_playesongname = (TextView) findViewById(R.id.txt_playesongname);
        txt_songartistname = (TextView) findViewById(R.id.txt_songartistname);
        txt_playesongname_slidetoptwo = (TextView) findViewById(R.id.txt_playesongname_slidetoptwo);
        txt_songartistname_slidetoptwo = (TextView) findViewById(R.id.txt_songartistname_slidetoptwo);

        slidepanelchildtwo_topviewone = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewone);
        slidepanelchildtwo_topviewtwo = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewtwo);

        slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
        slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);

        slidepanelchildtwo_topviewone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            }
        });

        slidepanelchildtwo_topviewtwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            }
        });

    }

    private void initListeners() {

        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    // if (isExpand) {
                    // slidepanelchildtwo_topviewone.setAlpha(1.0f);
                    // slidepanelchildtwo_topviewtwo.setAlpha(1.0f -
                    // slideOffset);
                    // } else {
                    // slidepanelchildtwo_topviewone.setAlpha(1.0f -
                    // slideOffset);
                    // slidepanelchildtwo_topviewtwo.setAlpha(1.0f);
                    // }

                } else {
                    isExpand = true;
                    slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPanelExpanded(View panel) {
                isExpand = true;
            }

            @Override
            public void onPanelCollapsed(View panel) {
                isExpand = false;
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });

        recycler_songslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> hashMap = PlaybackManager.songsList.get(position);
                loadSongInfo(hashMap);
                PlaybackManager.playSong(hashMap.get(SONG_PATH),
                        hashMap.get(SONG_TITLE),
                        hashMap.get(ARTIST_NAME));
                btn_playpause.Play();
                btn_playpausePanel.Play();
            }
        });
    }

    public void setAllSongs() {
        mAllSongsListAdapter = new AllSongListAdapter(mContext, PlaybackManager.songsList);
        recycler_songslist.setAdapter(mAllSongsListAdapter);
        PlaybackManager.songsList = mAllSongsListAdapter.getNewList();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bottombar_play:
                playPauseEvent();
                break;

            case R.id.btn_play:
                playPauseEvent();
                break;

            case R.id.btn_forward:
                    playbackManager.playNext(false);
                break;

            case R.id.btn_backward:
                playbackManager.playPrev();
                break;

            default:
                break;
        }

    }

    private void playPauseEvent() {
        if (SongService.isPlaying()) {
            btn_playpause.Pause();
            btn_playpausePanel.Pause();
            startService(new Intent(mContext, SongService.class).setAction(SongService.ACTION_PAUSE));
        } else {
            if (!PlaybackManager.getLastPlayingSongPref().get(SONG_ID).equals("")) {
                startService(new Intent(mContext, SongService.class).setAction(SongService.ACTION_RESUME));
                btn_playpause.Play();
                btn_playpausePanel.Play();
            }
        }
    }

    public void loadSongInfo(HashMap<String, String> songDetail) {
        String title = songDetail.get("songTitle");
        String artist = songDetail.get("artistName");
        int milliSecDuration = Integer.parseInt(songDetail.get("songDuration"));
        if (txt_playesongname != null) {
            txt_playesongname.setText(title);
            txt_playesongname_slidetoptwo.setText(title);
            txt_songartistname.setText(artist);
            txt_songartistname_slidetoptwo.setText(artist);
            txt_timetotal.setText(calculateDuration(milliSecDuration));
        }

        playbackManager.setLastPlayingSongPref(songDetail);
//        updateProgress(songsManager);
    }


    public String calculateDuration(int millisec) {
        long sec = millisec / 1000;
        return sec != 0 ? String.format("%d:%02d", sec / 60, sec % 60) : "-:--";
    }

//     private void updateProgress(HashMap songDetail) {
////        if (audio_progress != null) {
////            // When SeekBar Draging Don't Show Progress
////            if (!isDragingStart) {
////                // Progress Value comming in point it range 0 to 1
////                audio_progress.setValue((int) (mSongDetail.audioProgress * 100));
////            }
////            String timeString = String.format("%d:%02d", mSongDetail.audioProgressSec / 60, mSongDetail.audioProgressSec % 60);
////            txt_timeprogress.setText(timeString);
////        }
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            playbackManager = new PlaybackManager(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPlayPauseView();
        loadSongInfo(PlaybackManager.getLastPlayingSongPref());
    }

    private void setPlayPauseView() {
        if (SongService.isPlaying()) {
            btn_playpause.Play();
            btn_playpausePanel.Play();
        } else {
            btn_playpause.Pause();
            btn_playpausePanel.Pause();
        }
    }
}
