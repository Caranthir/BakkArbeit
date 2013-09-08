package ws13.bakkarbeit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return imageId.size();
    }

    public Object getItem(int position) {
        return null;
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


        Uri targetUri = Uri.parse(tests.get(0));
        //tests contains the uri of the photo i'm trying to import from my phone gallery in string form
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
        imageView.setImageURI(new Uri);

        return imageView;
    }


    // references to our images
    public Integer[] photo = {
          
    };

}
