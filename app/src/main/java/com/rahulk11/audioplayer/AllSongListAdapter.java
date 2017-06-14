package com.rahulk11.audioplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import com.rahulk11.audioplayer.R;

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
//            imageLoader.displayImage(contentURI, mViewHolder.imageSongThm, options);

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
                    PopupMenu popup = new PopupMenu(mContext, v);
                    popup.getMenuInflater().inflate(R.menu.list_item_option, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.playnext:
                                    break;
                                case R.id.addtoque:
                                    break;
                                case R.id.addtoplaylist:
                                    break;
                                case R.id.gotoartis:
                                    break;
                                case R.id.gotoalbum:
                                    break;
                                case R.id.delete:
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
