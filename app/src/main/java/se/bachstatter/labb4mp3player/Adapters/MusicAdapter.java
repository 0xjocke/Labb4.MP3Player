package se.bachstatter.labb4mp3player.Adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import se.bachstatter.labb4mp3player.Models.Track;
import se.bachstatter.labb4mp3player.R;

/**
 * Created by Jocek on 2014-10-01.
 */
public class MusicAdapter extends ArrayAdapter<Track> {

    private  ArrayList<Track> trackArrayList;
    private Context context;

    public MusicAdapter(Context context, ArrayList<Track> trackArrayList) {
        super(context, R.layout.row_tracks, trackArrayList);
        this.context = context;
        this.trackArrayList = trackArrayList;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_tracks, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.nameHolder = (TextView) convertView.findViewById(R.id.trackNameTv);
            viewHolder.artistHolder = (TextView) convertView.findViewById(R.id.artistTv);
            viewHolder.imageHolder = (ImageView) convertView.findViewById(R.id.albumArtIv);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Track track = trackArrayList.get(position);

        viewHolder.nameHolder.setText(track.getFileName());
        viewHolder.artistHolder.setText(track.getArtist());
        viewHolder.imageHolder.setImageDrawable(track.getAlbumArt());

        return convertView;
    }

    public static class ViewHolder{
        public TextView nameHolder;
        public TextView artistHolder;
        public ImageView imageHolder;
    }
}
