package com.rahulk11.audioplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rahulk11.audioplayer.slidinguppanelhelper.PlayPauseView;
import com.rahulk11.audioplayer.slidinguppanelhelper.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.concurrent.RunnableFuture;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static Context mContext;

    public static final String SONG_TITLE = "songTitle";
    public static final String DISPLAY_NAME = "displayName";
    public static final String SONG_ID = "songID";
    public static final String SONG_PATH = "songPath";
    public static final String ALBUM_NAME = "albumName";
    public static final String ARTIST_NAME = "artistName";
    public static final String SONG_DURATION = "songDuration";
    public static final String SONG_POS = "songPosInList";

    private ListView lv_songslist;
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
    private SeekBar seekBar;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
        toolbarStatusBar();
        initListeners();
        initPlaybackManager();
    }

    private void initPlaybackManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            playbackManager = PlaybackManager.getInstance(mContext);
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 0);
            else playbackManager = PlaybackManager.getInstance(mContext);
        }
    }

    public void toolbarStatusBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Audio Player");
    }

    private void init() {
        lv_songslist = (ListView) findViewById(com.rahulk11.audioplayer.R.id.recycler_allSongs);
        mLayout = (SlidingUpPanelLayout) findViewById(com.rahulk11.audioplayer.R.id.sliding_layout);
        songAlbumbg = (ImageView) findViewById(com.rahulk11.audioplayer.R.id.image_songAlbumbg_mid);
        img_bottom_slideone = (ImageView) findViewById(com.rahulk11.audioplayer.R.id.img_bottom_slideone);
        img_bottom_slidetwo = (ImageView) findViewById(com.rahulk11.audioplayer.R.id.img_bottom_slidetwo);

        txt_timeprogress = (TextView) findViewById(com.rahulk11.audioplayer.R.id.slidepanel_time_progress);
        txt_timetotal = (TextView) findViewById(com.rahulk11.audioplayer.R.id.slidepanel_time_total);
        imgbtn_backward = (ImageView) findViewById(com.rahulk11.audioplayer.R.id.btn_backward);
        imgbtn_forward = (ImageView) findViewById(com.rahulk11.audioplayer.R.id.btn_forward);

        btn_playpause = (PlayPauseView) findViewById(com.rahulk11.audioplayer.R.id.btn_play);
        btn_playpausePanel = (PlayPauseView) findViewById(com.rahulk11.audioplayer.R.id.bottombar_play);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setEnabled(false);
        btn_playpausePanel.Pause();
        btn_playpause.Pause();

        TypedValue typedvaluecoloraccent = new TypedValue();
        getTheme().resolveAttribute(com.rahulk11.audioplayer.R.attr.colorAccent, typedvaluecoloraccent, true);

        txt_playesongname = (TextView) findViewById(com.rahulk11.audioplayer.R.id.txt_playesongname);
        txt_songartistname = (TextView) findViewById(com.rahulk11.audioplayer.R.id.txt_songartistname);
        txt_playesongname_slidetoptwo = (TextView) findViewById(com.rahulk11.audioplayer.R.id.txt_playesongname_slidetoptwo);
        txt_songartistname_slidetoptwo = (TextView) findViewById(com.rahulk11.audioplayer.R.id.txt_songartistname_slidetoptwo);

        slidepanelchildtwo_topviewone = (RelativeLayout) findViewById(com.rahulk11.audioplayer.R.id.slidepanelchildtwo_topviewone);
        slidepanelchildtwo_topviewtwo = (RelativeLayout) findViewById(com.rahulk11.audioplayer.R.id.slidepanelchildtwo_topviewtwo);

        slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
        slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
    }

    private void initListeners() {
        btn_playpausePanel.setOnClickListener(this);
        btn_playpause.setOnClickListener(this);
        imgbtn_backward.setOnClickListener(this);
        imgbtn_forward.setOnClickListener(this);

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


        lv_songslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> hashMap = PlaybackManager.songsList.get(position);
                loadSongInfo(hashMap, true);
                PlaybackManager.playSong(hashMap);
                btn_playpause.Play();
                btn_playpausePanel.Play();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    shouldContinue = false;
                    PlaybackManager.playPauseEvent(false, false, progress);
                    seekBar.setProgress(progress);
                    txt_timeprogress.setText(calculateDuration(progress));
                    shouldContinue = true;
                    thread = new Thread(runnable);
                    thread.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int currSeekPos = SongService.getCurrPos();
            int max = seekBar.getMax();
            seekBar.setProgress(currSeekPos);
            while (currSeekPos < max && shouldContinue) {
                try {
                    Thread.sleep(1000);
                    currSeekPos = SongService.getCurrPos();
                    final int finalCurrSeekPos = currSeekPos;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_timeprogress.setText(calculateDuration(finalCurrSeekPos));
                        }
                    });
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    return;
                }
                seekBar.setProgress(currSeekPos);
            }
        }
    };

    public void setAllSongs() {
        mAllSongsListAdapter = new AllSongListAdapter(mContext, PlaybackManager.songsList);
        lv_songslist.setAdapter(mAllSongsListAdapter);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case com.rahulk11.audioplayer.R.id.bottombar_play:
                if (PlaybackManager.playPauseEvent(false, SongService.isPlaying(), seekBar.getProgress())) {
                    btn_playpause.Play();
                    btn_playpausePanel.Play();
                    shouldContinue = true;
                    thread = new Thread(runnable);
                    thread.start();
                } else {
                    btn_playpause.Pause();
                    btn_playpausePanel.Pause();
                    shouldContinue = false;
                }

                break;

            case com.rahulk11.audioplayer.R.id.btn_play:
                if (PlaybackManager.playPauseEvent(false, SongService.isPlaying(), seekBar.getProgress())) {
                    btn_playpause.Play();
                    btn_playpausePanel.Play();
                    shouldContinue = true;
                    thread = new Thread(runnable);
                    thread.start();

                } else {
                    btn_playpause.Pause();
                    btn_playpausePanel.Pause();
                    shouldContinue = false;
                }
                break;

            case com.rahulk11.audioplayer.R.id.btn_forward:
                playbackManager.playNext(true);
                break;

            case com.rahulk11.audioplayer.R.id.btn_backward:
                playbackManager.playPrev(true);
                break;

            default:
                break;
        }

    }

    public void loadSongInfo(HashMap<String, String> songDetail, boolean seeking) {
        String title = songDetail.get(SONG_TITLE);
        String artist = songDetail.get(ARTIST_NAME);
        String path = songDetail.get(SONG_PATH);
        int milliSecDuration = Integer.parseInt(songDetail.get(SONG_DURATION));
        if (txt_playesongname != null) {
            txt_playesongname.setText(title);
            txt_playesongname_slidetoptwo.setText(title);
            txt_songartistname.setText(artist);
            txt_songartistname_slidetoptwo.setText(artist);
            txt_timetotal.setText(calculateDuration(milliSecDuration));
            seekBar.setEnabled(true);
            seekBar.setMax((Integer.parseInt(songDetail.get(SONG_DURATION))));
            seekBar.setProgress(0);
            shouldContinue = true;
            txt_timeprogress.setText("0:00");
            if (seeking) {
                thread = new Thread(runnable);
                thread.start();
            }
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            if(!path.equals("") && path!=null){
                mmr.setDataSource(path);
                byte[] byteData = mmr.getEmbeddedPicture();
                Bitmap bitmap = null;
                if(byteData != null){
                    bitmap = AllSongListAdapter.getBitmap(mContext, byteData, false);
                    if(bitmap!=null)
                        songAlbumbg.setImageBitmap(bitmap);
                    else songAlbumbg.setImageResource(R.drawable.play_button);
                } else songAlbumbg.setImageResource(R.drawable.play_button);
            }


        }
//        updateProgress(songsManager);
    }


    public String calculateDuration(int millisec) {
        long sec = millisec / 1000;
        return sec != 0 ? String.format("%d:%02d", sec / 60, sec % 60) : "0:00";
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    playbackManager = PlaybackManager.getInstance(mContext);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playbackManager != null) {
            loadSongInfo(PlaybackManager.getPlayingSongPref(), SongService.isPlaying());
            setPlayPauseView(SongService.isPlaying());
        } else initPlaybackManager();
    }

    @Override
    public void onBackPressed() {
        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else super.onBackPressed();

    }

    public void setPlayPauseView(boolean isPlaying) {
        int currPos = SongService.getCurrPos();
        String text = calculateDuration(currPos);
        seekBar.setProgress(currPos);
        txt_timeprogress.setText(text);
        if (btn_playpause != null)
            if (isPlaying) {
                btn_playpause.Play();
                btn_playpausePanel.Play();
                shouldContinue = true;
                thread = new Thread(runnable);
                thread.start();
            } else {
                btn_playpause.Pause();
                btn_playpausePanel.Pause();
                shouldContinue = false;
            }
    }

    public static boolean shouldContinue = false;
}
