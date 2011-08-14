package com.droid4you.util.cropimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private static final int PICK_FROM_CAMERA  = 1;
	private static final int PICK_FROM_GALLERY = 2;
	private static final int CROP_IMAGE = 3;
	private EditText filename;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		filename = (EditText) findViewById(R.id.filename);
		if (TextUtils.isEmpty(filename.getText().toString())) {
			filename.setText(String.valueOf(System.currentTimeMillis()) + ".jpg");
		}
		
		findViewById(R.id.pick_from_camera_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doTakePhotoAction();
			}
		});
		
		findViewById(R.id.pick_from_gallery_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doPickFromGalleryAction();
			}
		});
	}

	private Uri getImageUri() {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(), filename.getText().toString()));
	}

	private void doTakePhotoAction() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, getImageUri());

		try {
			intent.putExtra("return-data", false);
			startActivityForResult(intent, PICK_FROM_CAMERA);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void doPickFromGalleryAction() {
		
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		
		try {
			startActivityForResult(intent, PICK_FROM_GALLERY);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
			case PICK_FROM_GALLERY:
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				
				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				
				OutputStream out = null;
				FileInputStream in = null;
				try {
					in  = new FileInputStream(new File(filePath));
					out = new FileOutputStream(getImageUri().getPath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
				
				byte[] buffer = new byte[1024];
				int length;
				
				try {
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					
					out.flush();
					out.close();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
				// Intentional break-through
			case PICK_FROM_CAMERA:
				Intent intent = new Intent(this, CropImage.class);
				intent.putExtra("image-path", getImageUri().getPath());
				intent.putExtra("scale", true);
				startActivityForResult(intent, CROP_IMAGE);
				break;
			
			case CROP_IMAGE:
				ImageView v = (ImageView) findViewById(R.id.result_image);
	            Bitmap resultImage = BitmapFactory.decodeFile(getImageUri().getPath());
	            v.setImageBitmap(resultImage);
	            v.setVisibility(View.VISIBLE);
		}
		
	}
}