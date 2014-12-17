package activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	// references to our images
	//public static ArrayList<Uri> photos = new ArrayList<Uri>();
	public static ArrayList<String> photosPath = new ArrayList<String>();

	public ImageAdapter(Context c) {
		mContext = c;
	}

	public int getCount() {
		return photosPath.size();
	}

	public Object getItem(int position) {
		return photosPath.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}

		loadBitmap(photosPath.get(position), imageView);
		return imageView;
	}


	private void loadBitmap(String path,ImageView imageView) {
		final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		task.execute(path);
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		protected Bitmap doInBackground(String... params) {
			return decodeSampledBitmapFromPath(params[0],  100, 100);
		}

		 // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            if (imageView != null) {
	                imageView.setImageBitmap(bitmap);
	            }
	        }

		}




	}
	// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	/** decodes a bitmap to a required size
	 * @param path path of the bitmap
	 * @param reqWidth required with, the bitmap will be downsized to
	 * @param reqHeight required height, the bitmap will be downsized to
	 * @return the bitmap that was downsized
	 */
	public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	//http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	/** To tell the decoder to subsample the image, loading a smaller version into memory, set 
	 * inSampleSize to true in your BitmapFactory.Options object. For example, an image with resolution 
	 * 2048x1536 that is decoded with an inSampleSize of 4 produces a bitmap of approximately 512x384. 
	 * Loading this into memory uses 0.75MB rather than 12MB for the full image (assuming a bitmap 
	 * configuration of ARGB_8888). Here’s a method to calculate a sample size value that is a power of 
	 * two based on a target width and height:
	 */
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	public void notifyDataSetChanged(){
		super.notifyDataSetChanged();
		Log.i("ImageAdapter", "dataSetChanged");
	}


}
