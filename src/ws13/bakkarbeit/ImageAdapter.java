package ws13.bakkarbeit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        System.out.println("dfdsfdsf");
        mContext = c;
    }

    public int getCount() {
		return photos.size();
        //return imageId.size();
    }

    public Object getItem(int position) {
        return photos.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }


        //Uri targetUri = Uri.parse(photos.get(0));
        //tests contains the uri of the photo i'm trying to import from my phone gallery in string form
        //Bitmap bitmap;
        //bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
        System.out.println(photos.get(position)+ "dfdsfdsf");
        imageView.setImageURI(photos.get(position));
//Uri.parse
        return imageView;
    }


    // references to our images
    public static ArrayList<Uri> photos = new ArrayList<Uri>();

}
