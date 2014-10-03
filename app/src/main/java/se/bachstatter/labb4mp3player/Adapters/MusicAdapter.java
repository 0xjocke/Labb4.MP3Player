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
    /**
     * getView get called for every row
     * Initialize inner Class ViewHolder
     * if converView is null we need to create a new view.
     * The LayoutInflater takes the chosen layout XML-file and creates View-objects from its contents.
     * Then I make a new instance on ViewHolder and saves my text- and imagesviews in class variables
     * and finally sets a tag on the viewHolder.
     *
     * else if convertView not is null we dont have to create a new.
     * we get our viewHolder by calling getTag on convertView (and casting it)
     *
     * Now we can get our track for this specific row and by calling get on trackList with our position.
     * We get our  trackname by getFileName() and puts it to our TextView which we access with help from ViewHolder.
     * And  does the same thing for artist and the album art.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
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

    /**
     * ViewHolder is used to store Text- and imageView
     */
    public static class ViewHolder{
        public TextView nameHolder;
        public TextView artistHolder;
        public ImageView imageHolder;
    }
}
