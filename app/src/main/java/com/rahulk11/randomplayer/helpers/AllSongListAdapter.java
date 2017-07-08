package com.rahulk11.randomplayer.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.rahulk11.randomplayer.MainActivity;
import com.rahulk11.randomplayer.R;

/**
 * Created by rahul on 6/8/2017.
 */

public class AllSongListAdapter extends BaseAdapter {

    private Context mContext = null;
    private LayoutInflater layoutInflater;
    private ArrayList<HashMap<String, String>> sList;

    public AllSongListAdapter(Context mContext, ArrayList<HashMap<String, String>> sList) {
        this.mContext = mContext;
        this.layoutInflater = LayoutInflater.from(mContext);
        this.sList = sList;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.inflate_allsongsitem, null);
            mViewHolder.song_row = (LinearLayout) convertView.findViewById(R.id.inflate_allsong_row);
            mViewHolder.textViewSongName = (TextView) convertView.findViewById(R.id.inflate_allsong_textsongname);
            mViewHolder.textViewSongArtisNameAndDuration = (TextView) convertView.findViewById(R.id.inflate_allsong_textsongArtisName_duration);
            mViewHolder.imageSongThm = (ImageView) convertView.findViewById(R.id.inflate_allsong_imgSongThumb);
            mViewHolder.imagemore = (ImageView) convertView.findViewById(R.id.img_moreicon);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        String title = "";
        String path = "";
        if (position < sList.size()) {
            title = sList.get(position).get("songTitle");
            path = sList.get(position).get("songPath");
        }

        mViewHolder.textViewSongName.setText(title);
        mViewHolder.textViewSongArtisNameAndDuration.setText(((MainActivity) mContext)
                .calculateDuration(Integer.parseInt(sList.get(position).get("songDuration")))
                + " | " + sList.get(position).get("artistName"));
//        Bitmap bitmap = BitmapPalette.getBitmapFromMediaPath(mContext, path, true);
        mViewHolder.imageSongThm.setImageResource(R.drawable.music);
        mViewHolder.imagemore.setColorFilter(Color.DKGRAY);
        if (Build.VERSION.SDK_INT > 15) {
            mViewHolder.imagemore.setImageAlpha(255);
        } else {
            mViewHolder.imagemore.setAlpha(255);
        }

        mViewHolder.imagemore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    final PopupMenu popup = new PopupMenu(mContext, v);
                    popup.getMenuInflater().inflate(R.menu.list_item_option, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            HashMap<String, String> hashmap = PlaybackManager.getPlayingSongPref();
                            int currSong = Integer.parseInt(hashmap.get(MainActivity.SONG_POS));
                            boolean removed = false;
                            switch (item.getItemId()) {
                                case R.id.playnext:
                                    while(PlaybackManager.shufflePosList.contains(position)) {
                                        removed = PlaybackManager.shufflePosList.remove((Integer)position);
                                    }
                                    if ( PlaybackManager.shufflePosList.contains(currSong)) {
                                        int currIndex = PlaybackManager.shufflePosList.indexOf(currSong);
                                        PlaybackManager.shufflePosList.add(currIndex + 1, position);
                                    }
                                    break;
                                case R.id.addtoque:
                                    while(PlaybackManager.shufflePosList.contains(position)) {
                                        removed = PlaybackManager.shufflePosList.remove((Integer)position);
                                    }
                                    if(currSong!=position)
                                        PlaybackManager.shufflePosList.add(position);
                                    break;
//                                case R.id.addtoplaylist:
//                                    break;
//                                case R.id.gotoartist:
//                                    break;
//                                case R.id.gotoalbum:
//                                    break;
                                case R.id.delete:
                                    while(PlaybackManager.shufflePosList.contains(position)) {
                                        removed = PlaybackManager.shufflePosList.remove((Integer)position);
                                    }
                                    PlaybackManager.songsList.remove(position);
                                    notifyDataSetChanged();
                                    break;
                                default:
                                    break;
                            }

                            return true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return convertView;
    }

    @Override
    public int getCount() {
        return (sList != null) ? sList.size() : 0;
    }

    class ViewHolder {
        TextView textViewSongName;
        ImageView imageSongThm, imagemore;
        TextView textViewSongArtisNameAndDuration;
        LinearLayout song_row;
    }

}
