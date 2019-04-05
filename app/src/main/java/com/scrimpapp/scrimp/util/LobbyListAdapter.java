package com.scrimpapp.scrimp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.scrimpapp.scrimp.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LobbyListAdapter extends ArrayAdapter {

    private final Activity context;

    private final List<String> imageIDarray;

    private final List<String> nameArray;

    private final List<String> infoArray;

    public LobbyListAdapter(Activity context, List<String> nameArrayParam, List<String> infoArrayParam, List<String> imageIDArrayParam){

        super(context, R.layout.list_tile , nameArrayParam);

        this.context=context;
        this.imageIDarray = imageIDArrayParam;
        this.nameArray = nameArrayParam;
        this.infoArray = infoArrayParam;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_tile, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = rowView.findViewById(R.id.tvDisplayNameInTile);
        TextView infoTextField = rowView.findViewById(R.id.tvBracuIdInTile);
        CircleImageView imageView = rowView.findViewById(R.id.imageView1ID);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray.get(position));
        infoTextField.setText(infoArray.get(position));

        String dpUrl = imageIDarray.get(position);
        new DpLoader(context, imageView).execute(dpUrl);

        return rowView;

    };
}

class DpLoader extends AsyncTask<String, Void, Bitmap> {

    final Context context;
    final CircleImageView profileImage;

    DpLoader(Context context, CircleImageView profileImage) {
        this.context = context;
        this.profileImage = profileImage;
    }

    @Override
    protected Bitmap doInBackground(String... src) {
        try {
            URL url = new URL(src[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
//            Toast.makeText(context, "Could not load profile image", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        profileImage.setImageBitmap(bitmap);
    }
}


