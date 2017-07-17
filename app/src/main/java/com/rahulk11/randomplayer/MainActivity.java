package com.rahulk11.randomplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rahulk11.randomplayer.helpers.AllSongListAdapter;
import com.rahulk11.randomplayer.helpers.BitmapPalette;
import com.rahulk11.randomplayer.helpers.PlayPauseView;
import com.rahulk11.randomplayer.helpers.PlaybackManager;
import com.rahulk11.randomplayer.slidingtabhelper.SlidingTabLayout;
import com.rahulk11.randomplayer.slidingtabhelper.ViewPagerAdapter;
import com.rahulk11.randomplayer.slidinguppanelhelper.SlidingUpPanelLayout;

import java.util.HashMap;

import static com.rahulk11.randomplayer.helpers.BitmapPalette.blurredBitmap;
import static com.rahulk11.randomplayer.helpers.BitmapPalette.darkVibrantBodyTextColor;
import static com.rahulk11.randomplayer.helpers.BitmapPalette.darkVibrantRGBColor;
import static com.rahulk11.randomplayer.helpers.BitmapPalette.darkVibrantTitleTextColor;
import static com.rahulk11.randomplayer.helpers.BitmapPalette.smallBitmap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SONG_TITLE = "songTitle";
    public static final String DISPLAY_NAME = "displayName";
    public static final String SONG_ID = "songID";
    public static final String SONG_PATH = "songPath";
    public static final String ALBUM_NAME = "albumName";
    public static final String ARTIST_NAME = "artistName";
    public static final String SONG_DURATION = "songDuration";
    public static final String SONG_POS = "songPosInList";
    public static final String SONG_PROGRESS = "songProgress";
    public static boolean shouldContinue = false;
    private static Context mContext;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"All Songs", "Playlists", "Favourites"};
    int Numboftabs = 3;
    private Toolbar toolbar;
    private ListView lv_songslist;
    private AllSongListAdapter mAllSongsListAdapter;
    private SlidingUpPanelLayout mLayout;
    private ImageView songAlbumbg, img_bottom_slideone, img_bottom_slidetwo,
            imgbtn_backward, imgbtn_forward, ivListBG, ivPanelBG;
    private PlayPauseView btn_playpause, btn_playpausePanel;
    private TextView txt_timeprogress, txt_timetotal, txt_playesongname,
            txt_songartistname, txt_playesongname_slidetoptwo, txt_songartistname_slidetoptwo;
    private RelativeLayout slidepanelchildtwo_topviewone, slidepanelchildtwo_topviewtwo;
    private LinearLayout llBottomLayout;
    private boolean isExpand = false;
    private PlaybackManager playbackManager;
    private SeekBar seekBar;
    private Handler seekHandler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (shouldContinue) {
                int currSeekPos = SongService.getCurrPos();
                int max = seekBar.getMax();
                if (currSeekPos > max) {
                    currSeekPos = 0;
                }

                txt_timeprogress.setText(calculateDuration(currSeekPos));
                seekBar.setProgress(currSeekPos);
                seekHandler.postDelayed(runnable, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

//        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);
//
//        // Assigning ViewPager View and setting the adapter
//        pager = (ViewPager) findViewById(R.id.pager);
//        pager.setAdapter(adapter);
//
//        // Assiging the Sliding Tab Layout View
//        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
//        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
//
//        // Setting Custom Color for the Scroll bar indicator of the Tab View
//        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//            @Override
//            public int getIndicatorColor(int position) {
//                return getResources().getColor(R.color.tabsScrollColor);
//            }
//        });
//
//        // Setting the ViewPager For the SlidingTabsLayout
//        tabs.setViewPager(pager);
        init();
        toolbarStatusBar();
        initListeners();
        initPlaybackManager();
        if (smallBitmap == null)
            BitmapPalette.getColorsFromBitmap(mContext, null, false);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void init() {
        lv_songslist = (ListView) findViewById(R.id.recycler_allSongs);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        songAlbumbg = (ImageView) findViewById(R.id.image_songAlbumbg_mid);
        ivPanelBG = (ImageView) findViewById(R.id.image_songAlbumbg);
        ivListBG = (ImageView) findViewById(R.id.iv_lvBG);
        img_bottom_slideone = (ImageView) findViewById(R.id.img_bottom_slideone);
        img_bottom_slidetwo = (ImageView) findViewById(R.id.img_bottom_slidetwo);

        llBottomLayout = (LinearLayout) findViewById(R.id.ll_bottom);
        txt_timeprogress = (TextView) findViewById(R.id.slidepanel_time_progress);
        txt_timetotal = (TextView) findViewById(R.id.slidepanel_time_total);
        imgbtn_backward = (ImageView) findViewById(R.id.btn_backward);
        imgbtn_forward = (ImageView) findViewById(R.id.btn_forward);

        btn_playpause = (PlayPauseView) findViewById(R.id.btn_play);
        btn_playpausePanel = (PlayPauseView) findViewById(R.id.bottombar_play);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setEnabled(false);
        btn_playpausePanel.Pause();
        btn_playpause.Pause();

        TypedValue typedvaluecoloraccent = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedvaluecoloraccent, true);

        txt_playesongname = (TextView) findViewById(R.id.txt_playesongname);
        txt_songartistname = (TextView) findViewById(R.id.txt_songartistname);
        txt_playesongname_slidetoptwo = (TextView) findViewById(R.id.txt_playesongname_slidetoptwo);
        txt_songartistname_slidetoptwo = (TextView) findViewById(R.id.txt_songartistname_slidetoptwo);

        slidepanelchildtwo_topviewone = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewone);
        slidepanelchildtwo_topviewtwo = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewtwo);

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
                    if (isExpand) {
//                        slidepanelchildtwo_topviewone.setAlpha(1.0f);
//                        slidepanelchildtwo_topviewtwo.setAlpha(1.0f -
//                                slideOffset);
                        slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
                        slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
                    } else {
//                        slidepanelchildtwo_topviewone.setAlpha(1.0f -
//                                slideOffset);
//                        slidepanelchildtwo_topviewtwo.setAlpha(1.0f);
                        slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
                        slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
                    }
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
//                loadSongInfo(hashMap, true);
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
                    PlaybackManager.playPauseEvent(false, false, false, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlaybackManager.showNotif(false);
            }
        });
    }

    public void setSeekProgress() {
        if (!seekBar.isEnabled())
            seekBar.setEnabled(true);
        seekHandler.removeCallbacks(runnable);
        shouldContinue = true;
//        if (duration != seekBar.getMax())
//            seekBar.setMax(duration);
        if (!btn_playpause.isPlay()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPlayPauseView(true);
                }
            });
        }
        runOnUiThread(runnable);
    }

    public void setAllSongs() {
        mAllSongsListAdapter = new AllSongListAdapter(mContext, PlaybackManager.songsList);
        lv_songslist.setAdapter(mAllSongsListAdapter);
    }

    @Override
    public void onClick(View v) {
        seekBar.setEnabled(true);
        switch (v.getId()) {
            case R.id.bottombar_play:
                if (PlaybackManager.playPauseEvent(false, SongService.isPlaying(), true, seekBar.getProgress())) {
//                    btn_playpause.Play();
//                    btn_playpausePanel.Play();
//                    thread = new Thread(runnable);
//                    thread.start();
                } else {
                    PlaybackManager.isManuallyPaused = true;
                    btn_playpause.Pause();
                    btn_playpausePanel.Pause();
                    shouldContinue = false;
                }
                break;

            case R.id.btn_play:
                if (PlaybackManager.playPauseEvent(false, SongService.isPlaying(), true, seekBar.getProgress())) {
//                    btn_playpause.Play();
//                    btn_playpausePanel.Play();
                } else {
                    PlaybackManager.isManuallyPaused = true;
                    btn_playpause.Pause();
                    btn_playpausePanel.Pause();
                    shouldContinue = false;
                }
                break;

            case R.id.btn_forward:
                playbackManager.playNext(true);
                break;

            case R.id.btn_backward:
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
        String progress = songDetail.get(SONG_PROGRESS);
        int milliSecProgress = Integer.parseInt(progress == null ? "0" : progress);
        if (txt_playesongname != null) {
            txt_playesongname.setText(title);
            txt_playesongname_slidetoptwo.setText(title);
            txt_songartistname.setText(artist);
            txt_songartistname_slidetoptwo.setText(artist);
            txt_timetotal.setText(calculateDuration(milliSecDuration));
            seekBar.setMax(milliSecDuration);
            txt_timeprogress.setText(calculateDuration(milliSecProgress));
            seekBar.setEnabled(true);
            if (seeking) {
                setSeekProgress();
            } else {
                seekBar.setProgress(milliSecProgress);
            }
        }
        try {
            BitmapPalette.getColorsFromBitmap(mContext, path, false);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
//        updateProgress(songsManager);
    }

    public void setBitmapColors() {
        if (songAlbumbg != null) {
            if (smallBitmap != null) {
                ivListBG.setImageBitmap(blurredBitmap);
                ivPanelBG.setImageBitmap(blurredBitmap);
                songAlbumbg.setImageBitmap(smallBitmap);
                img_bottom_slideone.setImageBitmap(smallBitmap);
                img_bottom_slidetwo.setImageBitmap(smallBitmap);
                img_bottom_slideone.setBackgroundColor(darkVibrantBodyTextColor);
                img_bottom_slidetwo.setBackgroundColor(darkVibrantBodyTextColor);
                toolbar.setBackgroundColor(darkVibrantRGBColor);
                toolbar.setTitleTextColor(darkVibrantTitleTextColor);
                slidepanelchildtwo_topviewone.setBackgroundColor(darkVibrantRGBColor);
                slidepanelchildtwo_topviewtwo.setBackgroundColor(darkVibrantRGBColor);
                txt_playesongname.setTextColor(darkVibrantBodyTextColor);
                txt_playesongname_slidetoptwo.setTextColor(darkVibrantBodyTextColor);
                txt_songartistname.setTextColor(darkVibrantBodyTextColor);
                txt_songartistname_slidetoptwo.setTextColor(darkVibrantBodyTextColor);
                txt_timetotal.setTextColor(darkVibrantBodyTextColor);
                txt_timeprogress.setTextColor(darkVibrantBodyTextColor);
//                llBottomLayout.setBackgroundColor(darkVibrantRGBColor);
            }
        }
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
            if (!SongService.isPlaying())
                seekBar.setEnabled(false);
        } else initPlaybackManager();
    }

    @Override
    public void onBackPressed() {
        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (SongService.isPlaying()) {
            super.onBackPressed();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (PlaybackManager.isServiceRunning && !SongService.isPlaying())
            PlaybackManager.stopService();
        super.onDestroy();
    }

    public void setPlayPauseView(boolean isPlaying) {

        if (btn_playpause != null)
            if (isPlaying) {
                btn_playpause.Play();
                btn_playpausePanel.Play();
            } else {
                btn_playpause.Pause();
                btn_playpausePanel.Pause();
                shouldContinue = false;
            }
    }
}
