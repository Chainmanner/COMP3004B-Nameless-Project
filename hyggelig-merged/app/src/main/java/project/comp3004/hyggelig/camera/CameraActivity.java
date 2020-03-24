// FIXME: Output images are rotated 90 degrees CCW and I don't know how to fix that.

package project.comp3004.hyggelig.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;

import project.comp3004.hyggelig.R;

public class CameraActivity extends AppCompatActivity
{
	private Camera theCamera;
	private CameraPreview thePreview;

	private String outPath;
	private boolean recordVideo;	// true = record video instead of taking a picture

	private Camera.PictureCallback thePictureCallback;

	private MediaRecorder theRecorder;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);

		theCamera = getCamera();

		thePreview = new CameraPreview(this, theCamera);
		FrameLayout previewFrame = this.findViewById(R.id.camera_preview);
		previewFrame.addView(thePreview);


		outPath = "";
		recordVideo = false;
		Bundle params = this.getIntent().getExtras();
		if ( params != null )
		{
			outPath = params.getString("outPath");
			recordVideo = params.getBoolean("recordVideo");
		}

		thePictureCallback = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.w("hyggelig", "onPictureTaken");
				saveData(data);
				setResult(RESULT_OK);
				finish();
			}
		};

		theRecorder = null;

		final Button button_capture = this.findViewById(R.id.button_capture);
		if ( recordVideo )
		{
			button_capture.setText("Start");
			button_capture.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					button_capture.setText("Stop");
					button_capture.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							stopRecording();
						}
					});
					startRecording();
				}
			});
		}
		else
		{
			button_capture.setText("Capture");
			button_capture.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					theCamera.takePicture(null, null, thePictureCallback);
				}
			});
		}
	}

	@Override
	public void onStart()
	{
		Log.w("hyggelig", "CameraActivity.onStart");
		super.onStart();
	}

	@Override
	public void onStop()
	{
		Log.w("hyggelig", "CameraActivity.onStop");
		super.onStop();
		theCamera.stopPreview();
		theCamera.release();
	}

	private static Camera getCamera()
	{
		Camera retCamera = null;
		try
		{
			retCamera = Camera.open();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while attempting to open camera: " + e.getMessage());
			e.printStackTrace();
		}
		return retCamera;
	}

	private void startRecording()
	{
		theRecorder = new MediaRecorder();

		theCamera.unlock();
		theRecorder.setCamera(theCamera);

		theRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		theRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

		theRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

		theRecorder.setOutputFile(outPath);

		theRecorder.setPreviewDisplay(thePreview.getHolder().getSurface());

		try
		{
			theRecorder.prepare();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while starting to record: " + e.getMessage());
			e.printStackTrace();
			theRecorder.release();
			return;
		}

		theRecorder.start();
	}

	private void stopRecording()
	{
		if ( theRecorder == null )
			return;

		theRecorder.stop();

		theRecorder.reset();

		theRecorder.release();

		theCamera.lock();
		//theCamera.stopPreview();
		//theCamera.release();
		setResult(RESULT_OK);
		finish();
	}

	private void saveData(byte[] data)
	{
		File outFile = new File(outPath);

		try
		{
			outFile.createNewFile();

			FileOutputStream fileOS = new FileOutputStream(outFile);
			fileOS.write(data);
			fileOS.close();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while attempting to save to " + outPath + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
}
